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

    private static final String REQUEST_BODY_ATTRIBUTE = "cachedRequestBody";

    /**
     * IllegalArgumentException 처리
     * 잘못된 인자가 전달된 경우 400 Bad Request 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e,
            HttpServletRequest request) {

        // 요청 정보 로깅
        logException("IllegalArgumentException", e, request);

        // 에러 응답 생성
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * RuntimeException 처리
     * 예상치 못한 런타임 예외 발생 시 500 Internal Server Error 응답
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException e,
            HttpServletRequest request) {

        // 요청 정보 로깅
        logException("RuntimeException", e, request);

        // 에러 응답 생성
        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    /**
     * 예외 정보를 로그로 출력하는 공통 메서드
     */
    private void logException(String exceptionType, Exception e, HttpServletRequest request) {
        String requestBody = getRequestBody(request);
        String queryString = request.getQueryString() != null ? request.getQueryString() : "N/A";

        log.error("\n{} - Method: {}, URL: {}, QueryString: {}, \nBody: {}, \nMessage: {}",
                exceptionType,
                request.getMethod(),
                request.getRequestURL(),
                queryString,
                requestBody,
                e.getMessage());
    }

    /**
     * Request Body를 읽어오는 공통 메서드
     * Filter에서 저장한 attribute 또는 ContentCachingRequestWrapper에서 읽기 시도
     */
    private String getRequestBody(HttpServletRequest request) {
        // 1. Filter에서 저장한 attribute 확인
        Object cachedBody = request.getAttribute(REQUEST_BODY_ATTRIBUTE);
        if (cachedBody != null && !cachedBody.toString().isEmpty()) {
            return cachedBody.toString();
        }

        // 2. ContentCachingRequestWrapper에서 직접 읽기 시도
        if (request instanceof ContentCachingRequestWrapper) {
            ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
            byte[] content = wrapper.getContentAsByteArray();
            if (content.length > 0) {
                return new String(content, StandardCharsets.UTF_8);
            }
        }

        return "N/A";
    }

    /**
     * ErrorResponse 객체를 생성하는 공통 메서드
     */
    private ErrorResponse createErrorResponse(HttpStatus status, String message, String path) {
        return ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
