# Spring Boot REST API 에러 로깅 & Elasticsearch/Kibana 모니터링

Spring Boot 애플리케이션의 REST API에서 발생하는 예외를 체계적으로 로깅하고, Elasticsearch와 Kibana를 통해 실시간으로 모니터링하는 예제 프로젝트입니다.

## 📋 프로젝트 개요

이 프로젝트는 다음과 같은 시나리오를 구현합니다:

1. **REST API 구성**: 간단한 회사/직원 관리 API 제공
2. **에러 로깅**: Filter와 GlobalExceptionHandler를 통해 예외 발생 시 상세 정보 로깅
3. **Elasticsearch 전송**: 커스텀 Logback Appender를 통해 Elasticsearch로 직접 로그 전송
4. **Kibana 모니터링**: Kibana 대시보드를 통한 실시간 에러 모니터링

## 🏗️ 아키텍처

```
Client Request
     ↓
RequestLoggingFilter (Request Body 캐싱)
     ↓
Controller (비즈니스 로직)
     ↓
Exception 발생 시
     ↓
GlobalExceptionHandler (예외 처리 & 상세 로깅)
     ↓
ElasticsearchAppender (로그 전송)
     ↓
Elasticsearch (로그 저장)
     ↓
Kibana (로그 시각화 & 모니터링)
```

## 🛠️ 기술 스택

- **Java**: 21
- **Spring Boot**: 4.0.0
- **Database**: MySQL + Spring Data JPA
- **Logging**: Logback + 커스텀 ElasticsearchAppender
- **Monitoring**: Elasticsearch 8.x + Kibana 8.x
- **Build Tool**: Gradle

## 📂 주요 컴포넌트

### 1. RequestLoggingFilter

```java
@Component
public class RequestLoggingFilter extends OncePerRequestFilter
```

**역할**: HTTP Request Body를 캐싱하여 GlobalExceptionHandler에서 읽을 수 있도록 함

**주요 기능**:
- `ContentCachingRequestWrapper`를 사용해 Request Body를 메모리에 캐싱
- 최대 10KB까지 캐싱 (설정 가능)
- 모든 컨트롤러에 자동으로 적용됨

### 2. GlobalExceptionHandler

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler
```

**역할**: 전역 예외 처리 및 상세 에러 로그 기록

**주요 기능**:
- `IllegalArgumentException` → 400 Bad Request
- `RuntimeException` → 500 Internal Server Error
- 예외 발생 시 다음 정보를 로그로 기록:
  - HTTP Method (GET, POST, PUT, DELETE)
  - Request URL
  - Query String
  - Request Body
  - 예외 메시지

**로그 예시**:
```
ERROR IllegalArgumentException - Method: POST, URL: http://localhost:8080/api/test/exception/illegal-argument, 
QueryString: userId=123, Body: {"name":"test","value":"sample"}, Message: This is a test IllegalArgumentException
```

### 3. ElasticsearchAppender

```java
public class ElasticsearchAppender extends AppenderBase<ILoggingEvent>
```

**역할**: Logback 로그를 Elasticsearch로 직접 전송 (Logstash 불필요)

**주요 기능**:
- 날짜별 인덱스 자동 생성 (예: `application-logs-2025.12.13`)
- 비동기 전송으로 애플리케이션 성능 영향 최소화
- JSON 포맷으로 구조화된 로그 전송
- 다음 필드를 포함:
  - `@timestamp`: ISO 8601 형식의 타임스탬프
  - `level`: 로그 레벨 (INFO, ERROR 등)
  - `logger`: 로거 이름
  - `thread`: 스레드 이름
  - `message`: 로그 메시지
  - `exception`: 예외 클래스명 (예외 발생 시)
  - `stacktrace`: 스택트레이스 (예외 발생 시)

### 4. logback-spring.xml 설정

```xml
<appender name="ELASTIC" class="kevin.elasticsearch.logging.ElasticsearchAppender">
    <elasticsearchUrl>http://localhost:9200</elasticsearchUrl>
    <indexName>application-logs</indexName>
    <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
        <level>INFO</level>
    </filter>
</appender>
```

**프로파일별 설정**:
- **local**: 콘솔 + 파일 + Elasticsearch
- **dev/prd**: 콘솔 + Elasticsearch
- INFO 레벨 이상의 로그만 Elasticsearch로 전송

## 🚀 시작하기

### 사전 요구사항

1. **Docker 설치** (Elasticsearch & Kibana 실행용)
2. **MySQL 설치** (또는 Docker로 실행)
3. **Java 21**
4. **Gradle**

### 1. Elasticsearch & Kibana 실행

Docker Compose를 사용하거나 개별적으로 실행:

```bash
# Elasticsearch
docker run -d --name elasticsearch \
  -p 9200:9200 -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0

# Kibana
docker run -d --name kibana \
  -p 5601:5601 \
  -e "ELASTICSEARCH_HOSTS=http://localhost:9200" \
  docker.elastic.co/kibana/kibana:8.11.0
