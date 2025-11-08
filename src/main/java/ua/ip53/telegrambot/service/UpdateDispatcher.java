package ua.ip53.telegrambot.service;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ua.ip53.telegrambot.bot.Bot;
import ua.ip53.telegrambot.entity.Action;
import ua.ip53.telegrambot.entity.UserEntity;
import ua.ip53.telegrambot.repos.UserRepo;
import ua.ip53.telegrambot.service.handler.CallbackQueryHandler;
import ua.ip53.telegrambot.service.handler.CommandHandler;
import ua.ip53.telegrambot.service.handler.MessageHandler;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UpdateDispatcher {
    MessageHandler messageHandler;
    CommandHandler commandHandler;
    CallbackQueryHandler callbackQueryHandler;
    UserRepo userRepo;
    AccessControlService accessControlService;
    WhiteListService whiteListService;

    public BotApiMethod<?> distribute(Update update, Bot bot) {

        if (update.hasMessage()) {
            checkUser(update, update.getMessage().getChatId());

            if (!accessControlService.checkIsUserAllowed(update)) {
                return SendMessage.builder()
                        .chatId(update.getMessage().getChatId())
                        .text("Ви не студент групи ІП-53")
                        .build();
            }
            var message = update.getMessage();

            if (message.hasText()) {
                if (message.getText().charAt(0) == '/') {
                    return commandHandler.answer(message, bot);
                }
                return messageHandler.answer(message, bot);
            } else return null;

        }
        if (update.hasCallbackQuery()) {
            checkUser(update, update.getCallbackQuery().getMessage().getChatId());

            if (!accessControlService.checkIsUserAllowed(update)) {
                return SendMessage.builder()
                        .chatId(update.getCallbackQuery().getMessage().getChatId())
                        .text("Ви не студент групи ІП-53")
                        .build();

            }
            return callbackQueryHandler.answer(update.getCallbackQuery(), bot);
        }

        log.warn("Unknown update type: {}", update);
        return null;
    }

    private void checkUser(Update update, Long chatId) {
        User telegramUser = getUserFromUpdate(update);
        if (telegramUser == null || chatId == null) {
            log.warn("Could not extract user or chat ID from update.");
            return;
        }

        Long userId = telegramUser.getId();
        String username = telegramUser.getUserName();

        if (userRepo.existsByUserId(userId)) {
            Optional<UserEntity> userOpt = userRepo.findByUserId(userId);
            if (userOpt.isPresent()) {
                UserEntity existingUser = userOpt.get();
                boolean updated = false;
                if (!chatId.equals(existingUser.getChatId())) {
                    existingUser.setChatId(chatId);
                    updated = true;
                }
                if (username != null && !username.equals(existingUser.getUsername())) {
                    existingUser.setUsername(username);
                    updated = true;
                }
                if (updated) {
                    userRepo.save(existingUser);
                }
            }
            return;
        }
        boolean isInGroup = whiteListService.isUsernameInGroup(username);

        userRepo.save(UserEntity.builder()
                .userId(userId)
                .chatId(chatId)
                .username(username)
                .inOurGroup(isInGroup)
                .isAdmin(false)
                .action(Action.FREE)
                .build());
        log.info("Created new user entry for userId: {}", userId);
    }

    private User getUserFromUpdate(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom();
        }
        return null;
    }

}