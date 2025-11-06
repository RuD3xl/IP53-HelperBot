package ua.ip53.telegrambot.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.ip53.telegrambot.entity.HomeworkEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface HomeworkRepo extends JpaRepository<HomeworkEntity, UUID> {
    Optional<HomeworkEntity> findFirstBySubjectOrderByDeadlineDesc(String subject);
    List<HomeworkEntity> findByDeadlineBetween(LocalDateTime start, LocalDateTime end);
    HomeworkEntity deleteHomeworkEntityById(UUID id);
}
