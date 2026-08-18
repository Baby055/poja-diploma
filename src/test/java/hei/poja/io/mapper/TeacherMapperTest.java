package hei.poja.io.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import hei.poja.io.model.Role;
import hei.poja.io.model.Teacher;
import hei.poja.io.repository.model.JAppUser;
import hei.poja.io.repository.model.JTeacher;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TeacherMapperTest {

  private final TeacherMapper mapper = new TeacherMapper(new AppUserMapper());

  @Test
  void toModel_maps_teacher_and_nested_user() {
    UUID id = UUID.randomUUID();
    JAppUser user = JAppUser.builder().id(id).email("prof@hei.school").role(Role.TEACHER).build();
    JTeacher entity =
        JTeacher.builder().id(id).user(user).firstName("Ada").lastName("Lovelace").build();

    Teacher model = mapper.toModel(entity);

    assertThat(model.firstName()).isEqualTo("Ada");
    assertThat(model.user().email()).isEqualTo("prof@hei.school");
  }

  @Test
  void toEntity_maps_teacher_and_nested_user() {
    UUID id = UUID.randomUUID();
    hei.poja.io.model.AppUser user =
        hei.poja.io.model.AppUser.builder()
            .id(id)
            .email("prof@hei.school")
            .role(Role.TEACHER)
            .build();
    Teacher model =
        Teacher.builder().id(id).user(user).firstName("Alan").lastName("Turing").build();

    JTeacher entity = mapper.toEntity(model);

    assertThat(entity.getUser().getEmail()).isEqualTo("prof@hei.school");
  }
}
