package ua.ip53.telegrambot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "bot_settings")
@AllArgsConstructor
@NoArgsConstructor
public class BotSettingEntity {

    @Id
    @Column(name = "setting_key")
    private String key;

    @Column(name = "setting_value")
    private String value;
}
