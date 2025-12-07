package kevin.elasticsearch.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 모든 HTTP 요청의 URL, HTTP 메소드, Query String, Request Body, response status 를 로깅하는 필터 클래스.
 * 모든 Controller에 자동으로 적용됨.
 */
@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    // 캐싱할 최대 바이트 크기 (예: 10KB)
    private static final int MAX_PAYLOAD_LENGTH = 10000;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Request Body를 여러 번 읽을 수 있도록 래핑. 이게 없으면 여기서 읽고, 컨트롤러에서 읽지 못함.
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, MAX_PAYLOAD_LENGTH);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        // 요청 URL 로깅
        log.info("Method: {}, URL: {}, query string: {}", request.getMethod(), request.getRequestURL(), request.getQueryString());

        // 필터 체인 실행 (Controller 처리)
        filterChain.doFilter(wrappedRequest, wrappedResponse);

        // Request Body 로깅
        String requestBody = getRequestBody(wrappedRequest);
        if (requestBody != null && !requestBody.isEmpty()) {
            log.info("Request Body: {}", requestBody);
        }

        // Response 정보 로깅 (선택사항)
        log.info("Status: {}", wrappedResponse.getStatus());

        // Response Body를 실제로 클라이언트에 전송
        wrappedResponse.copyBodyToResponse();
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return null;
    }
}
