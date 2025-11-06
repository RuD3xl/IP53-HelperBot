package ua.ip53.telegrambot.service.handler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import ua.ip53.telegrambot.bot.Bot;
import ua.ip53.telegrambot.entity.UserEntity;
import ua.ip53.telegrambot.repos.UserRepo;
import ua.ip53.telegrambot.service.manager.MainManager;
import ua.ip53.telegrambot.service.contract.AbstractHandler;
import ua.ip53.telegrambot.service.manager.admin.HomeworkCreateManager;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageHandler extends AbstractHandler {
    UserRepo userRepo;
    MainManager mainManager;
    HomeworkCreateManager homeworkCreateManager;

    @Override
    public BotApiMethod<?> answer(BotApiObject object, Bot bot) {
        var message = (Message) object;
        Optional<UserEntity> userOpt = userRepo.findByUserId(message.getFrom().getId());
        if (message.getChat().isUserChat()) {
            if (userOpt.isPresent()) {
                UserEntity user = userOpt.get();
                log.warn("Message handler - User^ " + user);
                switch (user.getAction()) {
                    case FREE -> {
                        return mainManager.answerMessage(message, bot);
                    }
                    case SENDING_DEADLINE, SENDING_DESCRIPTION, SENDING_TITLE -> {
                        log.warn("Message handler - message" + message);
                        return homeworkCreateManager.answerMessage(message, bot);
                    }
                }
            }
        }
        return null;
    }

}
