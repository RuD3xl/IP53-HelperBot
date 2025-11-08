package ua.ip53.telegrambot.service.manager.birthday;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ua.ip53.telegrambot.bot.Bot;
import ua.ip53.telegrambot.entity.BotSettingEntity;
import ua.ip53.telegrambot.repos.BotSettingsRepo;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BirthdayManager {
    private final Bot bot;
    private final BotSettingsRepo settingsRepo;
    private static final String BIRTHDAY_CHAT_ID_KEY = "BIRTHDAY_CHAT_ID";
    private static final String BIRTHDAY_THREAD_ID_KEY = "BIRTHDAY_THREAD_ID";


    @Value("${birthday.file.path}")
    private String filePath;

    @Scheduled(cron = "0 0 0 * * *")
    public void sendBirthday() {
        checkBirthday();
    }

    private void checkBirthday() {
        try (
                InputStream inputStream = new FileInputStream(filePath);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        ) {
            XSSFSheet sheet = workbook.getSheet("Аркуш1");
            Row headerRow = sheet.getRow(2);
            if (headerRow == null) {
                throw new RuntimeException("Header row is empty.");
            }

            int nameCellIndex = -1;
            int birthdayCellIndex = -1;
            for (int cellIndex = 0; cellIndex < headerRow.getLastCellNum(); cellIndex++) {
                String cellValue = headerRow.getCell(cellIndex).getStringCellValue();
                if ("Тег в тг".equals(cellValue)) {
                    nameCellIndex = cellIndex;
                } else if ("Дата народження".equals(cellValue)) {
                    birthdayCellIndex = cellIndex;
                }
            }
            LocalDate today = LocalDate.now();
            for (int r = 3; r <= 45; r++) {
                Row row = sheet.getRow(r);
                Cell nameCell = row.getCell(nameCellIndex);
                Cell birthdayCell = row.getCell(birthdayCellIndex);

                if (row.getCell(nameCellIndex) != null && row.getCell(birthdayCellIndex) != null) {
                    try {
                        LocalDateTime birthdayDateTime = birthdayCell.getDateCellValue().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime();
                        LocalDate birthdayDate = birthdayDateTime.toLocalDate();
                        if (birthdayDate.getMonth() == today.getMonth() && birthdayDate.getDayOfMonth() == today.getDayOfMonth()) {
                            sendBirthdayMessage(nameCell.getStringCellValue());
                        }
                    } catch (Exception e) {
                        log.error("BirthdayManager: checkBirthday2 - " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("BirthdayManager: checkBirthday1 - " + e.getMessage());
        }
    }
    private void sendBirthdayMessage(String studentName) {
        Optional<BotSettingEntity> settingOpt = settingsRepo.findById(BIRTHDAY_CHAT_ID_KEY);

        if (settingOpt.isEmpty()) {
            log.error("BirthdayManager: CHAT_ID not set in database");
            return;
        }

        Optional<BotSettingEntity> threadSettingOpt = settingsRepo.findById(BIRTHDAY_THREAD_ID_KEY);
        Integer threadId = null;
        if (threadSettingOpt.isPresent() && threadSettingOpt.get().getValue() != null) {
            try {
                threadId = Integer.parseInt(threadSettingOpt.get().getValue());
            } catch (NumberFormatException e) {
                log.warn("Invalid Birthday Thread ID in database (not a number): {}", threadSettingOpt.get().getValue());
            }
        }

        String chatId = settingOpt.get().getValue();
        String messageText = String.format(
                "З Днем Народження, %s! 🎉🎉\n\n",
                studentName
        );
        SendMessage.SendMessageBuilder messageBuilder = SendMessage.builder()
                .chatId(chatId)
                .text(messageText);
        if (threadId != null) {
            messageBuilder.messageThreadId(threadId);
        }

        SendMessage sendMessage = messageBuilder.build();
        try {
            bot.execute(sendMessage);
        } catch (Exception e) {
            log.error("BirthdayManager: Failed to send message for {}: {}", studentName, e.getMessage());
        }
    }

}
