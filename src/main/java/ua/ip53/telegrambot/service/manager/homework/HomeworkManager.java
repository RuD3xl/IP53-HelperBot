package ua.ip53.telegrambot.service.manager.homework;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ua.ip53.telegrambot.bot.Bot;
import ua.ip53.telegrambot.entity.HomeworkEntity;
import ua.ip53.telegrambot.repos.HomeworkRepo;
import ua.ip53.telegrambot.service.contract.AbstractManager;
import ua.ip53.telegrambot.service.contract.CommandListener;
import ua.ip53.telegrambot.service.contract.MessageListener;
import ua.ip53.telegrambot.service.contract.QueryListener;
import ua.ip53.telegrambot.service.factory.KeyboardFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static ua.ip53.telegrambot.data.CallbackData.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HomeworkManager extends AbstractManager implements MessageListener, CommandListener, QueryListener {
    KeyboardFactory keyboardFactory;
    HomeworkRepo homeworkRepo;

    @Override
    public BotApiMethod<?> mainMenu(CallbackQuery query, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> mainMenu(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }


    @Override
    public BotApiMethod<?> answerQuery(CallbackQuery query, String[] words, Bot bot) {
        switch (words.length) {
            case 2 -> {
                switch (words[1]) {
                    case "main" -> {
                        return showSubjects(query, bot);
                    }
                    case "mat" -> {
                        return getSubjectHomework(words[1], "матану", query);
                    }
                    case "LAAG" -> {
                        return getSubjectHomework(words[1], "лаагу", query);
                    }
                    case "kdm" -> {
                        log.warn("kdm");
                        return getSubjectHomework(words[1], "кдму", query);
                    }
                    case "eng" -> {
                        return getSubjectHomework(words[1], "англ мові", query);
                    }
                    case "ykr" -> {
                        return getSubjectHomework(words[1], "укр мові", query);
                    }
                    case "new" -> {

                    }
                }
            }
        }
        return null;
    }

    private BotApiMethod<?> getSubjectHomework(String subjectKey, String subjectDisplayName, CallbackQuery query) {
        Message message = (Message) query.getMessage();
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();
        String responseText;
        Optional<HomeworkEntity> latestHomeworkOpt = homeworkRepo.findFirstBySubjectOrderByDeadlineDesc(subjectKey);

        if (latestHomeworkOpt.isPresent()) {
            HomeworkEntity homeworkEntity = latestHomeworkOpt.get();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm, dd MMMM (EEEE)", new Locale("uk"));

            String escapedText = escapeMarkdownV2(homeworkEntity.getText());
            String escapedDescription = escapeMarkdownV2(homeworkEntity.getDescription());
            String deadlineFormatted = (homeworkEntity.getDeadline() != null)
                    ? escapeMarkdownV2(homeworkEntity.getDeadline().format(formatter)) + (homeworkEntity.getDeadline().isBefore(LocalDateTime.now()) ? " ‼️Минуле дз" : "")
                    : "_не вказано_";

            responseText = String.format(
                    "*Домашка з %s:*\n\n" +
                            "📌 *Тема:* %s\n" +
                            "📝 *Завдання:* %s\n\n" +
                            "⏰ *Дедлайн:* %s",
                    subjectDisplayName,
                    escapedText.isEmpty() ? "_не вказано_" : escapedText,
                    escapedDescription.isEmpty() ? "_не вказано_" : escapedDescription,
                    "*" + deadlineFormatted + "*"
            );
        } else {
            responseText = String.format("🤷 Для предмету '%s' ще немає домашнього завдання.", subjectDisplayName);
        }

        return EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(responseText)
                .parseMode(ParseMode.MARKDOWNV2)
                .replyMarkup(
                        keyboardFactory.createInlineKeyboardMarkup(
                                List.of("⬅️ Назад до предметів"),
                                List.of(1),
                                List.of(homework_main.name())
                        )
                )
                .build();
    }

    private BotApiMethod<?> showSubjects(CallbackQuery query, Bot bot) {
        Message message = (Message) query.getMessage();

        return EditMessageText.builder()
                .chatId(query.getMessage().getChatId())
                .messageId(message.getMessageId())
                .text("Тут знаходиться домашка з наших предметів\n" +
                        "P.s. Якщо кнопка не працює - домашки нема в БД (Можете написати мені - @backward_jitter)\n\n" +
                        "Оберіть предмет, домашку з якого ви хочете отримати:")
                .replyMarkup(
                        keyboardFactory.createInlineKeyboardMarkup(
                                List.of("Мат", "Лааг", "КДМ", "Укр", "Англ", "Меню"),
                                List.of(3, 2, 1),
                                List.of(homework_mat.name(), homework_LAAG.name(), homework_kdm.name(), homework_ykr.name(), homework_eng.name(), main.name())
                        )
                )
                .build();


    }

    private String escapeMarkdownV2(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }

}

