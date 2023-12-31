package kz.zsanzharko.implement;

import kz.zsanzharko.model.Profile;
import lombok.extern.slf4j.Slf4j;
import kz.zsanzharko.exception.InvalidProfileException;
import kz.zsanzharko.service.ProfileService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class TelegramProfileService {
    private final AbsSender sender;
    private final TelegramResponseService telegramResponseService;
    private final ProfileService profileService;

    public TelegramProfileService(AbsSender sender) {
        this.sender = sender;
        this.profileService = new ProfileServiceImpl();
        this.telegramResponseService = new TelegramResponseService(sender);
    }

    public Profile authorize(Long chatId) throws InvalidProfileException {
        log.debug("Authorizing chat id {}", chatId);
        var profile = profileService.auth(chatId);
        log.debug("Profile {} is found.", profile);
        return profile;
    }

    public void blankProfile(Profile profile, String credentials) throws TelegramApiException {
        if (telegramResponseService.isCommand(credentials)) {
            sendAuthorizeMessage(profile.getChatId());
            return;
        }
        try {
            profileService.setInitials(profile.getChatId(), credentials);
        } catch (InvalidProfileException e) {
            sender.execute(SendMessage.builder()
                    .chatId(profile.getChatId())
                    .text(e.getClientMessage())
                    .build());
            throw new RuntimeException(e);
        }
        // Жесть, очень плохой код. Тот кто будет переделывать его, прошу прощения -_-
        String message = "Вы успешно авторизовались! ";
        try {
            profileService.validProfile(profile.getChatId());
            message += "Теперь попробуйте нажать на /report";
        } catch (InvalidProfileException e) {
            message += e.getClientMessage();
        }
        sender.execute(SendMessage.builder()
                .chatId(profile.getChatId())
                .text(message)
                .build());
    }

    private void sendAuthorizeMessage(Long chatId) throws TelegramApiException {
        sender.execute(SendMessage.builder()
                .chatId(chatId)
                .text("Просим написать ваше имя фамилию следующим сообщением.")
                .build());
    }
}
