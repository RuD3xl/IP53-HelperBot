package ua.ip53.telegrambot.service.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class KeyboardFactory {
    public InlineKeyboardMarkup createInlineKeyboardMarkup(
            List<String> text,
            List<Integer> configuration,
            List<String> data
    ) {
        if(text.size() != data.size() || text.size() != configuration.stream().reduce(0, Integer::sum)) {
            log.error("Wrong arguments: [" + text + ", " + configuration + ", " + data + "]");
            return null;
        }
        List<List<InlineKeyboardButton>> keyboard = getInlineKeyboard(text, configuration, data);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        return markup;
    }

    private List<List<InlineKeyboardButton>> getInlineKeyboard(
            List<String> text,
            List<Integer> configuration,
            List<String> data) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        int index = 0;
        for (Integer rowNumber : configuration) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int i = 0; i < rowNumber; i++) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(text.get(index));

                String buttonData = data.get(index);
                if (buttonData != null && (buttonData.startsWith("http://") || buttonData.startsWith("https://"))) {
                    button.setUrl(buttonData);
                } else {
                    button.setCallbackData(buttonData);
                }

                index++;
                row.add(button);
            }
            keyboard.add(row);
        }
        return keyboard;
    }

    public ReplyKeyboard createReplyKeyboard(
            List<String> text,
            List<Integer> configuration
    ){
        if(text.size() != configuration.stream().reduce(0, Integer::sum)) {
            log.error("Wrong arguments: [" + text + ", " + configuration + "]");
            return null;
        }

        // ❗️ ВИПРАВЛЕННЯ 1: Викликаємо getReplyKeyboard і передаємо результат в setKeyboard
        List<KeyboardRow> keyboard = getReplyKeyboard(text, configuration);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(keyboard); // Тепер тут не порожній список
        markup.setResizeKeyboard(true); // Дуже корисна опція

        // ❗️ РЕКОМЕНДАЦІЯ: 'true' сховає клавіатуру після натискання.
        // 'false' залишить її відкритою, як постійні підказки.
        markup.setOneTimeKeyboard(false);

        return markup;
    }

    private List<KeyboardRow> getReplyKeyboard(
            List<String> text,
            List<Integer> configuration){
        List<KeyboardRow> keyboard = new ArrayList<>();
        int index = 0;
        for(Integer rowNumber: configuration){
            KeyboardRow row = new KeyboardRow();
            for(int i = 0; i < rowNumber; i++){
                KeyboardButton button = new KeyboardButton();
                button.setText(text.get(index));
                row.add(button);

                index++;
            }
            keyboard.add(row);
        }
        return keyboard;
    }
}
