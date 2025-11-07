package ua.ip53.telegrambot.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.ip53.telegrambot.entity.BotSettingEntity;

@Repository
public interface BotSettingsRepo extends JpaRepository<BotSettingEntity, String> {
}