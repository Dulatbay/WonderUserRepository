package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> { }
