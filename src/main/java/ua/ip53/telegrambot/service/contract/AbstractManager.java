package ua.ip53.telegrambot.service.contract;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ua.ip53.telegrambot.bot.Bot;
import ua.ip53.telegrambot.service.handler.CallbackQueryHandler;

public abstract class AbstractManager {

    public abstract BotApiMethod<?> mainMenu(CallbackQuery query, Bot bot);
    public abstract BotApiMethod<?> mainMenu(Message message, Bot bot);

}
