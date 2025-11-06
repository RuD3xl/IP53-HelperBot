package ua.ip53.telegrambot.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.ip53.telegrambot.entity.UserEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<UserEntity, UUID> {
    UserEntity findByChatId(Long chatId);
    Optional<UserEntity> findByUsernameAndChatId(String username, Long chatId);

    boolean existsByUserId(Long userId);
    Optional<UserEntity> findByUserId(Long userId);
}
