package hei.poja.io.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import hei.poja.io.model.Exam;
import hei.poja.io.model.Track;
import hei.poja.io.repository.model.JCourse;
import hei.poja.io.repository.model.JExam;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExamMapperTest {

    private final ExamMapper mapper = new ExamMapper(new CourseMapper());

    @Test
    void toModel_maps_exam_and_nested_course() {
        UUID id = UUID.randomUUID();
        JCourse course =
                JCourse.builder().id(UUID.randomUUID()).ref("ALG101").title("Algo").credits(5).track(Track.EL).build();
        JExam entity =
                JExam.builder()
                        .id(id)
                        .course(course)
                        .title("Final")
                        .dateExam(Instant.now())
                        .coefficient(new BigDecimal("0.5"))
                        .academicYear(2024)
                        .semester(1)
                        .build();

        Exam model = mapper.toModel(entity);

        assertThat(model.course().ref()).isEqualTo("ALG101");
        assertThat(model.coefficient()).isEqualByComparingTo("0.5");
    }
}