## ipia-order
Spring Boot 기반 주문·결제 서버 (Intern 과제)

### 개요
- 주문 생성/조회, 결제 승인/실패 처리, 외부 결제사(Toss Payments) 연동을 포함한 서비스입니다.
- **도메인 주도 설계(DDD)** 기반으로 비즈니스 로직을 도메인 엔티티에 캡슐화했습니다.
- **멱등키(Idempotency Key)**를 통한 중복 요청 방지 및 안전한 결제 처리 구현
- **실무형 TDD**와 클린 아키텍처 지향, 테스트 우선 개발로 구성했습니다.


### 주요 기능
- **멤버 도메인**: JWT 토큰 기반 인증 시스템
- **주문 도메인**: 생성, 상태 전이, 조회 - 멱등키를 이용한 중복 요청 방지
- **결제 도메인**: 결제 요청/승인/실패 흐름
- **보안**: Spring Security, JWT
- **운영**: Actuator, Redis 캐시, Swagger(OpenAPI) 문서



### 🚀 기술적 어필 포인트
- **DDD 기반 설계**: 비즈니스 로직을 도메인 엔티티에 캡슐화하여 유지보수성 향상
- **멱등성 보장**: Redis 기반 멱등키로 중복 결제/주문 방지 및 데이터 일관성 확보

- **표준화된 API 응답**: `@ApiResponse` 어노테이션으로 Swagger 문서 자동 생성, 일관된 에러 응답 구조
- **이벤트 기반 아키텍처**: 결제 상태 변경 시 도메인 이벤트 발행으로 느슨한 결합 구현
- **테스트 커버리지**: 단위/통합/API 테스트로 안정성 확보 (TDD 기반 개발)
- **환경별 설정 분리**: dev/test/prod 프로파일로 환경별 최적화된 설정 관리
- **Redis 캐시 활용**: 멱등키 저장 및 성능 최적화

### 기술 스택
- Java 17, Spring Boot 3.5.x
- Web (MVC, WebFlux-Client), Data JPA, Validation
- Security, JWT (jjwt)
- H2 (dev/test), Redis, Spring Cache
- Springdoc OpenAPI
- Gradle, JUnit 5

### 프로젝트 구조
```
ipia-order/
├─ order/                    # 메인 모듈
│  ├─ src/main/java/com/ipia/order/...   
│  ├─ src/test/java/com/ipia/order/...   
│  └─ src/main/resources/application.yml  

```

### 빠른 시작 (Windows / PowerShell 기준)

빠른 실행을 위해, 도커 이미지를 배포한 상태입니다.

docker-compose up -d 하면 작동합니다.

.env로 저장하고 사용해주세요. (편의를 위해 readme에 작성성)
TOSS_SECRETKEY=test_sk_mBZ1gQ4YVXKK46Dx47LZ3l2KPoqN
SPRING_DATA_REDIS_PORT=6379
SPRING_PROFILES_ACTIVE=dev
SPRING_DATA_REDIS_HOST=redis



애플리케이션 기동 후:
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb` )

- 현재 컨트롤러에 권한 매칭 리팩토링으로 인해, 컨트롤러 쪽 테스트들이 실패하는 상황입니다. 양해 부탁드립니다.
- 필터 별 주문 조회 시 조건에 PENDING이 아닌, CREATED 등 OrderStatus에 있는 상태로 기입해야 작동합니다.
- 

### 테스트 계정 (dev 프로파일)
개발 편의를 위해 자동 생성되는 테스트 계정들입니다:
- **관리자**: admin@local.dev / admin1234
- **일반 사용자**: 
  - user1@test.com / user1pass
  - user2@test.com / user2pass
  - user3@test.com / user3pass
  - user4@test.com / user4pass
  - user5@test.com / user5pass

### 환경/프로파일
기본 설정은 `order/src/main/resources/application.yml` 을 참고하세요.
- profiles: `dev`(기본), `test`, `prod (배포 가정하고)`
- JPA: `ddl-auto=update`, SQL 포맷/로그 활성화
- H2 Console 활성화 (dev)
- 로깅 레벨: `com.ipia.order`, `org.springframework.security`


실운영 전환 시 유의사항:
- `toss.secretKey` 는 환경변수/시크릿으로 주입하고, `enableRealCall=false` 로부터 점진 전환 권장
- DB/H2 → 운영 DB로 교체

### 테스트
```bash
cd order
..\gradlew.bat test
```
- 단위/통합 테스트는 JUnit 5 기반이며, WebMvc, Service, Domain 레벨 테스트를 포함합니다.


### 개발 가이드 (요약)
- TDD 우선: 테스트 케이스 작성 → 최소 구현 → 리팩토링
- 코드 스타일: 가독성/명료성 우선, 의미 있는 네이밍, 불필요한 주석 배제
- 예외/에러: 도메인 예외 명확화, Validation 적극 활용, ControllerAdvice로 응답 일관화 권장


### 라이선스
내부 과제 용도로 사용됩니다.