```

### 2. MySQL 설정

```sql
CREATE DATABASE test_db;
```

`application-local.yml`에서 데이터베이스 접속 정보 수정:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test_db
    username: your_username
    password: your_password
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

또는

```bash
./gradlew build
java -jar build/libs/logback_elasticsearch_kibana-0.0.1-SNAPSHOT.jar
```

## 🧪 테스트

### 예외 발생 테스트 API

프로젝트는 테스트용 API를 제공합니다:

#### 1. IllegalArgumentException 테스트

```bash
curl -X POST http://localhost:8080/api/test/exception/illegal-argument?userId=123 \
  -H "Content-Type: application/json" \
  -d '{"name":"test","value":"sample"}'
```

**응답**:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "This is a test IllegalArgumentException",
  "path": "/api/test/exception/illegal-argument",
  "timestamp": "2025-12-13T10:30:45.123"
}
```

#### 2. RuntimeException 테스트

```bash
curl -X POST http://localhost:8080/api/test/exception/runtime \
  -H "Content-Type: application/json" \
  -d '{"message":"error test","code":500}'
```

**응답**:
```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "This is a test RuntimeException",
  "path": "/api/test/exception/runtime",
  "timestamp": "2025-12-13T10:31:20.456"
}
```

#### 3. 정상 응답 테스트 (비교용)

```bash
curl -X POST http://localhost:8080/api/test/exception/success?userId=999 \
  -H "Content-Type: application/json" \
  -d '{"result":"ok"}'
```

## 📊 Kibana에서 로그 확인

### 1. Kibana 접속

브라우저에서 `http://localhost:5601` 접속

### 2. Index Pattern 생성

1. **Management** → **Stack Management** → **Index Patterns** 이동
2. **Create index pattern** 클릭
3. Index pattern 이름: `application-logs-*`
4. Timestamp field: `@timestamp` 선택
5. **Create index pattern** 클릭

### 3. Discover에서 로그 조회

1. **Analytics** → **Discover** 이동
2. 시간 범위 설정 (예: Last 15 minutes)
3. 필터링 조건 추가:
   - `level: ERROR` → 에러 로그만 조회
   - `message: *IllegalArgumentException*` → 특정 예외 검색

### 4. 로그 필드 확인

Elasticsearch에 저장된 로그 예시:

```json
{
  "@timestamp": "2025-12-13T10:30:45.123Z",
  "level": "ERROR",
  "logger": "kevin.elasticsearch.exception.GlobalExceptionHandler",
  "thread": "http-nio-8080-exec-1",
  "message": "IllegalArgumentException - Method: POST, URL: http://localhost:8080/api/test/exception/illegal-argument, QueryString: userId=123, Body: {\"name\":\"test\",\"value\":\"sample\"}, Message: This is a test IllegalArgumentException",
  "exception": "java.lang.IllegalArgumentException",
  "stacktrace": "This is a test IllegalArgumentException"
}
```

## 📈 Kibana 로그 screenshot
<img width="1045" height="710" alt="스크린샷 2025-12-08 오후 9 19 22" src="https://github.com/user-attachments/assets/4cad681f-009f-4a54-b419-6479075f2ecc" />



## 🔍 주요 특징

### 1. Logstash 불필요

- 기존: Application → Logstash → Elasticsearch → Kibana
- 현재: Application → Elasticsearch → Kibana
- ElasticsearchAppender가 HTTP로 직접 전송

### 2. 날짜별 인덱스 자동 관리

- 매일 자동으로 새로운 인덱스 생성
- 예: `application-logs-2025.12.13`, `application-logs-2025.12.14`
- ILM(Index Lifecycle Management) 정책 적용 가능

### 3. 상세한 에러 컨텍스트

예외 발생 시 다음 정보를 모두 기록:
- HTTP Method
- 전체 URL
- Query Parameters
- Request Body
- Exception Type
- Error Message

### 4. 성능 최적화

- 비동기 로그 전송 (ExecutorService)
- Request Body 캐싱 크기 제한 (10KB)
- INFO 레벨 이상만 Elasticsearch 전송

## 🔧 설정 커스터마이징

### Elasticsearch URL 변경

`logback-spring.xml`:

```xml
<appender name="ELASTIC" class="kevin.elasticsearch.logging.ElasticsearchAppender">
    <elasticsearchUrl>http://your-elasticsearch-host:9200</elasticsearchUrl>
    <indexName>your-custom-index-name</indexName>
</appender>
```

### Request Body 캐싱 크기 조정

`RequestLoggingFilter.java`:

```java
private static final int MAX_PAYLOAD_LENGTH = 50000; // 50KB로 증가
```

### 로그 레벨 조정

`logback-spring.xml`:

```xml
<!-- ERROR 레벨만 Elasticsearch로 전송 -->
<filter class="ch.qos.logback.classic.filter.ThresholdFilter">
    <level>ERROR</level>
</filter>
```

## 📝 추가 API 엔드포인트

프로젝트는 다음 비즈니스 API도 제공합니다:

### Company API
- `POST /api/companies` - 회사 생성
- `GET /api/companies` - 전체 회사 조회
- `GET /api/companies/{id}` - 특정 회사 조회
- `PUT /api/companies/{id}` - 회사 정보 수정
- `DELETE /api/companies/{id}` - 회사 삭제

### Employee API
- `POST /api/employees` - 직원 생성
- `GET /api/employees` - 전체 직원 조회
- `GET /api/employees/{id}` - 특정 직원 조회
- `PUT /api/employees/{id}` - 직원 정보 수정
- `DELETE /api/employees/{id}` - 직원 삭제




