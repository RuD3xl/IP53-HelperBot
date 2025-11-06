package ua.ip53.telegrambot.service.manager.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Call;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.ip53.telegrambot.bot.Bot;
import ua.ip53.telegrambot.entity.Action;
import ua.ip53.telegrambot.entity.HomeworkEntity;
import ua.ip53.telegrambot.entity.UserEntity;
import ua.ip53.telegrambot.repos.HomeworkRepo;
import ua.ip53.telegrambot.repos.UserRepo;
import ua.ip53.telegrambot.service.factory.KeyboardFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import static ua.ip53.telegrambot.data.CallbackData.*;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HomeworkCreateManager {
    HomeworkRepo homeworkRepo;
    KeyboardFactory keyboardFactory;
    UserRepo userRepo;


    public BotApiMethod<?> createHomework(CallbackQuery query, Bot bot, String subject) {

        String id = String.valueOf(homeworkRepo.save(
                HomeworkEntity.builder()
                        .subject(subject)
                        .build()
        ).getId());
        Message message = (Message) query.getMessage();
        try {
            bot.execute(
                    DeleteMessage.builder()
                            .chatId(message.getChatId())
                            .messageId(message.getMessageId())

                            .build()
            );
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        log.warn("Id1" + id);
        return SendMessage.builder()
                .chatId(query.getMessage().getChatId())
                .text("Створення домашки:")
                .replyMarkup(editHomeworkMarkup(id))
                .build();
    }

    public BotApiMethod<?> applyHomework(CallbackQuery query, Bot bot) throws TelegramApiException {
        Message message = (Message) query.getMessage();
        String id = String.valueOf(
                userRepo.findByChatId(message.getChatId())
                        .getCurrentHomeworkId()
        );
        log.warn("apply");
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("HH:mm  dd MMMM'('EEEE')'", new Locale("uk"));
        UserEntity user = userRepo.findByChatId(message.getChatId());
        user.setAction(Action.FREE);
        user.setCurrentHomeworkId(null);
        userRepo.save(user);
        return EditMessageText.builder()
                .chatId(message.getChatId())
                .messageId(message.getMessageId())
                .text("Домашнє завдання з " + homeworkRepo.findById(UUID.fromString(id)).orElseThrow().getSubject() +
                        ":\n\n Заголовок: \"" + homeworkRepo.findById(UUID.fromString(id)).orElseThrow().getText() +
                        "\"\n\n Вміст дз: \"" + homeworkRepo.findById(UUID.fromString(id)).orElseThrow().getDescription() +
                        "\n\n Термін здачі: " + homeworkRepo.findById(UUID.fromString(id)).orElseThrow().getDeadline().format(formatter)
                )
                .replyMarkup(keyboardFactory.createInlineKeyboardMarkup(
                        List.of("✅ Підтвердити", "❌ Відмінити"),
                        List.of(1, 1),
                        List.of(admin_main.name(), admin_homework.name())
                ))
                .build();
    }

    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        var user = userRepo.findByChatId(message.getChatId());
        try {
            bot.execute(
                    DeleteMessage.builder()
                            .chatId(message.getChatId())
                            .messageId(message.getMessageId() - 1)
                            .build()
            );
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

        switch (user.getAction()) {
                case SENDING_TITLE -> {
                    return setTitle(message, user);
                }
                case SENDING_DESCRIPTION -> {
                    return setDescription(message, user);
                }
                case SENDING_DEADLINE -> {
                    return setDeadline(message, user, bot);
                }
            }
        return null;
    }

    public BotApiMethod<?> editHomework(CallbackQuery query, String[] words, Bot bot) {

        switch (words[2]) {
            case "title" -> {
                return askTitle(query, words[3]);
            }
            case "dc" -> {
                return askDescription(query, words[3]);
            }
            case "dl" -> {
                return askDeadline(query, words[3]);
            }
            case "done" -> {
                log.warn("done");
                try {
                    return applyHomework(query, bot);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
            case "back" -> {
                log.warn("back");
                return backButtonMarkup(query, words[3]);
            }
        }
        return null;
    }

    private BotApiMethod<?> backButtonMarkup(CallbackQuery query, String id) {
        Message message = (Message) query.getMessage();
        log.warn("back2");
        return SendMessage.builder()
                .chatId(query.getMessage().getChatId())
                .text("Створення домашки:")
                .replyMarkup(editHomeworkMarkup(id))
                .build();
    }

    private InlineKeyboardMarkup editHomeworkMarkup(String id) {
        List<String> text = new ArrayList<>();

        var homeworkEntity = homeworkRepo.findById(UUID.fromString(id)).orElseThrow();
        if (homeworkEntity.getText() != null && !homeworkEntity.getText().isBlank()) {
            text.add("✅ Заголовок");
        } else {
            text.add("❌ Заголовок");
        }
        if (homeworkEntity.getDescription() != null && !homeworkEntity.getDescription().isBlank()) {
            text.add("✅ Опис дз");
        } else {
            text.add("❌ Опис дз");
        }
        if (homeworkEntity.getDeadline() != null) {
            text.add("✅ Час");
        } else {
            text.add("❌ Час");
        }

        text.add("\uD83D\uDD50 Готово");
        text.add("\uD83D\uDD19 Меню");
        return keyboardFactory.createInlineKeyboardMarkup(
                text,
                List.of(2, 1, 2),
                List.of(
                        admin_edit_title_.name() + id,
                        admin_edit_dc_.name() + id,
                        admin_edit_dl_.name() + id,
                        admin_hw_done_.name() + id, admin_main.name()
                )
        );

    }

    private BotApiMethod<?> askTitle(CallbackQuery query, String id) {
        UserEntity user = userRepo.findByChatId(query.getMessage().getChatId());
        Message message = (Message) query.getMessage();
        user.setAction(Action.SENDING_TITLE);
        user.setCurrentHomeworkId(UUID.fromString(id));
        userRepo.save(user);

        return EditMessageText.builder()
                .messageId(message.getMessageId())
                .chatId(message.getChatId())
                .text("Введіть заголовок домашнього завдання:")
                .replyMarkup(keyboardFactory.createInlineKeyboardMarkup(
                        List.of("\uD83D\uDD19Назад"),
                        List.of(1),
                        List.of(admin_edit_back_.name() + id)
                ))
                .build();
    }

    private BotApiMethod<?> setTitle(Message message, UserEntity user) {
        var homework = homeworkRepo.findById(user.getCurrentHomeworkId()).orElseThrow();
        String id = String.valueOf(
                userRepo.findByChatId(message.getChatId())
                        .getCurrentHomeworkId()
        );
        homework.setText(message.getText());
        homeworkRepo.save(homework);
        user.setAction(Action.FREE);
        userRepo.save(user);
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("Створення домашки:")
                .replyMarkup(editHomeworkMarkup(id))
                .build();
    }

    private BotApiMethod<?> askDescription(CallbackQuery query, String id) {
        UserEntity user = userRepo.findByChatId(query.getMessage().getChatId());
        Message message = (Message) query.getMessage();
        user.setAction(Action.SENDING_DESCRIPTION);
        user.setCurrentHomeworkId(UUID.fromString(id));
        userRepo.save(user);

        return EditMessageText.builder()
                .messageId(message.getMessageId())
                .chatId(message.getChatId())
                .text("Введіть вміст домашнього завдання:")
                .replyMarkup(keyboardFactory.createInlineKeyboardMarkup(
                        List.of("\uD83D\uDD19Назад"),
                        List.of(1),
                        List.of(admin_edit_back_.name() + id)
                ))
                .build();
    }

    private BotApiMethod<?> setDescription(Message message, UserEntity user) {
        var homework = homeworkRepo.findById(user.getCurrentHomeworkId()).orElseThrow();
        String id = String.valueOf(
                userRepo.findByChatId(message.getChatId())
                        .getCurrentHomeworkId()
        );
        homework.setDescription(message.getText());
        homeworkRepo.save(homework);
        user.setAction(Action.FREE);
        userRepo.save(user);

        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("Створення домашки:")
                .replyMarkup(editHomeworkMarkup(id))
                .build();
    }

    private BotApiMethod<?> askDeadline(CallbackQuery query, String id) {
        UserEntity user = userRepo.findByChatId(query.getMessage().getChatId());
        Message message = (Message) query.getMessage();
        user.setAction(Action.SENDING_DEADLINE);
        user.setCurrentHomeworkId(UUID.fromString(id));
        userRepo.save(user);

        return EditMessageText.builder()
                .messageId(message.getMessageId())
                .chatId(message.getChatId())
                .text("Введіть термін здачі:\n\n" +
                        "Формат вводу: \n1)dd.MM HH:mm" +
                        "\n2)dd.MM (у час виставиться 23:59)")
                .replyMarkup(keyboardFactory.createInlineKeyboardMarkup(
                        List.of("\uD83D\uDD19Назад"),
                        List.of(1),
                        List.of(admin_edit_back_.name() + id)
                ))
                .build();
    }

    private BotApiMethod<?> setDeadline(Message message, UserEntity user, Bot bot) {
        var homework = homeworkRepo.findById(user.getCurrentHomeworkId()).orElseThrow();
        String id = String.valueOf(
                userRepo.findByChatId(message.getChatId())
                        .getCurrentHomeworkId()
        );

        LocalDateTime deadline = tryParseDeadline(message.getText());
        homework.setDeadline(deadline);
        homeworkRepo.save(homework);
        user.setAction(Action.FREE);
        userRepo.save(user);

        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("Створення домашки:")
                .replyMarkup(editHomeworkMarkup(id))
                .build();
    }

    private LocalDateTime tryParseDeadline(String text) {
        int currentYear = LocalDate.now().getYear();

        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        try {
            if (text.matches("\\d{2}\\.\\d{2}\\s+\\d{2}:\\d{2}")) {
                String textWithYear = text.replaceFirst(" ", "." + currentYear + " ");
                log.debug("Trying to parse deadline as '{}' using format dd.MM.yyyy HH:mm", textWithYear);
                return LocalDateTime.parse(textWithYear, fullFormatter);
            } else if (text.matches("\\d{2}\\.\\d{2}")) {
                String textWithYear = text + "." + currentYear;
                log.debug("Trying to parse deadline as '{}' using format dd.MM.yyyy, setting time to 23:59", textWithYear);
                LocalDate date = LocalDate.parse(textWithYear, dateFormatter);
                return date.atTime(23, 59);
            } else {
                log.warn("Deadline input '{}' does not match expected formats (dd.MM HH:mm or dd.MM)", text);
                return null;
            }
        } catch (DateTimeParseException e) {
            log.error("Error parsing deadline string '{}': {}", text, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error parsing deadline string '{}'", text, e);
            return null;
        }
    }
}
