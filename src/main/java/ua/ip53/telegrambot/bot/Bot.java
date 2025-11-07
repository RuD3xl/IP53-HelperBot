package ua.ip53.telegrambot.bot;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot; // ❗️ 1. ЗМІНЕНО
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.ip53.telegrambot.configuration.TelegramProperties;
import ua.ip53.telegrambot.service.UpdateDispatcher;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class Bot extends TelegramLongPollingBot {

    TelegramProperties telegramProperties;
    UpdateDispatcher updateDispatcher;

    public Bot(TelegramProperties telegramProperties, UpdateDispatcher updateDispatcher) {
        super(telegramProperties.getToken());
        this.telegramProperties = telegramProperties;
        this.updateDispatcher = updateDispatcher;
        try {
            this.setBotCommands();
        } catch (TelegramApiException e) {
            log.error("Failed to set bot commands: {}", e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()){
            log.info("Received message: " + update.getMessage().getText());
        } else if (update.hasCallbackQuery()){
            log.info("Received callback query: " + update.getCallbackQuery().getData());
        }

        BotApiMethod<?> response = updateDispatcher.distribute(update, this);

        if (response != null) {
            try {
                this.execute(response);
            } catch (TelegramApiException e) {
                log.error("Error executing response: {}", e.getMessage(), e);
            }
        }
    }

    private void setBotCommands() throws TelegramApiException {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("start@ip53bot", "Запустити бота"));
        SetMyCommands setMyCommands = new SetMyCommands();
        setMyCommands.setCommands(commands);
        this.execute(setMyCommands);
        log.info("Bot commands menu updated.");
    }

    @Override
    public String getBotUsername() {
        return telegramProperties.getName();
    }
}