package hei.poja.io.repository;

import hei.poja.io.repository.model.JGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<JGroup, UUID> {
    List<JGroup> findByAcademicYear(UUID academicYear);
}
