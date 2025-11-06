package ua.ip53.telegrambot.entity.contract;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Getter
@MappedSuperclass
public abstract class AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected UUID id;

}
