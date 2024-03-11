package kz.wonder.wonderuserrepository.entities;


import jakarta.persistence.*;

@Entity
@Table(schema = "schema_wonder", name = "telegram_account")
public class TelegramAccount {
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
