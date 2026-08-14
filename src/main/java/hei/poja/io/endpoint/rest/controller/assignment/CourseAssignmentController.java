package hei.poja.io.endpoint.rest.controller.assignment;

import hei.poja.io.model.CourseAssignment;
import hei.poja.io.service.CourseAssignmentService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/course-assignments")
@AllArgsConstructor
public class CourseAssignmentController {
    private final CourseAssignmentService courseAssignmentService;

    public record CourseAssignmentRequest(
            UUID courseId, UUID teacherId, UUID groupId, int academicYear, int semester) {}

    @GetMapping
    public List<CourseAssignment> getCourseAssignments(){
        return courseAssignmentService.findAll();
    }

    @GetMapping("/{id}")
    public CourseAssignment getCourseAssignment(@PathVariable UUID id) {
        return courseAssignmentService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseAssignment createCourseAssignment(@RequestBody CourseAssignmentRequest request) {
        return courseAssignmentService.create(
                request.courseId(),
                request.teacherId(),
                request.groupId(),
                request.academicYear(),
                request.semester());
    }

    @PutMapping("/{id}")
    public CourseAssignment updateCourseAssignment(
            @PathVariable UUID id, @RequestBody CourseAssignmentRequest request) {
        return courseAssignmentService.update(
                id,
                request.courseId(),
                request.teacherId(),
                request.groupId(),
                request.academicYear(),
                request.semester());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourseAssignment(@PathVariable UUID id) {
        courseAssignmentService.deleteById(id);
    }
}
