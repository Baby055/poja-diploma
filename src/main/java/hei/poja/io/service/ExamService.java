package hei.poja.io.service;

import hei.poja.io.exception.NotFoundException;
import hei.poja.io.mapper.ExamMapper;
import hei.poja.io.model.Exam;
import hei.poja.io.model.Role;
import hei.poja.io.repository.AppUserRepository;
import hei.poja.io.repository.CourseAssignmentRepository;
import hei.poja.io.repository.CourseRepository;
import hei.poja.io.repository.ExamRepository;
import hei.poja.io.repository.model.JAppUser;
import hei.poja.io.repository.model.JCourse;
import hei.poja.io.repository.model.JExam;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ExamService {
  private final ExamRepository examRepository;
  private final CourseRepository courseRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final AppUserRepository appUserRepository;
  private final ExamMapper examMapper;

  public List<Exam> findByCourse(UUID courseId) {
    return examMapper.toModel(examRepository.findByCourseId(courseId));
  }

  public Exam createExam(
      UUID courseId,
      String title,
      Instant dateExam,
      BigDecimal coefficient,
      int academicYear,
      int semester,
      UUID actingUserId) {
    JAppUser actingUser =
            appUserRepository
                    .findById(actingUserId)
                    .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));
    JCourse course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course introuvable"));

    checkCanCreateExam(actingUser, courseId, academicYear);

    JExam exam =
        JExam.builder()
            .id(UUID.randomUUID())
            .course(course)
            .title(title)
            .dateExam(dateExam)
            .coefficient(coefficient)
            .academicYear(academicYear)
            .semester(semester)
            .build();
    return examMapper.toModel(examRepository.save(exam));
  }

  private void checkCanCreateExam(JAppUser actingUser, UUID courseId, int academicYear) {
    if (actingUser.getRole() == Role.ADMIN) {
      return;
    }
    if (actingUser.getRole() != Role.TEACHER) {
      throw new AccessDeniedException("Seuls les enseignants ou admins peuvent creer un examen");
    }
    boolean teaches =
        courseAssignmentRepository.findByCourseIdAndAcademicYear(courseId, academicYear).stream()
            .anyMatch(ca -> ca.getTeacher().getId().equals(actingUser.getId()));
    if (!teaches) {
      throw new AccessDeniedException("Ce cours ne vous est pas assigne pour cette annee");
    }
  }
}
