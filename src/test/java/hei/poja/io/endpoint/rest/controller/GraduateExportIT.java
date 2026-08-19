package hei.poja.io.endpoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;

import hei.poja.io.conf.FacadeIT;
import hei.poja.io.model.Track;
import hei.poja.io.service.AccountService;
import hei.poja.io.service.CourseAssignmentService;
import hei.poja.io.service.CourseService;
import hei.poja.io.service.GradeService;
import hei.poja.io.service.GraduateExportService;
import hei.poja.io.service.GroupService;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GraduateExportIT extends FacadeIT {

  @Autowired private AccountService accountService;
  @Autowired private CourseService courseService;
  @Autowired private GroupService groupService;
  @Autowired private CourseAssignmentService courseAssignmentService;
  @Autowired private GradeService gradeService;
  @Autowired private GraduateExportService graduateExportService;
  @Autowired private hei.poja.io.repository.ExamRepository examRepository;
  @Autowired private hei.poja.io.repository.CourseRepository courseRepository;

  @Test
  void only_student_above_ten_on_every_course_is_diplomable() {
    accountService.createAdmin("admin4@hei.school", "adminPwd");
    var teacher = accountService.createTeacher("prof3@hei.school", "profPwd", "Ada", "Lovelace");
    var goodStudent =
        accountService.createStudent("good@hei.school", "pwd", "Good", "Student", Track.EL, 2030);
    var badStudent =
        accountService.createStudent("bad@hei.school", "pwd", "Bad", "Student", Track.EL, 2030);

    var course = courseService.create("ALG201", "Algo avancee", 5, Track.EL);
    var group = groupService.create("EL-2030-A", Track.EL, 2030);
    groupService.changeGroup(goodStudent.id(), group.id());
    groupService.changeGroup(badStudent.id(), group.id());
    courseAssignmentService.create(course.id(), teacher.id(), group.id(), 2030, 1);

    var jCourse = courseRepository.findById(course.id()).orElseThrow();
    var jExam =
        hei.poja.io.repository.model.JExam.builder()
            .id(java.util.UUID.randomUUID())
            .course(jCourse)
            .title("Final")
            .dateExam(Instant.now())
            .coefficient(BigDecimal.ONE)
            .academicYear(2030)
            .semester(1)
            .build();
    examRepository.save(jExam);

    gradeService.setGrade(
        goodStudent.id(), jExam.getId(), new BigDecimal("15"), null, teacher.id());
    gradeService.setGrade(badStudent.id(), jExam.getId(), new BigDecimal("8"), null, teacher.id());

    var graduates = graduateExportService.computeGraduates(Track.EL, 2030);

    assertThat(graduates).hasSize(1);
    assertThat(graduates.get(0).student().id()).isEqualTo(goodStudent.id());
    assertThat(graduates.get(0).rank()).isEqualTo(1);
  }
}
