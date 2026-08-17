package hei.poja.io.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import hei.poja.io.model.Role;
import hei.poja.io.model.StudentGroupHistory;
import hei.poja.io.model.Track;
import hei.poja.io.repository.model.JAppUser;
import hei.poja.io.repository.model.JGroup;
import hei.poja.io.repository.model.JStudent;
import hei.poja.io.repository.model.JStudentGroupHistory;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StudentGroupHistoryMapperTest {

    private final AppUserMapper appUserMapper = new AppUserMapper();
    private final StudentGroupHistoryMapper mapper =
            new StudentGroupHistoryMapper(new StudentMapper(appUserMapper), new GroupMapper());

    @Test
    void toModel_maps_open_entry_with_null_toDate() {
        UUID studentId = UUID.randomUUID();
        JAppUser user = JAppUser.builder().id(studentId).email("etu@hei.school").role(Role.STUDENT).build();
        JStudent student =
                JStudent.builder().id(studentId).user(user).firstName("Grace").lastName("Hopper").track(Track.EL).enrollmentYear(2024).build();
        JGroup group = JGroup.builder().id(UUID.randomUUID()).ref("EL1-A").track(Track.EL).academicYear(2024).build();
        JStudentGroupHistory entity =
                JStudentGroupHistory.builder().id(UUID.randomUUID()).student(student).group(group).fromDate(Instant.now()).toDate(null).build();

        StudentGroupHistory model = mapper.toModel(entity);

        assertThat(model.toDate()).isNull();
        assertThat(model.group().ref()).isEqualTo("EL1-A");
    }
}