package kz.wonder.wonderuserrepository.entities;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@Entity
@EqualsAndHashCode(callSuper = false)
@Table(schema = schemaName, name = "telegram_account")
public class TelegramAccount  extends AbstractEntity<Long>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tg_username", unique = true, nullable = false)
    private String tgUsername;
    @Column(name = "tg_chat_id", unique = true, nullable = false)
    private String tgChatId;
    private boolean verified;
    private boolean enabled;
    @OneToOne
    @JoinColumn(columnDefinition = "integer", name = "userId")
    private User user;
}
