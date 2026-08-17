package hei.poja.io.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import hei.poja.io.model.Grade;
import hei.poja.io.model.Role;
import hei.poja.io.model.Track;
import hei.poja.io.repository.model.JAppUser;
import hei.poja.io.repository.model.JCourse;
import hei.poja.io.repository.model.JExam;
import hei.poja.io.repository.model.JGrade;
import hei.poja.io.repository.model.JStudent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GradeMapperTest {

  private final AppUserMapper appUserMapper = new AppUserMapper();
  private final GradeMapper mapper =
      new GradeMapper(new StudentMapper(appUserMapper), new ExamMapper(new CourseMapper()));

  @Test
  void toModel_maps_grade_with_nested_student_and_exam() {
    UUID studentId = UUID.randomUUID();
    JAppUser user =
        JAppUser.builder().id(studentId).email("etu@hei.school").role(Role.STUDENT).build();
    JStudent student =
        JStudent.builder()
            .id(studentId)
            .user(user)
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
            .title("Final")
            .coefficient(BigDecimal.ONE)
            .academicYear(2024)
            .semester(1)
            .build();
    JGrade entity =
        JGrade.builder()
            .id(UUID.randomUUID())
            .student(student)
            .exam(exam)
            .value(new BigDecimal("14.5"))
            .lastModifiedAt(Instant.now())
            .build();

    Grade model = mapper.toModel(entity);

    assertThat(model.student().firstName()).isEqualTo("Grace");
    assertThat(model.exam().course().ref()).isEqualTo("ALG101");
    assertThat(model.value()).isEqualByComparingTo("14.5");
  }
}
