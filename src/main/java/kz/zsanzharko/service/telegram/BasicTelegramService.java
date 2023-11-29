package kz.zsanzharko.service.telegram;

import kz.zsanzharko.enums.CallbackType;
import kz.zsanzharko.model.CategoryModel;
import kz.zsanzharko.service.category.CategoryService;
import kz.zsanzharko.service.category.CategoryServiceImpl;
import kz.zsanzharko.utils.KeyboardFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import kz.zsanzharko.enums.CommandType;
import kz.zsanzharko.enums.ProfileRole;
import kz.zsanzharko.enums.ProfileState;
import kz.zsanzharko.exception.IncorrectAnswerForQuestionException;
import kz.zsanzharko.exception.InvalidProfileException;
import kz.zsanzharko.model.AnswerQuestionModel;
import kz.zsanzharko.model.Profile;
import kz.zsanzharko.model.QuestionModel;
import kz.zsanzharko.service.profile.DatabaseProfileServiceImpl;
import kz.zsanzharko.service.profile.ProfileService;
import kz.zsanzharko.service.question.DatabaseQuestionServiceImpl;
import kz.zsanzharko.service.question.QuestionService;
import kz.zsanzharko.utils.CommandUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import static kz.zsanzharko.config.TelegramMessageConfig.CALLBACK_DELIMITER;

@Slf4j
public class BasicTelegramService {
    private final AbsSender sender;
    private final ProfileService profileService = new DatabaseProfileServiceImpl();
    private final QuestionService questionService = new DatabaseQuestionServiceImpl();
    private final CategoryService categoryService = new CategoryServiceImpl();
    private final CommandUtils commandUtils = new CommandUtils();

    public BasicTelegramService(AbsSender sender) {
        this.sender = sender;
    }

    public void commandResolve(Profile profile, String text)
            throws TelegramApiException, InvalidProfileException, SQLException {
        log.debug("RESOLVING command from chat {}, COMMAND: {}", profile.getChatId(), text);
        var command = commandUtils.resolve(text);
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
            welcomeMessage(profile);
        }
    }

    public void messageResolve(Profile profile, String text) throws TelegramApiException {
        if (profile.getRole() == ProfileRole.USER) {
            if (Objects.requireNonNull(profile.getState()) == ProfileState.IN_QUESTIONS) {
                if (questionService.isFinished(profile.getChatId())) {
                    sendFinishReport(profile);
                    return;
                }
                inQuestionState(profile, text);
            }
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
            Long currentId = null;
            if (!data.substring(0, data.indexOf(CALLBACK_DELIMITER)).equals("null")) {
                currentId = Long.parseLong(data.substring(0, data.indexOf(CALLBACK_DELIMITER)));
            }
            data = data.substring(data.indexOf(CALLBACK_DELIMITER) + 1);
            Long parentId = null;
            if (!data.equals("null")) {
                parentId = Long.parseLong(data);
            }
            var categories = categoryService.getCategory(profile, currentId, parentId);
            sender.execute(SendMessage.builder()
                    .chatId(profile.getChatId())
                            .text("Выберите категорию.")
                            .replyMarkup(KeyboardFactory.getCategoryKeyboardMarkup(categories, currentId))
                    .build());
            log.info(categories.toString());
        }
    }

    private void inQuestionState(Profile profile, String text) throws TelegramApiException {
        log.debug("Check profile: {} for text: {}", profile.getChatId(), text);
        log.debug("Text is not /report");
        log.debug("Getting question for profile {}", profile.getChatId());
        if (commandUtils.isCommand(text)) {
            sendQuestion(profile);
            return;
        }
        var question = questionService.getQuestion(profile.getChatId());
        log.debug("Setting answer for {}, text: {}", profile.getChatId(), text);
        try {
            questionService.setAnswer(AnswerQuestionModel.builder()
                    .profileId(profile.getChatId())
                    .questionId(question.getId())
                    .answer(text)
                            .answerType(question.getAnswerType())
                    .build());
            log.debug("Answer is saved for {}, text: {}", profile.getChatId(), text);
        } catch (IncorrectAnswerForQuestionException e) {
            log.debug("{} Answer is not correct: {}", profile.getChatId(), text);
            sender.execute(SendMessage.builder()
                            .chatId(profile.getChatId())
                            .text(e.getClientMessage())
                    .build());
            return;
        }
        sendQuestion(profile);
    }

    private void sendQuestion(Profile profile) throws TelegramApiException {
        var question = questionService.getQuestion(profile.getChatId());
        if (question == null) {
            log.debug("Questions for {} is finished. Disabling Question state", profile.getChatId());
            sendFinishReport(profile);
            return;
        }
        val textConstructor = generateQuestionPattern(question);
        sender.execute(SendMessage.builder()
                .chatId(profile.getChatId())
                .text(textConstructor)
                .build()
        );
    }

    private String generateQuestionPattern(QuestionModel question) {
        return String.valueOf(question.getId()) +
               '.' +
               question.getTitle() +
               '?';
    }

    private void sendFinishReport(Profile profile) throws TelegramApiException {
        profileService.disableMarkChecker(profile);
        sender.execute(SendMessage.builder()
                .chatId(profile.getChatId())
                .text("Вы ответили на все вопросы, увидимся завтра!")
                .build());
    }


    public boolean isCommand(String command) {
        return commandUtils.isCommand(command);
    }

    private void welcomeMessage(Profile profile) throws TelegramApiException, SQLException {
        val text = """
                Приветствую вас, я бот по финансам!""";
        List<CategoryModel> categoryModelList = categoryService.getCategory(profile, null, null);
        sender.execute(
                SendMessage.builder()
                        .chatId(profile.getChatId())
                        .text(text)
                        .replyMarkup(KeyboardFactory.getCategoryKeyboardMarkup(categoryModelList, null))
                        .build()
        );
    }
}
