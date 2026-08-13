package hei.poja.io.repository;

import hei.poja.io.repository.model.JTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TeacherRepository extends JpaRepository<JTeacher, UUID> {
}
