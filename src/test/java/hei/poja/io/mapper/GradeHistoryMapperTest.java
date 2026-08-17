package hei.poja.io.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import hei.poja.io.model.GradeHistory;
import hei.poja.io.model.Role;
import hei.poja.io.model.Track;
import hei.poja.io.repository.model.JAppUser;
import hei.poja.io.repository.model.JCourse;
import hei.poja.io.repository.model.JExam;
import hei.poja.io.repository.model.JGrade;
import hei.poja.io.repository.model.JGradeHistory;
import hei.poja.io.repository.model.JStudent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GradeHistoryMapperTest {

  private final AppUserMapper appUserMapper = new AppUserMapper();
  private final GradeMapper gradeMapper =
      new GradeMapper(new StudentMapper(appUserMapper), new ExamMapper(new CourseMapper()));
  private final GradeHistoryMapper mapper = new GradeHistoryMapper(gradeMapper, appUserMapper);

  @Test
  void toModel_maps_history_entry_with_reason() {
    UUID studentId = UUID.randomUUID();
    JAppUser studentUser =
        JAppUser.builder().id(studentId).email("etu@hei.school").role(Role.STUDENT).build();
    JStudent student =
        JStudent.builder()
            .id(studentId)
            .user(studentUser)
            .firstName("Grace")
            .lastName("Hopper")
            .track(Track.EL)
            .enrollmentYear(2024)
            .build();
    JCourse course =
        JCourse.builder()
            .id(UUID.randomUUID())
            .ref("ALG101")
            .title("Algo")
            .credits(5)
            .track(Track.EL)
            .build();
    JExam exam =
        JExam.builder()
            .id(UUID.randomUUID())
            .course(course)
            .coefficient(BigDecimal.ONE)
            .academicYear(2024)
            .semester(1)
            .build();
    JGrade grade =
        JGrade.builder()
            .id(UUID.randomUUID())
            .student(student)
            .exam(exam)
            .value(new BigDecimal("14"))
            .lastModifiedAt(Instant.now())
            .build();
    JAppUser modifier =
        JAppUser.builder()
            .id(UUID.randomUUID())
            .email("prof@hei.school")
            .role(Role.TEACHER)
            .build();
    JGradeHistory entity =
        JGradeHistory.builder()
            .id(UUID.randomUUID())
            .grade(grade)
            .previousValue(new BigDecimal("8"))
            .newValue(new BigDecimal("14"))
            .reason("erreur de saisie")
            .modifiedBy(modifier)
            .modifiedAt(Instant.now())
            .build();

    GradeHistory model = mapper.toModel(entity);

    assertThat(model.reason()).isEqualTo("erreur de saisie");
    assertThat(model.previousValue()).isEqualByComparingTo("8");
    assertThat(model.modifiedBy().email()).isEqualTo("prof@hei.school");
  }
}
