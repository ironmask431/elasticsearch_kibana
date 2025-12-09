package kevin.elasticsearch.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Elasticsearch로 직접 로그를 전송하는 Logback Appender
 */
public class ElasticsearchAppender extends AppenderBase<ILoggingEvent> {

    private String elasticsearchUrl = "http://localhost:9200";
    private String indexName = "application-logs";
    private HttpClient httpClient;
    private ExecutorService executorService;
    private ObjectMapper objectMapper;

    @Override
    public void start() {
        this.httpClient = HttpClient.newHttpClient();
        this.executorService = Executors.newFixedThreadPool(2);
        this.objectMapper = new ObjectMapper();
        super.start();
    }

    @Override
    public void stop() {
        executorService.shutdown();
        super.stop();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        executorService.submit(() -> sendToElasticsearch(eventObject));
    }

    private void sendToElasticsearch(ILoggingEvent event) {
        try {
            // JSON 로그 생성
            ObjectNode logJson = objectMapper.createObjectNode();
            logJson.put("@timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(event.getTimeStamp())));
            logJson.put("level", event.getLevel().toString());
            logJson.put("logger", event.getLoggerName());
            logJson.put("thread", event.getThreadName());
            logJson.put("message", event.getFormattedMessage());
            
            if (event.getThrowableProxy() != null) {
                logJson.put("exception", event.getThrowableProxy().getClassName());
                logJson.put("stacktrace", event.getThrowableProxy().getMessage());
            }

            // Elasticsearch로 전송
            String jsonBody = objectMapper.writeValueAsString(logJson);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(elasticsearchUrl + "/" + indexName + "/_doc"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            addError("Failed to send log to Elasticsearch", e);
        }
    }

    public void setElasticsearchUrl(String elasticsearchUrl) {
        this.elasticsearchUrl = elasticsearchUrl;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
}
