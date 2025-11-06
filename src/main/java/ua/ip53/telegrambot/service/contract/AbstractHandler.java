package ua.ip53.telegrambot.service.contract;

import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.ip53.telegrambot.bot.Bot;

public abstract class AbstractHandler {
    public abstract BotApiMethod<?> answer(BotApiObject object, Bot bot);
}
