package ua.ip53.telegrambot.service.manager.homework;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ua.ip53.telegrambot.bot.Bot;
import ua.ip53.telegrambot.entity.BotSettingEntity;
import ua.ip53.telegrambot.entity.HomeworkEntity;
import ua.ip53.telegrambot.repos.BotSettingsRepo;
import ua.ip53.telegrambot.repos.HomeworkRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class HomeworkReminderService {
    private final Bot bot;
    private final HomeworkRepo homeworkRepo;
    private final BotSettingsRepo settingsRepo;
    private static final String REMINDER_CHAT_ID_KEY = "REMINDER_CHAT_ID";

    @Scheduled(cron = "0 0 0 * * *")
    private void homeworkReminder() {
        Optional<BotSettingEntity> settingOpt = settingsRepo.findById(REMINDER_CHAT_ID_KEY);

        if (settingOpt.isEmpty()) {
            log.error("HWReminder: CHAT_ID not set in database");
            return;
        }
        String chatId = settingOpt.get().getValue();
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime startOfTomorrow = tomorrow.atStartOfDay();
        LocalDateTime startOfDayAfterTomorrow = tomorrow.plusDays(1).atStartOfDay();

        List<HomeworkEntity> homeworkDueTomorrow = homeworkRepo.findByDeadlineBetween(
                startOfTomorrow,
                startOfDayAfterTomorrow
        );

        if (homeworkDueTomorrow.isEmpty()) {
            log.info("No homework due tomorrow. No reminders sent.");
            return;
        }

        log.info("Found {} homework items due tomorrow. Sending reminders...", homeworkDueTomorrow.size());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm EEEE", new Locale("uk"));
        StringBuilder reminderText = new StringBuilder("‼️*Нагадування про дедлайни на завтра\\!*\n\n");

        for (HomeworkEntity homework : homeworkDueTomorrow) {
            reminderText.append(String.format(
                    "*%s* \\(%s\\)\n" +
                            "*Задвання:* %s\n\n" +
                            "*Дедлайн\\:* %s",
                    escapeMarkdownV2(subjectParsing(homework.getSubject())),
                    escapeMarkdownV2(homework.getText()),
                    escapeMarkdownV2(homework.getDescription()),
                    escapeMarkdownV2(homework.getDeadline().format(formatter))
            ));
        }

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(reminderText.toString())
                .parseMode(ParseMode.MARKDOWNV2)
                .build();
        try {
            bot.execute(message);
        } catch (Exception e) {
            log.error("Failed to send homework reminder: {}", e.getMessage(), e);
        }
    }
    private String subjectParsing(String subject){
        switch (subject){
            case "mat" -> {
                return "Мат. Аналіз";
            }
            case "LAAG" -> {
                return "Лааг";
            }
            case "ykr" -> {
                return "Укр. мова";
            }
            case "eng" -> {
                return "Англ. мова";
            }
            case "kdm" -> {
                return "Кдм";
            }
        }
        return subject;
    }
    private String escapeMarkdownV2(String text) {
        if (text.isEmpty()) {
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
