package kz.zsanzharko;

import kz.zsanzharko.exception.InvalidProfileException;
import kz.zsanzharko.service.telegram.BasicTelegramService;
import kz.zsanzharko.service.telegram.ProfileTelegramService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;

@Slf4j
public class FinanceReportBot extends TelegramLongPollingBot {
    private final String username;
    private final BasicTelegramService telegramService = new BasicTelegramService(this);
    private final ProfileTelegramService profileService = new ProfileTelegramService(this);



    public FinanceReportBot(String token, String username) {
        super(token);
        this.username = username;
    }

    @SneakyThrows({TelegramApiException.class})
    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                var message = update.getMessage();
                var profile = profileService.authorize(message.getChatId());
                if (telegramService.isCommand(message.getText())) {
                    telegramService.commandResolve(profile, message.getText());
                }
                if (!profile.validate()) {
                    profileService.blankProfile(profile, message.getText());
                    return;
                }
                telegramService.messageResolve(profile, message.getText());
            } else if (update.hasCallbackQuery()) {
                CallbackQuery callback = update.getCallbackQuery();
                var profile = profileService.authorize(callback.getMessage().getChatId());
                telegramService.callbackResolve(profile, callback.getData());
            }
        } catch (InvalidProfileException e) {
            execute(SendMessage.builder()
                    .chatId(e.getChatId())
                    .text(e.getClientMessage())
                    .build());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return username;
    }
}
