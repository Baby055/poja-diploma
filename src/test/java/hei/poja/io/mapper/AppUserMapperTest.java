package hei.poja.io.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import hei.poja.io.model.AppUser;
import hei.poja.io.model.Role;
import hei.poja.io.repository.model.JAppUser;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AppUserMapperTest {

    private final AppUserMapper mapper = new AppUserMapper();

    @Test
    void toModel_maps_all_fields() {
        UUID id = UUID.randomUUID();
        JAppUser entity =
                JAppUser.builder().id(id).email("a@b.com").passwordHash("hash").role(Role.ADMIN).build();

        AppUser model = mapper.toModel(entity);

        assertThat(model.id()).isEqualTo(id);
        assertThat(model.email()).isEqualTo("a@b.com");
        assertThat(model.passwordHash()).isEqualTo("hash");
        assertThat(model.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void toEntity_maps_all_fields() {
        UUID id = UUID.randomUUID();
        AppUser model =
                AppUser.builder().id(id).email("a@b.com").passwordHash("hash").role(Role.TEACHER).build();

        JAppUser entity = mapper.toEntity(model);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getEmail()).isEqualTo("a@b.com");
        assertThat(entity.getRole()).isEqualTo(Role.TEACHER);
    }

    @Test
    void toModel_list_maps_each_element() {
        JAppUser e1 = JAppUser.builder().id(UUID.randomUUID()).email("a@b.com").role(Role.STUDENT).build();
        JAppUser e2 = JAppUser.builder().id(UUID.randomUUID()).email("c@d.com").role(Role.STUDENT).build();

        assertThat(mapper.toModel(List.of(e1, e2))).hasSize(2);
    }
}