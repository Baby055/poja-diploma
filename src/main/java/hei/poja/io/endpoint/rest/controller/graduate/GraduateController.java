package hei.poja.io.endpoint.rest.controller.graduate;

import hei.poja.io.model.Track;
import hei.poja.io.service.GraduateExportService;
import java.net.URI;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class GraduateController {
  private final GraduateExportService graduateExportService;

  @GetMapping("/promotions/{enrollmentYear}/{track}/graduates")
  public Object getGraduates(@PathVariable int enrollmentYear, @PathVariable Track track) {
    return graduateExportService.computeGraduates(track, enrollmentYear);
  }

  @GetMapping("/promotions/{enrollmentYear}/{track}/graduates/export")
  public ResponseEntity<Void> exportGraduates(
      @PathVariable int enrollmentYear, @PathVariable Track track) {
    var presignedUrl = graduateExportService.exportAndUpload(track, enrollmentYear);
    return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create(presignedUrl.toString()))
        .build();
  }
}
