package hei.poja.io.repository;

import hei.poja.io.model.Track;
import hei.poja.io.repository.model.JCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<JCourse, UUID> {
    List<JCourse> findByTrackIsNullOrTrack(Track track);
}
