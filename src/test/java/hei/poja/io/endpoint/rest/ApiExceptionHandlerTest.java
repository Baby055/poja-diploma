package hei.poja.io.endpoint.rest;

import static org.assertj.core.api.Assertions.assertThat;

import hei.poja.io.exception.BadRequestException;
import hei.poja.io.exception.ConflictException;
import hei.poja.io.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class ApiExceptionHandlerTest {

  private final ApiExceptionHandler handler = new ApiExceptionHandler();

  @Test
  void notFoundException_maps_to_404() {
    var response = handler.handleNotFound(new NotFoundException("Course introuvable"));
    assertThat(response.getStatusCode().value()).isEqualTo(404);
    assertThat(response.getBody().message()).isEqualTo("Course introuvable");
  }

  @Test
  void conflictException_maps_to_409() {
    var response = handler.handleConflict(new ConflictException("Email deja utilise"));
    assertThat(response.getStatusCode().value()).isEqualTo(409);
    assertThat(response.getBody().message()).isEqualTo("Email deja utilise");
  }

  @Test
  void badRequestException_maps_to_400() {
    var response = handler.handleBadRequest(new BadRequestException("Raison obligatoire"));
    assertThat(response.getStatusCode().value()).isEqualTo(400);
    assertThat(response.getBody().message()).isEqualTo("Raison obligatoire");
  }

  @Test
  void illegalArgumentException_also_maps_to_400() {
    var response = handler.handleBadRequest(new IllegalArgumentException("Parametre invalide"));
    assertThat(response.getStatusCode().value()).isEqualTo(400);
    assertThat(response.getBody().message()).isEqualTo("Parametre invalide");
  }

  @Test
  void accessDeniedException_maps_to_403() {
    var response = handler.handleForbidden(new AccessDeniedException("Interdit"));
    assertThat(response.getStatusCode().value()).isEqualTo(403);
    assertThat(response.getBody().message()).isEqualTo("Interdit");
  }
}
