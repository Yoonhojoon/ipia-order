FROM eclipse-temurin:17-jdk AS builder
WORKDIR /workspace

# 모듈 소스 전체 복사 (Gradle Wrapper 포함)
COPY . .

# 권한 부여 및 빌드 (테스트는 컨테이너 빌드 속도 위해 생략)
RUN chmod +x ./gradlew && ./gradlew clean bootJar -x test


FROM eclipse-temurin:17-jre
WORKDIR /app

EXPOSE 8080

# 빌드 산출물 복사
COPY --from=builder /workspace/build/libs/order-0.0.1-SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]


