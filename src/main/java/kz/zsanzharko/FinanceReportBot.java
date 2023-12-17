package kz.zsanzharko;

import kz.zsanzharko.exception.InvalidProfileException;
import kz.zsanzharko.implement.BasicTelegramService;
import kz.zsanzharko.model.Profile;
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
    private final BasicTelegramService service = new BasicTelegramService(this);
    private final String username;


    public FinanceReportBot(String token, String username) {
        super(token);
        this.username = username;
    }

    @SneakyThrows({TelegramApiException.class})
    @Override
    public void onUpdateReceived(Update update) {
        try {
            Profile profile = service.authorize(update);
            if (update.hasMessage() && update.getMessage().hasText()) {
                var message = update.getMessage();
                service.messageResolve(profile, message);
            } else if (update.hasCallbackQuery()) {
                CallbackQuery callback = update.getCallbackQuery();
                service.callbackResolve(profile, callback);
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
