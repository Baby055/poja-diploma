package hei.poja.io.endpoint.rest.controller.exam;

import hei.poja.io.model.Exam;
import hei.poja.io.security.AppUserDetails;
import hei.poja.io.service.ExamService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class ExamController {
  private final ExamService examService;

  public record CreateExamRequest(
      UUID courseId,
      String title,
      Instant dateExam,
      BigDecimal coefficient,
      int academicYear,
      int semester) {}

  @GetMapping("/exams")
  public List<Exam> getExams(@RequestParam UUID courseId) {
    return examService.findByCourse(courseId);
  }

  @PostMapping("/exams")
  @ResponseStatus(HttpStatus.CREATED)
  public Exam createExam(
      @RequestBody CreateExamRequest request, @AuthenticationPrincipal AppUserDetails principal) {
    return examService.createExam(
        request.courseId(),
        request.title(),
        request.dateExam(),
        request.coefficient(),
        request.academicYear(),
        request.semester(),
        principal.getId());
  }
}
