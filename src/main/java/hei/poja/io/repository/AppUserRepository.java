package hei.poja.io.repository;

import hei.poja.io.model.AppUser;
import hei.poja.io.repository.model.JAppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<JAppUser> findByEmail(String email);
}
