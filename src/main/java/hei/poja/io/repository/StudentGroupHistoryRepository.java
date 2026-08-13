package hei.poja.io.repository;

import hei.poja.io.repository.model.JStudentGroupHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentGroupHistoryRepository extends JpaRepository<JStudentGroupHistory, UUID> {
    List<JStudentGroupHistory> findByStudentIdOrderByFromDateDesc(UUID studentId);

    Optional<JStudentGroupHistory> findByStudentIdAndToDateIsNull(UUID studentId);
}
