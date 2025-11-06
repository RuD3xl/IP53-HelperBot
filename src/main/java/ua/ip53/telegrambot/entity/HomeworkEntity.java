package ua.ip53.telegrambot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import ua.ip53.telegrambot.entity.contract.AbstractEntity;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Builder
@Slf4j
@Table(name = "homework")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class HomeworkEntity extends AbstractEntity {

    @Column(name = "title",length = 2048)
    String text;

    @Column(name = "description", length = 2048)
    String description;

    @Column(name = "subject", length = 2048)
    String subject;

    @Column(name = "deadline",length = 2048)
    LocalDateTime deadline;
}
