package hei.poja.io.repository;

import hei.poja.io.repository.model.JGradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GradeHistoryRepository extends JpaRepository<JGradeHistory, UUID> {
    List<JGradeHistory> findByGradeIdOrderByModifiedAtDesc(UUID gradeId);
}
