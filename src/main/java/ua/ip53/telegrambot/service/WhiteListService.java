package ua.ip53.telegrambot.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WhiteListService {
    @Value("${bot.allowed-usernames:}")
    private String[] allowedUsernamesArray;
    private final Set<String> allowedUsernames = new HashSet<>();

    @PostConstruct
    public void init() {
        allowedUsernames.addAll(
                Arrays.stream(allowedUsernamesArray)
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet())
        );
        log.info("Whitelist initialized with {} usernames.", allowedUsernames.size());
    }

    public boolean isUsernameInGroup(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        return allowedUsernames.contains(username.toLowerCase());
    }
}

