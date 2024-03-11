package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.TelegramAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramAccountRepository extends JpaRepository<TelegramAccount, Long> { }
