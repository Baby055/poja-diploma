package hei.poja.io.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import hei.poja.io.model.Role;
import hei.poja.io.model.Student;
import hei.poja.io.model.Track;
import hei.poja.io.repository.model.JAppUser;
import hei.poja.io.repository.model.JStudent;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StudentMapperTest {

    private final StudentMapper mapper = new StudentMapper(new AppUserMapper());

    @Test
    void toModel_maps_student_and_nested_user() {
        UUID id = UUID.randomUUID();
        JAppUser user = JAppUser.builder().id(id).email("etu@hei.school").role(Role.STUDENT).build();
        JStudent entity =
                JStudent.builder().id(id).user(user).firstName("Grace").lastName("Hopper").track(Track.EL).enrollmentYear(2024).build();

        Student model = mapper.toModel(entity);

        assertThat(model.track()).isEqualTo(Track.EL);
        assertThat(model.enrollmentYear()).isEqualTo(2024);
        assertThat(model.user().email()).isEqualTo("etu@hei.school");
    }

    @Test
    void toEntity_maps_student_and_nested_user() {
        UUID id = UUID.randomUUID();
        hei.poja.io.model.AppUser user =
                hei.poja.io.model.AppUser.builder().id(id).email("etu@hei.school").role(Role.STUDENT).build();
        Student model =
                Student.builder().id(id).user(user).firstName("Linus").lastName("Torvalds").track(Track.TN).enrollmentYear(2023).build();

        JStudent entity = mapper.toEntity(model);

        assertThat(entity.getEnrollmentYear()).isEqualTo(2023);
    }
}