package kevin.elasticsearch.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * Request Body를 캐싱하여 GlobalExceptionHandler에서 읽을 수 있도록 하는 필터 클래스.
 * 모든 Controller에 자동으로 적용됨.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    // 캐싱할 최대 바이트 크기 (예: 10KB)
    private static final int MAX_PAYLOAD_LENGTH = 10000;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Request Body를 여러 번 읽을 수 있도록 래핑
        // GlobalExceptionHandler에서 body를 읽기 위해 필요
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, MAX_PAYLOAD_LENGTH);

        // 필터 체인 실행 (Controller 처리)
        filterChain.doFilter(wrappedRequest, response);
    }
}
