package hei.poja.io;

import hei.poja.io.model.Role;
import hei.poja.io.repository.AppUserRepository;
import hei.poja.io.repository.model.JAppUser;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Slf4j
public class BootstrapConf implements CommandLineRunner {

  private final AppUserRepository appUserRepository;
  private final PasswordEncoder passwordEncoder;
  private final String bootstrapEmail;
  private final String bootstrapPassword;

  public BootstrapConf(
      AppUserRepository appUserRepository,
      PasswordEncoder passwordEncoder,
      @Value("${bootstrap.admin.email:admin@hei.school}") String bootstrapEmail,
      @Value("${bootstrap.admin.password:changeme}") String bootstrapPassword) {
    this.appUserRepository = appUserRepository;
    this.passwordEncoder = passwordEncoder;
    this.bootstrapEmail = bootstrapEmail;
    this.bootstrapPassword = bootstrapPassword;
  }

  @Override
  public void run(String... args) {
    if (appUserRepository.existsByRole(Role.ADMIN)) {
      return;
    }
    appUserRepository.save(
        JAppUser.builder()
            .id(UUID.randomUUID())
            .email(bootstrapEmail)
            .passwordHash(passwordEncoder.encode(bootstrapPassword))
            .role(Role.ADMIN)
            .build());
    log.warn("Bootstrap admin cree : {} (pensez a changer le mot de passe)", bootstrapEmail);
  }
}
