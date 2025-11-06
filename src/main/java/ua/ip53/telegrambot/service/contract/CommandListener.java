package ua.ip53.telegrambot.service.contract;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import ua.ip53.telegrambot.bot.Bot;

public interface CommandListener {
    BotApiMethod<?> answerCommand(Message message, Bot bot);
}
