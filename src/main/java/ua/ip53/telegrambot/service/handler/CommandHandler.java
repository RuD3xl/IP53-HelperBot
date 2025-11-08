package ua.ip53.telegrambot.service.handler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.ip53.telegrambot.bot.Bot;
import ua.ip53.telegrambot.entity.BotSettingEntity;
import ua.ip53.telegrambot.repos.BotSettingsRepo;
import ua.ip53.telegrambot.service.AccessControlService;
import ua.ip53.telegrambot.service.manager.admin.AdminManager;
import ua.ip53.telegrambot.service.manager.MainManager;
import ua.ip53.telegrambot.service.contract.AbstractHandler;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommandHandler extends AbstractHandler {
    MainManager mainManager;
    AdminManager adminManager;
    AccessControlService accessControlService;
    BotSettingsRepo settingsRepo;

    private static final String BIRTHDAY_CHAT_ID_KEY = "BIRTHDAY_CHAT_ID";
    private static final String REMINDER_CHAT_ID_KEY = "REMINDER_CHAT_ID";
    private static final String BIRTHDAY_THREAD_ID_KEY = "BIRTHDAY_THREAD_ID";
    private static final String REMINDER_THREAD_ID_KEY = "REMINDER_THREAD_ID";

    @Override
    public BotApiMethod<?> answer(BotApiObject object, Bot bot) {

        var message = (Message) object;
        if (message == null || !message.hasText()) {
            return null;
        }
        Integer threadId = message.getMessageThreadId();
        String fullCommand = message.getText();
        String cleanCommand;
        String botUsername;

        try {
            botUsername = bot.getMe().getUserName();
        } catch (TelegramApiException e) {
            log.error("Failed to get bot username: {}", e.getMessage(), e);
            return null;
        }

        if (message.getChat().isUserChat()) {
            cleanCommand = fullCommand;
        } else {
            if (fullCommand.contains("@")) {
                String[] parts = fullCommand.split("@");
                String targetBot = parts[1];
                if (!targetBot.equalsIgnoreCase(botUsername)) {
                    return null;
                }
                cleanCommand = parts[0];
            } else {

                return null;
            }
        }

        return switch (cleanCommand) {
            case "/start" -> mainManager.answerCommand(message, bot);
            case "/admin" -> {
                if (accessControlService.isAdmin(message)) {
                    yield adminManager.answerCommand(message, bot);
                }
                yield null;
            }
            case "/setrmd" -> {
                if (accessControlService.isAdmin(message)) {
                    String newChatId = message.getChatId().toString();
                    settingsRepo.save(new BotSettingEntity(REMINDER_CHAT_ID_KEY, newChatId));
                    String threadIdString = (threadId != null) ? threadId.toString() : null;
                    settingsRepo.save(new BotSettingEntity(REMINDER_THREAD_ID_KEY, threadIdString));
                    log.warn("Reminder Chat ID updated to: {}", newChatId);
                }
                yield null;
            }
            case "/setdn" -> {
                if (accessControlService.isAdmin(message)) {
                    String newChatId = message.getChatId().toString();
                    settingsRepo.save(new BotSettingEntity(BIRTHDAY_CHAT_ID_KEY, newChatId));
                    String threadIdString = (threadId != null) ? threadId.toString() : null;
                    settingsRepo.save(new BotSettingEntity(BIRTHDAY_THREAD_ID_KEY, threadIdString));
                    log.warn("Birthday Chat ID updated to: {}", newChatId);
                }
                yield null;
            }
            default -> {
                log.warn("Unknown command received: {}", fullCommand);
                yield null;
            }
        };
    }
}
