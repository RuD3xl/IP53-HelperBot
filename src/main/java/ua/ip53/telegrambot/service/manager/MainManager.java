package ua.ip53.telegrambot.service.manager;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.ip53.telegrambot.bot.Bot;
import ua.ip53.telegrambot.repos.UserRepo;
import ua.ip53.telegrambot.service.contract.AbstractManager;
import ua.ip53.telegrambot.service.contract.CommandListener;
import ua.ip53.telegrambot.service.contract.MessageListener;
import ua.ip53.telegrambot.service.contract.QueryListener;
import ua.ip53.telegrambot.service.factory.KeyboardFactory;

import java.util.List;

import static ua.ip53.telegrambot.data.CallbackData.*;

@Service
public class MainManager extends AbstractManager implements QueryListener, CommandListener, MessageListener {
    @Value("${links.google-drive}")
    String googleDriveLink;

    @Value("${links.queue}")
    String queueLink;

    @Value("${links.schedule}")
    String scheduleLink;


    @Autowired
    public MainManager(KeyboardFactory keyboardFactory) {
        this.keyboardFactory = keyboardFactory;
    }

    private String menuText =
            "✨*Привіт\\! Я телеграм\\-бот нашої групи*✨\n" +
                    "Тут ти зможеш знайти:\n" +
                    "*📚 Останнє домашнє завдання* " + "з майже усіх предметів\n" +
                    "*🔗 Силку на гугл диск\\, чергу* та *розклад*\n\n" +
                    "P\\.s\\. У майбутньому планується додати більше функціоналу\\, тому я сподіваюсь\\, " +
                    "що цей бот стане вам у пригоді \\:\\)\n\n" +
                    "Оберіть потрібну функцію\\:";

    KeyboardFactory keyboardFactory;

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
        return greetings(message, bot);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return invalidText(message.getChatId());
    }

    @Override
    public BotApiMethod<?> answerQuery(CallbackQuery query, String[] words, Bot bot) {
        switch (words.length) {
            case 2 -> {
                return linksMenu(query.getMessage().getChatId(), query);
            }
            case 1 -> {
                return menuQuery(query, bot);
            }
        }
        return null;
    }

    private BotApiMethod<?> greetings(Message message, Bot bot) {
        Long chatId = message.getChatId();
        if (message.getChat().isUserChat()) {
            try {
                bot.execute(SendMessage.builder()
                        .chatId(chatId)
                        .text("Меню:")
                        .replyMarkup(keyboardFactory.createReplyKeyboard(
                                List.of("/start"),
                                List.of(1)

                        ))
                        .build());
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        return SendMessage.builder()
                .chatId(chatId)
                .text(menuText)
                .parseMode(ParseMode.MARKDOWNV2)
                .replyMarkup(
                        keyboardFactory.createInlineKeyboardMarkup(
                                List.of("Д/з", "Посилання"),
                                List.of(2),
                                List.of(homework_main.name(), main_links.name())
                        )
                )
                .build();
    }

    private BotApiMethod<?> menuQuery(CallbackQuery query, Bot bot) {
        Message message = (Message) query.getMessage();
        return EditMessageText.builder()
                .chatId(query.getMessage().getChatId())
                .text(menuText)
                .parseMode(ParseMode.MARKDOWNV2)
                .messageId(message.getMessageId())
                .replyMarkup(
                        keyboardFactory.createInlineKeyboardMarkup(
                                List.of("Д/з", "Посилання"),
                                List.of(2),
                                List.of(homework_main.name(), main_links.name())
                        )
                )
                .build();
    }

    private BotApiMethod<?> invalidText(Long chatId) {
        return SendMessage.builder()
                .chatId(chatId)
                .text("Я не зміг розпізнати вашу команду")
                .build();
    }

    public BotApiMethod<?> linksMenu(Long chatId, CallbackQuery query) {
        Message message = (Message) query.getMessage();
        return EditMessageText.builder()
                .chatId(chatId)
                .messageId(message.getMessageId())
                .text("*Тут представлені усі корисні посилання\\.*" +
                        "\n\nТакож нагадую, на гугл диску таблиці з викладачами і з інформацією нашого потоку\\(ПІБ, тг\\), тому якщо вам потрібно когось знайти \\- прошу туди \\:\\)")
                .parseMode(ParseMode.MARKDOWNV2)
                .replyMarkup(
                        keyboardFactory.createInlineKeyboardMarkup(
                                List.of("Гугл диск", "Черга", "Розклад", "Меню"),
                                List.of(2, 2),
                                List.of(googleDriveLink,
                                        queueLink,
                                        scheduleLink, main.name()
                                )
                        )
                )
                .build();
    }


}

