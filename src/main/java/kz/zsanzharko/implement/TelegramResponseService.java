package kz.zsanzharko.implement;

import kz.zsanzharko.enums.CallbackType;
import kz.zsanzharko.enums.CategoryListType;
import kz.zsanzharko.enums.CommandType;
import kz.zsanzharko.exception.InvalidProfileException;
import kz.zsanzharko.model.CategoryModel;
import kz.zsanzharko.model.Profile;
import kz.zsanzharko.service.CategoryService;
import kz.zsanzharko.service.CommandService;
import kz.zsanzharko.utils.KeyboardFactory;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import static kz.zsanzharko.config.TelegramMessageConfig.CALLBACK_DELIMITER;
import static kz.zsanzharko.enums.CategoryListType.BACK;
import static kz.zsanzharko.enums.CategoryListType.NEXT;

@Slf4j
public class TelegramResponseService implements CommandService {
    private final CategoryService categoryService = new CategoryServiceImpl();
    private final AbsSender sender;

    public TelegramResponseService(AbsSender sender) {
        this.sender = sender;
    }


    public void commandResolve(Profile profile, String text)
            throws TelegramApiException, InvalidProfileException, SQLException {
        if (!isCommand(text)) return;
        log.debug("RESOLVING command from chat {}, COMMAND: {}", profile.getChatId(), text);
        var command = resolve(text);
        if (command == null) return;
        else if (command == CommandType.NONE) {
            log.debug("UNRESOLVABLE command from chat {}, COMMAND: {}", profile.getChatId(), command);
            sender.execute(SendMessage.builder()
                    .text("Не допустимая команда. Пожалуйста, ознакомтесь с командами на главном меню")
                    .chatId(profile.getChatId())
                    .build());
            return;
        }
        log.debug("Detected command from chat {}, COMMAND: {}", profile.getChatId(), command);
        if (command == CommandType.START) {
            String message = """
            Приветствую вас, я бот по финансам!""";
            List<CategoryModel> categoryModelList = categoryService.getCategory(profile, NEXT, null);
            sender.execute(
                    SendMessage.builder()
                            .chatId(profile.getChatId())
                            .text(message)
                            .replyMarkup(KeyboardFactory.getCategoryKeyboardMarkup(categoryModelList))
                            .build()
            );
        }
    }

    public void callbackResolve(Profile profile, String callbackData)
            throws IllegalArgumentException, SQLException, TelegramApiException {
        log.debug("Resolve callback for profile: {}, Callback data: '{}'",
                profile.getChatId(), callbackData);
        CallbackType callbackType = CallbackType.valueOf(
                callbackData.substring(0, callbackData.indexOf(CALLBACK_DELIMITER)));
        String data = callbackData.substring(callbackData.indexOf(CALLBACK_DELIMITER) + 1);
        if (callbackType == CallbackType.CATEGORY) {
            String v = data.substring(data.indexOf(CALLBACK_DELIMITER) + 1);
            Long id = v.equals("null") ? null : Long.valueOf(v);
            List<CategoryModel> categories = switch (CategoryListType.valueOf(
                            data.substring(0, data.indexOf(CALLBACK_DELIMITER)))) {
                case NEXT -> categoryService.getCategory(profile, NEXT,
                        id);
                case BACK -> categoryService.getCategory(profile,
                        BACK, id);
            };
            Optional<CategoryModel> categoryModel = categories.stream().findFirst();
            Long parentId;
            if (categoryModel.isPresent()) {
                parentId = categoryModel.get().getIdParentCategory();
            } else {
                sender.execute(SendMessage.builder()
                        .chatId(profile.getChatId())
                        .text("К сожалению у нас нету предоставляемой услуги. Повторите попытку позже. /start")
                        .build());
                return;
            }

            sender.execute(SendMessage.builder()
                    .chatId(profile.getChatId())
                    .text("Выберите категорию.")
                    .replyMarkup(KeyboardFactory.getCategoryKeyboardMarkup(categories, parentId))
                    .build());
            log.info(categories.toString());
        }
    }

    private CommandType resolve(String text) {
        if(text.startsWith("/")) {
            text = text.substring(1);
            String command = text.toLowerCase(Locale.ROOT);
            for (CommandType value : CommandType.values()) {
                if (command.equals(value.getCommand())) {
                    return value;
                }
            }
            return CommandType.NONE;
        }
        return null;
    }

    public boolean isCommand(String command) {
        return command.startsWith("/");
    }
}
