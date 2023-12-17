package kz.zsanzharko.implement;

import kz.zsanzharko.enums.ProfileRole;
import kz.zsanzharko.exception.InvalidProfileException;
import kz.zsanzharko.model.Profile;
import kz.zsanzharko.service.ProfileService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;

@Slf4j
public class BasicTelegramService {
    private final TelegramProfileService profileService;

    private final TelegramResponseService responseService;

    public BasicTelegramService(AbsSender sender) {
        this.responseService = new TelegramResponseService(sender);
        this.profileService = new TelegramProfileService(sender);
    }

    public void messageResolve(Profile profile, Message message) throws TelegramApiException,
            InvalidProfileException, SQLException {
        responseService.commandResolve(profile, message.getText());
        if (!profile.validate()) {
            profileService.blankProfile(profile, message.getText());
            return;
        }


        // todo include for users
        //  profile.getRole() == ProfileRole.USER
    }

    public void callbackResolve(Profile profile, CallbackQuery callback) throws TelegramApiException, SQLException {
        if (callback != null) {
            responseService.callbackResolve(profile, callback.getData());
        }
    }

    public Profile authorize(Update update) throws InvalidProfileException {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var message = update.getMessage();
            return profileService.authorize(message.getChatId());
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callback = update.getCallbackQuery();
            return profileService.authorize(callback.getMessage().getChatId());
        }
        log.error(update.toString());
        throw new RuntimeException("Can't authorize profile");
    }
}
