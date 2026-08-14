package hei.poja.io.security;

import hei.poja.io.repository.model.JAppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.UUID;

public class AppUserDetails extends User {
    private final UUID id;

    public AppUserDetails(JAppUser appUser) {
        super(appUser.getEmail(), appUser.getPasswordHash(), authorities(appUser));
        this.id = appUser.getId();
    }

    private static List<GrantedAuthority> authorities(JAppUser appUser) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name()));
    }

    public UUID getId() {
        return id;
    }
}
