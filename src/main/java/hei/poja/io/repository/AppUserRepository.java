package hei.poja.io.repository;

import hei.poja.io.model.AppUser;
import hei.poja.io.repository.model.JAppUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends JpaRepository<JAppUser, UUID> {
  Optional<JAppUser> findByEmail(String email);
}
