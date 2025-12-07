package kevin.elasticsearch.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 예외 처리 테스트를 위한 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/test/exception")
public class ExceptionTestController {

    /**
     * IllegalArgumentException을 발생시키는 API
     * POST /api/test/exception/illegal-argument?userId=123
     * Body: {"name": "test", "value": "sample"}
     */
    @PostMapping("/illegal-argument")
    public ResponseEntity<?> throwIllegalArgumentException(
            @RequestParam(required = false) String userId,
            @RequestBody Map<String, Object> requestBody) {
        // IllegalArgumentException 발생
        throw new IllegalArgumentException("This is a test IllegalArgumentException");
    }

    /**
     * RuntimeException을 발생시키는 API
     * POST /api/test/exception/runtime
     * Body: {"message": "error test", "code": 500}
     */
    @PostMapping("/runtime")
    public ResponseEntity<?> throwRuntimeException(
            @RequestParam(required = false) String userId,
            @RequestBody Map<String, Object> requestBody) {
        // RuntimeException 발생
        throw new RuntimeException("This is a test RuntimeException");
    }

    /**
     * 정상 응답을 반환하는 API (비교용)
     * POST /api/test/exception/success?userId=999
     * Body: {"result": "ok"}
     */
    @PostMapping("/success")
    public ResponseEntity<?> successResponse(
            @RequestParam(required = false) String userId,
            @RequestBody Map<String, Object> requestBody) {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "userId", userId,
                "receivedBody", requestBody,
                "message", "This is a successful response"
        ));
    }
}
