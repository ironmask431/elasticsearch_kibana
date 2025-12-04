package kevin.elasticsearch.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 전역 예외 처리 핸들러
 * 모든 Controller에서 발생하는 예외를 처리함
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * IllegalArgumentException 처리
     * 잘못된 인자가 전달된 경우 400 Bad Request 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e,
            HttpServletRequest request) {

        // Request Body 읽기 (ContentCachingRequestWrapper인 경우만 가능)
        String requestBody = "N/A";
        if (request instanceof ContentCachingRequestWrapper) {
            ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
            byte[] content = wrapper.getContentAsByteArray();
            if (content.length > 0) {
                requestBody = new String(content, StandardCharsets.UTF_8);
            }
        }

        // 한 줄 로그 출력
        log.error("\nIllegalArgumentException - \nMethod: {}, URL: {}, QueryString: {}, \nBody: {}, \nMessage: {}",
                request.getMethod(),
                request.getRequestURL(),
                request.getQueryString() != null ? request.getQueryString() : "N/A",
                requestBody,
                e.getMessage());

        // 에러 응답 생성
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
}
