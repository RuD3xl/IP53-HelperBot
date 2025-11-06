package ua.ip53.telegrambot.service.handler;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ua.ip53.telegrambot.bot.Bot;
import ua.ip53.telegrambot.service.contract.AbstractHandler;
import ua.ip53.telegrambot.service.manager.admin.AdminManager;
import ua.ip53.telegrambot.service.manager.homework.HomeworkManager;
import ua.ip53.telegrambot.service.manager.MainManager;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CallbackQueryHandler extends AbstractHandler {
    HomeworkManager homeworkManager;
    MainManager mainManager;
    AdminManager adminManager;

    @Override
    public BotApiMethod<?> answer(BotApiObject object, Bot bot)  {
        var callbackQuery = (CallbackQuery) object;
        String[] words = callbackQuery.getData().split("_");
        switch (words[0]) {
            case "homework" -> {
                return homeworkManager.answerQuery(callbackQuery, words, bot);
            }
            case "admin" -> {
                return adminManager.answerQuery(callbackQuery, words, bot);
            }
            case "main" -> {
                return mainManager.answerQuery(callbackQuery, words, bot);
            }

        }
        throw new UnsupportedOperationException();
    }
}
