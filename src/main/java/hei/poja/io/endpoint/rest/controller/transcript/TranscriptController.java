package hei.poja.io.endpoint.rest.controller.transcript;

import hei.poja.io.security.AppUserDetails;
import hei.poja.io.service.TranscriptService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TranscriptController {
  private final TranscriptService transcriptService;

  @PostMapping("/students/{studentId}/transcript")
  public ResponseEntity<Void> sendTranscript(
      @PathVariable UUID studentId,
      @RequestParam(defaultValue = "false") boolean complete,
      @AuthenticationPrincipal AppUserDetails principal) {
    transcriptService.assertCanRequestTranscript(studentId, principal);
    transcriptService.generateAndSendTranscript(studentId, complete);
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }
}
