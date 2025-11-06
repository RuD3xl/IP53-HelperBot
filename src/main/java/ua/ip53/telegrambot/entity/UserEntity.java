package ua.ip53.telegrambot.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ua.ip53.telegrambot.entity.contract.AbstractEntity;

import java.util.UUID;


@Getter
@Setter
@Builder
@Entity
@Table(name = "bot_users")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends AbstractEntity {

    @Column(name = "userId", nullable = false)
    Long userId;
    @Column(name = "chat_id", nullable = false)
    Long chatId;
    @Column(name = "username", nullable = false)
    String username;
    @Column(name = "inOurGroup", nullable = false)
    Boolean inOurGroup;
    @Column(name = "is_admin", nullable = false)
    Boolean isAdmin;
    @Enumerated(EnumType.STRING)
    Action action;
    @Column(name = "current_homework_id")
    UUID currentHomeworkId;
}
