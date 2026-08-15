package hei.poja.io.service;

import hei.poja.io.exception.NotFoundException;
import hei.poja.io.mapper.GroupMapper;
import hei.poja.io.mapper.StudentGroupHistoryMapper;
import hei.poja.io.model.Group;
import hei.poja.io.model.Role;
import hei.poja.io.model.StudentGroupHistory;
import hei.poja.io.model.Track;
import hei.poja.io.repository.AppUserRepository;
import hei.poja.io.repository.GroupRepository;
import hei.poja.io.repository.StudentGroupHistoryRepository;
import hei.poja.io.repository.StudentRepository;
import hei.poja.io.repository.model.JAppUser;
import hei.poja.io.repository.model.JGroup;
import hei.poja.io.repository.model.JStudentGroupHistory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class GroupService {
  private final GroupRepository repository;
  private final StudentRepository studentRepository;
  private final StudentGroupHistoryRepository studentGroupHistoryRepository;
  private final AppUserRepository appUserRepository;
  private final GroupMapper mapper;
  private final StudentGroupHistoryMapper studentGroupHistoryMapper;

  public List<Group> findAll() {
    return mapper.toModel(repository.findAll());
  }

  public Group findById(UUID id) {
    return mapper.toModel(
            repository.findById(id).orElseThrow(() -> new NotFoundException("Group introuvable")));
  }

  public Group create(String ref, Track track, int academicYear) {
    JGroup group =
            JGroup.builder().id(UUID.randomUUID()).ref(ref).track(track).academicYear(academicYear).build();
    return mapper.toModel(repository.save(group));
  }

  public Group update(UUID id, String ref, Track track, int academicYear) {
    JGroup group =
            repository.findById(id).orElseThrow(() -> new NotFoundException("Group introuvable"));
    group.setRef(ref);
    group.setTrack(track);
    group.setAcademicYear(academicYear);
    return mapper.toModel(repository.save(group));
  }

  public void deleteById(UUID id) {
    repository.deleteById(id);
  }

  @Transactional
  public StudentGroupHistory changeGroup(UUID studentId, UUID newGroupId) {
    var student =
            studentRepository
                    .findById(studentId)
                    .orElseThrow(() -> new NotFoundException("Student introuvable"));
    JGroup newGroup =
            repository.findById(newGroupId).orElseThrow(() -> new NotFoundException("Group introuvable"));

    Instant now = Instant.now();
    studentGroupHistoryRepository
            .findByStudentIdAndToDateIsNull(studentId)
            .ifPresent(
                    current -> {
                      current.setToDate(now);
                      studentGroupHistoryRepository.save(current);
                    });

    JStudentGroupHistory entry =
            JStudentGroupHistory.builder()
                    .id(UUID.randomUUID())
                    .student(student)
                    .group(newGroup)
                    .fromDate(now)
                    .build();
    return studentGroupHistoryMapper.toModel(studentGroupHistoryRepository.save(entry));
  }

  @Transactional(readOnly = true)
  public List<StudentGroupHistory> history(UUID studentId, UUID actingUserId) {
    JAppUser actingUser =
            appUserRepository
                    .findById(actingUserId)
                    .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));
    boolean isStaff = actingUser.getRole() == Role.TEACHER || actingUser.getRole() == Role.ADMIN;
    if (!isStaff && !actingUserId.equals(studentId)) {
      throw new AccessDeniedException("Vous ne pouvez voir que votre propre historique de groupe");
    }
    return studentGroupHistoryMapper.toModel(
            studentGroupHistoryRepository.findByStudentIdOrderByFromDateDesc(studentId));
  }
}