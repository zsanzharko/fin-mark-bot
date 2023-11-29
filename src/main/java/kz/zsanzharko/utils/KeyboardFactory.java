package kz.zsanzharko.utils;


import kz.zsanzharko.enums.CallbackType;
import kz.zsanzharko.model.CategoryModel;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static kz.zsanzharko.config.TelegramMessageConfig.CALLBACK_DELIMITER;
import static kz.zsanzharko.config.TelegramMessageConfig.MESSAGE_COLUMNS;

@Slf4j
public class KeyboardFactory {

    private static ReplyKeyboard getBackRowKeyboard(InlineKeyboardMarkup inlineKeyboard,
                                                    List<List<InlineKeyboardButton>> rowsInline,
                                                    Long parentId) {
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("🔙 Назад");
        button.setCallbackData(String.valueOf(parentId));
        rowInline.add(button);
        rowsInline.add(rowInline);
        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public static InlineKeyboardMarkup getCategoryKeyboardMarkup(List<CategoryModel> categoryModelList,
                                                                  Long parentId) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton button;
        String callback;
        for (int i = 0; i < categoryModelList.size(); i++) {
            var model = categoryModelList.get(i);
            button = new InlineKeyboardButton();
            if (i % MESSAGE_COLUMNS == 0) {
                rowsInline.add(rowInline);
                rowInline = new ArrayList<>();
            }
//            todo check parent id is need or not
            callback = CallbackType.CATEGORY + CALLBACK_DELIMITER
                    + model.getId() + CALLBACK_DELIMITER + parentId;
            log.debug("Setting button title: '{}', callback: '{}'",
                    model.getTitle(), callback);
            button.setText(model.getTitle());
            button.setCallbackData(callback);
            rowInline.add(button);
        }
        rowsInline.add(rowInline);
        //set back button
        rowInline = new ArrayList<>(1);
        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText("🔙 Назад");
        backButton.setCallbackData(CallbackType.CATEGORY + CALLBACK_DELIMITER + "null"
                + CALLBACK_DELIMITER + parentId);
        rowInline.add(backButton);
        rowsInline.add(rowInline);
        // set keyboard
        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }
}
