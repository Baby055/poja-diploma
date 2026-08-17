package hei.poja.io.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import hei.poja.io.model.CourseAssignment;
import hei.poja.io.model.Track;
import hei.poja.io.repository.model.JCourse;
import hei.poja.io.repository.model.JCourseAssignment;
import hei.poja.io.repository.model.JGroup;
import hei.poja.io.repository.model.JTeacher;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CourseAssignmentMapperTest {

    private final AppUserMapper appUserMapper = new AppUserMapper();
    private final CourseAssignmentMapper mapper =
            new CourseAssignmentMapper(new CourseMapper(), new TeacherMapper(appUserMapper), new GroupMapper());

    @Test
    void toModel_maps_all_nested_entities() {
        JCourse course =
                JCourse.builder().id(UUID.randomUUID()).ref("ALG101").title("Algo").credits(5).track(Track.EL).build();
        JTeacher teacher = JTeacher.builder().id(UUID.randomUUID()).firstName("Ada").lastName("Lovelace").build();
        JGroup group = JGroup.builder().id(UUID.randomUUID()).ref("EL1-A").track(Track.EL).academicYear(2024).build();
        JCourseAssignment entity =
                JCourseAssignment.builder().id(UUID.randomUUID()).course(course).teacher(teacher).group(group).academicYear(2024).semester(1).build();

        CourseAssignment model = mapper.toModel(entity);

        assertThat(model.course().ref()).isEqualTo("ALG101");
        assertThat(model.teacher().firstName()).isEqualTo("Ada");
        assertThat(model.group().ref()).isEqualTo("EL1-A");
    }
}