package hei.poja.io.endpoint.rest.controller.group;

import hei.poja.io.model.StudentGroupHistory;
import hei.poja.io.security.AppUserDetails;
import hei.poja.io.service.GroupService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students/{studentId}")
@AllArgsConstructor
public class StudentGroupController {
  private final GroupService groupService;

  public record ChangeGroupRequest(UUID newGroupId) {}

  @PostMapping("/group")
  @ResponseStatus(HttpStatus.CREATED)
  public StudentGroupHistory changeGroup(
      @PathVariable UUID studentId, @RequestBody ChangeGroupRequest request) {
    return groupService.changeGroup(studentId, request.newGroupId());
  }

  @GetMapping("/group-history")
  public List<StudentGroupHistory> groupHistory(
      @PathVariable UUID studentId, @AuthenticationPrincipal AppUserDetails principal) {
    return groupService.history(studentId, principal);
  }
}
