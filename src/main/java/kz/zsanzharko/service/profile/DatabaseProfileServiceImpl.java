package kz.zsanzharko.service.profile;

import kz.zsanzharko.enums.Lang;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import kz.zsanzharko.enums.ProfileRole;
import kz.zsanzharko.enums.ProfileState;
import kz.zsanzharko.exception.InvalidProfileException;
import kz.zsanzharko.model.Profile;
import kz.zsanzharko.utils.DatabaseConnector;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class DatabaseProfileServiceImpl implements ProfileService {
    private final DatabaseConnector connector;

    public DatabaseProfileServiceImpl() {
        this.connector = DatabaseConnector.getInstance();
    }

    @Override
    public Profile auth(Long chatId) throws InvalidProfileException {
        final String sql = """
                SELECT
                "chat_id", "profile_state", "full_name",
                "profile_role", "is_block", "is_accepted", il."fm_l" as lang
                FROM mark_checker.profiles
                left join mark_checker.i_lang il on il.id = profiles.lang
                WHERE "chat_id" = ?
                """;
        try (PreparedStatement statement = connector.getConnection().prepareStatement(sql)) {
            statement.setLong(1, chatId);
            log.debug("\n{}", statement);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                {
                    String errorMessage;
                    String clientMessage;
                    if (result.getBoolean("is_block")) {
                        errorMessage = String.format("Profile %s is blocked", chatId);
                        clientMessage = "Вы были заблокированны администратором.";
                        throw new InvalidProfileException(errorMessage, clientMessage, chatId);
                    }
                }
                Lang lang = null;
                String sLang = result.getString("lang");
                for(Lang l : Lang.values()) {
                    if (sLang.equals(l.getLangText())) {
                        lang = l;
                    }
                }
                if (lang == null) lang = Lang.RU;
                return Profile.builder()
                        .chatId(chatId)
                        .fullName(result.getString("full_name"))
                        .role(ProfileRole.valueOf(result.getString("profile_role")))
                        .state(ProfileState.valueOf(result.getString("profile_state")))
                        .lang(lang)
                        .build();
            } else {
                return createProfile(chatId);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Profile createProfile(Long chatId) throws SQLException {
        var profile = new Profile();
        profile.setState(ProfileState.REGISTER);
        profile.setRole(ProfileRole.USER);
        profile.setChatId(chatId);
        val sql = """
                INSERT INTO mark_checker.profiles (chat_id, profile_state)
                VALUES (?, ?)
                """;
        PreparedStatement statement = connector.getConnection().prepareStatement(sql);
        statement.setLong(1, profile.getChatId());
        statement.setString(2, String.valueOf(profile.getState()));
        statement.executeUpdate();
        statement.close();
        return profile;
    }

    @Override
    public void setInitials(Long chatId, String fullName) throws InvalidProfileException {
        var profile = auth(chatId);
        profile.setFullName(fullName);
        profile.setState(ProfileState.NONE);
        final String sql = """
                UPDATE mark_checker.profiles
                SET "full_name" = ?,
                    "profile_state" = ?
                    WHERE "chat_id" = ?
                    RETURNING chat_id AS id;
                """;
        try (PreparedStatement statement = connector.getConnection().prepareStatement(sql)) {
            statement.setString(1, profile.getFullName());
            statement.setString(2, profile.getState().toString());
            statement.setLong(3, profile.getChatId());
            log.debug("\n{}", statement);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                Long expectChatId = result.getLong("id");
                if (expectChatId.equals(profile.getChatId())) {
                    return;
                }
            }
            String errorMessage = String.format("Profile %d is not found, when user is set initials",
                    profile.getChatId());
            String clientMessage = "Извините, но мы не смогли сохранить ваши данные. " +
                                   "Пожалуйста повторите позднее, либо напишите @sanzharrko";
            throw new InvalidProfileException(errorMessage, clientMessage, chatId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean validProfile(Long chatId) throws InvalidProfileException {
        final String sql = """
                select chat_id, profile_state, is_accepted
                from mark_checker.profiles
                where chat_id = ?
                """;
        try (PreparedStatement statement = connector.getConnection().prepareStatement(sql)) {
            statement.setLong(1, chatId);
            log.debug("\n{}", statement);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                Long expectChatId = result.getLong("chat_id");
                ProfileState state = ProfileState.valueOf(result.getString("profile_state"));
                if (state == ProfileState.REGISTER) {
                    log.warn("Profile with chat id: {}, not registered", expectChatId);
                    String errorMessage = String.format("Profile %s is not registered", chatId);
                    String clientMessage = """
                            Вы не прошли регистрацию ваших инициал.
                            Просим написать ваше имя фамилию следующим сообщением.
                            """;
                    throw new InvalidProfileException(errorMessage, clientMessage, chatId);
                } else if (!result.getBoolean("is_accepted")) {
                    log.warn("Profile with chat id: {}, not accepted", expectChatId);
                    String errorMessage = String.format("Profile %s is not accepted", chatId);
                    String clientMessage = "В данный момент сервисы не доступны, мы проверяем вашу личность.";
                    throw new InvalidProfileException(errorMessage, clientMessage, chatId);
                }
                if (expectChatId.equals(chatId)) return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public void enableMarkChecker(Profile profile) {
        if (profile.getState() == ProfileState.IN_QUESTIONS) return;
        final String sql = """
                update mark_checker.profiles
                set profile_state = ?
                where chat_id = ?
                """;
        try (PreparedStatement statement = connector.getConnection().prepareStatement(sql)) {
            statement.setString(1, String.valueOf(ProfileState.IN_QUESTIONS));
            statement.setLong(2, profile.getChatId());
            log.debug("\n{}", statement);
            statement.executeUpdate();
            profile.setState(ProfileState.IN_QUESTIONS);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disableMarkChecker(Profile profile) {
        if (profile.getState() == ProfileState.NONE) return;

        final String sql = """
                update mark_checker.profiles
                set profile_state = ?
                where chat_id = ?
                """;
        try (PreparedStatement statement = connector.getConnection().prepareStatement(sql)) {
            statement.setString(1, String.valueOf(ProfileState.NONE));
            statement.setLong(2, profile.getChatId());
            log.debug("\n{}", statement);
            statement.executeUpdate();
            profile.setState(ProfileState.NONE);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
