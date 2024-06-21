package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.BoxTypeImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BoxTypeImageRepository extends JpaRepository<BoxTypeImages, Long> {



}
