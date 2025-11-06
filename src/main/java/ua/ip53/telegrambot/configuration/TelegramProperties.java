package ua.ip53.telegrambot.configuration;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bot")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TelegramProperties {
    String name;
    String token;
    String url;
}
