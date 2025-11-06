package ua.ip53.telegrambot.service.manager.admin;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.ip53.telegrambot.bot.Bot;
import ua.ip53.telegrambot.entity.Action;
import ua.ip53.telegrambot.entity.HomeworkEntity;
import ua.ip53.telegrambot.entity.UserEntity;
import ua.ip53.telegrambot.repos.HomeworkRepo;
import ua.ip53.telegrambot.repos.UserRepo;
import ua.ip53.telegrambot.service.contract.AbstractManager;
import ua.ip53.telegrambot.service.contract.CommandListener;
import ua.ip53.telegrambot.service.contract.MessageListener;
import ua.ip53.telegrambot.service.contract.QueryListener;
import ua.ip53.telegrambot.service.factory.KeyboardFactory;

import java.util.List;

import static ua.ip53.telegrambot.data.CallbackData.*;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminManager extends AbstractManager implements MessageListener, CommandListener, QueryListener {
    KeyboardFactory keyboardFactory;
    HomeworkRepo homeworkRepo;
    UserRepo userRepo;
    HomeworkCreateManager homeworkCreateManager;

    @Override
    public BotApiMethod<?> mainMenu(CallbackQuery query, Bot bot) {
        Message message = (Message) query.getMessage();
        Long chatId = message.getChatId();
        UserEntity user = userRepo.findByChatId(chatId);
        user.setAction(Action.FREE);
        user.setCurrentHomeworkId(null);
        userRepo.save(user);
        return EditMessageText.builder()
                .chatId(query.getMessage().getChatId())
                .messageId(message.getMessageId())
                .chatId(chatId)
                .text("Меню адміна\n\n" +
                        "Обери функцію:")
                .replyMarkup(
                        keyboardFactory.createInlineKeyboardMarkup(
                                List.of("Д/з"),
                                List.of(1),
                                List.of(admin_homework.name())
                        )
                )
                .build();
    }

    @Override
    public BotApiMethod<?> mainMenu(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        Long chatId = message.getChatId();
        return SendMessage.builder()
                .chatId(chatId)
                .text("Меню адміна\n\n" +
                        "Обери функцію:")
                .replyMarkup(
                        keyboardFactory.createInlineKeyboardMarkup(
                                List.of("Д/з"),
                                List.of(1),
                                List.of(admin_homework.name())
                        )
                )
                .build();
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {

        return null;
    }

    @Override
    public BotApiMethod<?> answerQuery(CallbackQuery query, String[] words, Bot bot) {
        switch (words.length) {
            case 2 -> {
                switch (words[1]) {
                    case "homework" -> {
                        return showSubjects(query, bot);
                    }
                    case "main" -> {
                        return mainMenu(query, bot);
                    }
                }
            }

            case 3 -> {
                switch (words[1]) {
                    case "new" -> {
                        return homeworkCreateManager.createHomework(query, bot, words[2]);
                    }
                }

            }
            case 4 -> {

                log.warn("admin manager 5");
                return homeworkCreateManager.editHomework(query, words, bot);
            }


        }

        return null;
    }

    private BotApiMethod<?> showSubjects(CallbackQuery query, Bot bot) {
        Message message = (Message) query.getMessage();

        return EditMessageText.builder()
                .chatId(message.getChatId())
                .messageId(message.getMessageId())
                .text("Створення нового дз:\n\n" +
                        "Оберіть предмет, домашку з якого ви хочете створити:")
                .replyMarkup(
                        keyboardFactory.createInlineKeyboardMarkup(
                                List.of("Мат", "Лааг", "КДМ", "Укр", "Англ", "Меню"),
                                List.of(3, 2, 1),
                                List.of(admin_new_mat.name(), admin_new_LAAG.name(), admin_new_kdm.name(), admin_new_ykr.name(), admin_new_eng.name(), admin_main.name())
                        )
                )
                .build();
    }
}
