package ua.ip53.telegrambot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ua.ip53.telegrambot.entity.UserEntity;
import ua.ip53.telegrambot.repos.UserRepo;

import java.util.Optional;

@Slf4j
@Service
public class AccessControlService {

    private final UserRepo userRepo;

    @Autowired
    public AccessControlService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public boolean checkIsUserAllowed(Update update) {
        long chatId = 0;
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }
        if (chatId == 0) {
            return false;
        }
        String username = getUsernameFromUpdate(update);
        if (username == null || username.isEmpty()) {
            return false;
        }

        Optional<UserEntity> user = userRepo.findByUsernameAndChatId(username, chatId);
        return user.map(UserEntity::getInOurGroup)
                .orElse(false);
    }

    public boolean isAdmin(Message message) {
        String username = message.getFrom().getUserName();
        Long chatId = message.getChatId();
        if (username == null || username.isEmpty()) {
            return false;
        }
        log.warn("isAdmin "+message.getText());
        Optional<UserEntity> user = userRepo.findByUsernameAndChatId(username, chatId);
        return user.map(UserEntity::getIsAdmin)
                .orElse(false);
    }

    private String getUsernameFromUpdate(Update update) {
        User telegramUser = null;
        if (update.hasMessage()) {
            telegramUser = update.getMessage().getFrom();
        } else if (update.hasCallbackQuery()) {
            telegramUser = update.getCallbackQuery().getFrom();
        }
        return (telegramUser != null) ? telegramUser.getUserName() : null;
    }
}