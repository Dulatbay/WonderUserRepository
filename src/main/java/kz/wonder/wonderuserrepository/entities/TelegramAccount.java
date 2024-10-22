package kz.wonder.wonderuserrepository.entities;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper=false)
@Entity
@Data
@Table(schema = schemaName, name = "telegram_account")
public class TelegramAccount extends AbstractEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tg_username", unique = true, nullable = false)
    private String tgUsername;

    @Column(name = "tg_chat_id", unique = true, nullable = false)
    private String tgChatId;

    @Column(name = "verified", nullable = false)
    private boolean verified;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    private WonderUser wonderUser;
}