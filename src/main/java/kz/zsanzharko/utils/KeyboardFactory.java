package kz.zsanzharko.utils;


import kz.zsanzharko.enums.CallbackType;
import kz.zsanzharko.enums.CategoryListType;
import kz.zsanzharko.model.CategoryModel;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static kz.zsanzharko.config.TelegramMessageConfig.CALLBACK_DELIMITER;
import static kz.zsanzharko.config.TelegramMessageConfig.MESSAGE_COLUMNS;

@Slf4j
public class KeyboardFactory {

    public static InlineKeyboardMarkup getCategoryKeyboardMarkup(List<CategoryModel> categoryModelList,
                                                                  Long parentId) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton button;
        for (int i = 0; i < categoryModelList.size(); i++) {
            var model = categoryModelList.get(i);
            button = new InlineKeyboardButton();
            if (i % MESSAGE_COLUMNS == 0) {
                rowsInline.add(rowInline);
                rowInline = new ArrayList<>();
            }
            log.debug("Setting button title: '{}', callback: '{}'", model.getTitle(),
                    getCategoryCallback(CategoryListType.NEXT, model.getId()));
            button.setText(model.getTitle());
            button.setCallbackData(getCategoryCallback(CategoryListType.NEXT, model.getId()));
            rowInline.add(button);
        }
        rowsInline.add(rowInline);
        setCategoryBackButtonKeyboard(rowsInline, parentId);

        // set keyboard
        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public static InlineKeyboardMarkup getCategoryKeyboardMarkup(List<CategoryModel> categoryModelList) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton button;
        for (int i = 0; i < categoryModelList.size(); i++) {
            var model = categoryModelList.get(i);
            button = new InlineKeyboardButton();
            if (i % MESSAGE_COLUMNS == 0) {
                rowsInline.add(rowInline);
                rowInline = new ArrayList<>();
            }
            log.debug("Setting button title: '{}', callback: '{}'", model.getTitle(),
                    getCategoryCallback(CategoryListType.NEXT, model.getId()));
            button.setText(model.getTitle());
            button.setCallbackData(getCategoryCallback(CategoryListType.NEXT, model.getId()));
            rowInline.add(button);
        }
        rowsInline.add(rowInline);
        setCategoryBackButtonKeyboard(rowsInline, null);

        // set keyboard
        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    private static void setCategoryBackButtonKeyboard(
            List<List<InlineKeyboardButton>> rowsInline,
            Long parentId) {
        List<InlineKeyboardButton> rowInline = new ArrayList<>(1);
        InlineKeyboardButton backButton = new InlineKeyboardButton();
        // todo exclude to props
        backButton.setText("🔙 Назад");
        backButton.setCallbackData(getCategoryCallback(CategoryListType.BACK, parentId));
        rowInline.add(backButton);
        rowsInline.add(rowInline);
    }

    private static String getCategoryCallback(CategoryListType type, Long id) {
        return CallbackType.CATEGORY + CALLBACK_DELIMITER
                + type + CALLBACK_DELIMITER + id;
    }
}
