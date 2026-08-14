package hei.poja.io.security;

import hei.poja.io.repository.AppUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
  private final AppUserRepository appUserRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    var appUser =
        appUserRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Unknown user: " + email));
    return new AppUserDetails(appUser);
  }
}
