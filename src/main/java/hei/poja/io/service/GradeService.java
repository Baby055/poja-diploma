package hei.poja.io.service;

import hei.poja.io.exception.BadRequestException;
import hei.poja.io.exception.NotFoundException;
import hei.poja.io.mapper.GradeHistoryMapper;
import hei.poja.io.mapper.GradeMapper;
import hei.poja.io.model.Grade;
import hei.poja.io.model.GradeHistory;
import hei.poja.io.model.Role;
import hei.poja.io.repository.*;
import hei.poja.io.repository.model.JAppUser;
import hei.poja.io.repository.model.JExam;
import hei.poja.io.repository.model.JGrade;
import hei.poja.io.repository.model.JGradeHistory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class GradeService {
  private final GradeRepository gradeRepository;
  private final GradeHistoryRepository gradeHistoryRepository;
  private final ExamRepository examRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final StudentRepository studentRepository;
  private final GradeMapper gradeMapper;
  private final GradeHistoryMapper gradeHistoryMapper;

  @Transactional(readOnly = true)
  public List<Grade> getGradesForStudent(UUID studentId) {
    return gradeMapper.toModel(gradeRepository.findByStudentId(studentId));
  }

  @Transactional(readOnly = true)
  public List<GradeHistory> getHistory(UUID gradeId) {
    return gradeHistoryMapper.toModel(
        gradeHistoryRepository.findByGradeIdOrderByModifiedAtDesc(gradeId));
  }

  @Transactional
  public Grade setGrade(
      UUID studentId, UUID examId, BigDecimal newValue, String reason, JAppUser actingUser) {
    JExam exam =
        examRepository
            .findById(examId)
            .orElseThrow(() -> new NotFoundException("Exam introuvable"));

    checkTeacherOwnsCourses(actingUser, exam);

    var existing = gradeRepository.findByStudentIdAndExamId(studentId, examId);

    BigDecimal previousValue = existing.map(JGrade::getValue).orElse(null);
    if (existing.isPresent() && (reason == null || reason.isBlank())) {
      throw new BadRequestException("Une raison est obligatoire pour modifier une note existante");
    }

    JGrade grade = existing.orElseGet(JGrade::new);
    if (grade.getId() == null) {
      grade.setId(UUID.randomUUID());
      grade.setStudent(studentRepository.getReferenceById(studentId));
      grade.setExam(exam);
    }
    grade.setValue(newValue);
    grade.setLastModifiedAt(Instant.now());
    grade = gradeRepository.save(grade);

    var history =
        JGradeHistory.builder()
            .id(UUID.randomUUID())
            .grade(grade)
            .previousValue(previousValue)
            .newValue(newValue)
            .reason(reason == null || reason.isBlank() ? "Saisie initiale" : reason)
            .modifiedBy(actingUser)
            .modifiedAt(Instant.now())
            .build();
    gradeHistoryRepository.save(history);

    return gradeMapper.toModel(grade);
  }

  public void checkTeacherOwnsCourses(JAppUser actingUser, JExam exam) {
    if (actingUser.getRole() == Role.ADMIN) {
      return;
    }
    if (actingUser.getRole() != Role.TEACHER) {
      throw new AccessDeniedException("Seuls les enseignants ou admins peuvent noter");
    }
    boolean teaches =
        !courseAssignmentRepository
            .findByCourseIdAndAcademicYear(exam.getCourse().getId(), exam.getAcademicYear())
            .stream()
            .filter(ca -> ca.getTeacher().getId().equals(actingUser.getId()))
            .toList()
            .isEmpty();
    if (!teaches) {
      throw new AccessDeniedException("Ce cours ne vous est pas assigne");
    }
  }
}
