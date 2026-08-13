package hei.poja.io.mapper;

import hei.poja.io.model.AppUser;
import hei.poja.io.repository.model.JAppUser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AppUserMapper {
    public List<AppUser> toModel(List<JAppUser> jAppUsers) {
        return jAppUsers.stream().map(this::toModel).toList();
    }

    public AppUser toModel(JAppUser jAppUser) {
        return AppUser.builder()
                .id(jAppUser.getId())
                .email(jAppUser.getEmail())
                .passwordHash(jAppUser.getPasswordHash())
                .role(jAppUser.getRole())
                .build();
    }

    public List<JAppUser> toEntity(List<AppUser> appUsers) {
        return appUsers.stream().map(this::toEntity).toList();
    }

    public JAppUser toEntity(AppUser appUser) {
        return JAppUser.builder()
                .id(appUser.id())
                .email(appUser.email())
                .passwordHash(appUser.passwordHash())
                .role(appUser.role())
                .build();
    }
}
