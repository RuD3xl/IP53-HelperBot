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

    @Override
    public BotApiMethod<?> answer(BotApiObject object, Bot bot) {
        var message = (Message) object;
        if (message.getChat().isUserChat()) {
            if ("/start".equals(message.getText())) {
                return mainManager.answerCommand(message, bot);
            } else if ("/admin".equals(message.getText())) {
                if (accessControlService.isAdmin(message)) {
                    return adminManager.answerCommand(message, bot);
                }
                return null;
            }
        }
        else{
            try {
                String botName = bot.getMe().getUserName();
                log.warn("botName: " + botName);
                if(message.getText().contains(botName)){
                    String[] command = message.getText().split("@");
                    switch (command[0]){
                        case "/setdn":
                            if (accessControlService.isAdmin(message)) {
                                String newChatId = message.getChatId().toString();
                                BotSettingEntity setting = new BotSettingEntity(BIRTHDAY_CHAT_ID_KEY, newChatId);
                                settingsRepo.save(setting);
                                log.warn("Birthday Chat ID updated to: {}", newChatId);
                            }
                            break;
                        case "/start":
                            return mainManager.answerCommand(message, bot);
                        case "/admin":
                            if (accessControlService.isAdmin(message)) {
                                return adminManager.answerCommand(message, bot);
                            }
                        case "/setrmd":
                            if (accessControlService.isAdmin(message)) {
                                String newChatId = message.getChatId().toString();
                                BotSettingEntity setting = new BotSettingEntity(REMINDER_CHAT_ID_KEY, newChatId);
                                settingsRepo.save(setting);
                                log.warn("Reminder Chat ID updated to: {}", newChatId);
                            }
                        default:
                            break;
                    }
//                    if ("/start".equals(command[0])) {
//                        return mainManager.answerCommand(message, bot);
//                    } else if ("/admin".equals(command[0])) {
//                        if (accessControlService.isAdmin(message)) {
//                            return adminManager.answerCommand(message, bot);
//                        }
//                        return null;
//                    } else if (message.getText().startsWith("/setdn")) {
//                        if (accessControlService.isAdmin(message)) {
//                            String newChatId = message.getChatId().toString();
//                            birthdaySettings.setBirthdayChatId(newChatId);
//                            log.info("Birthday Chat ID updated to: {}", newChatId);
//                        }
//                    }
                }
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
            return null;
    }
}
