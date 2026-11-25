package fh.swen.paperless.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Turns the exceptions of all layers into ProblemDetail responses:
 * <ul>
 *     <li>controller: bean validation of the request DTOs (400 with the field errors)</li>
 *     <li>business layer: {@link ServiceException} subclasses (4xx)</li>
 *     <li>service agents: {@link ServiceAgentException} subclasses for MinIO and RabbitMQ (503)</li>
 *     <li>data access layer: Spring's {@link DataAccessException} (500)</li>
 *     <li>anything else (500)</li>
 * </ul>
 * Spring MVC's own exceptions (wrong method, unreadable body, ...) are handled by the base class.
 * Internal details are only logged, never sent to the client.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // business layer

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException e, HttpServletRequest request) {
        return clientError(HttpStatus.NOT_FOUND, e, request);
    }

    @ExceptionHandler({UsernameTakenException.class, TagNameTakenException.class})
    public ProblemDetail handleNameTaken(ServiceException e, HttpServletRequest request) {
        return clientError(HttpStatus.CONFLICT, e, request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException e, HttpServletRequest request) {
        return clientError(HttpStatus.UNAUTHORIZED, e, request);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ProblemDetail handleInvalidFile(InvalidFileException e, HttpServletRequest request) {
        return clientError(HttpStatus.BAD_REQUEST, e, request);
    }

    // service agents (details are logged where the exception is thrown)

    @ExceptionHandler(FileStorageException.class)
    public ProblemDetail handleFileStorage(FileStorageException e, HttpServletRequest request) {
        return unavailable("File storage is not available", e, request);
    }

    @ExceptionHandler(MessagePublishException.class)
    public ProblemDetail handleMessagePublish(MessagePublishException e, HttpServletRequest request) {
        return unavailable("Document processing is not available, please try again later", e, request);
    }

    // data access layer

    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail handleDataAccess(DataAccessException e, HttpServletRequest request) {
        log.error("Database error on {} {}", request.getMethod(), request.getRequestURI(), e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
    }

    // fallback

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception e, HttpServletRequest request) {
        log.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
    }

    // controller: request validation (overrides of ResponseEntityExceptionHandler)

    // @Valid failed on a request DTO: list every invalid field in "detail" and as "errors" map
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        String detail = "Invalid input: " + errors.entrySet().stream()
                .map(entry -> entry.getKey() + " " + entry.getValue())
                .collect(Collectors.joining(", "));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setProperty("errors", errors);
        log.warn("Validation failed on {}: {}", request.getDescription(false), errors);
        return ResponseEntity.badRequest().headers(headers).body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e,
                                                                          HttpHeaders headers,
                                                                          HttpStatusCode status,
                                                                          WebRequest request) {
        log.warn("Upload too large on {}", request.getDescription(false));
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONTENT_TOO_LARGE, "File is too large (max 20 MB)");
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).headers(headers).body(problem);
    }

    private ProblemDetail clientError(HttpStatus status, Exception e, HttpServletRequest request) {
        log.warn("{} {} -> {}: {}", request.getMethod(), request.getRequestURI(), status.value(), e.getMessage());
        return ProblemDetail.forStatusAndDetail(status, e.getMessage());
    }

    private ProblemDetail unavailable(String detail, ServiceAgentException e, HttpServletRequest request) {
        log.error("{} {} -> 503: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, detail);
    }
}
