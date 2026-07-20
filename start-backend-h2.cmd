@echo off
TITLE ATS Backend (H2 In-Memory DB)

:: Set JAVA_HOME explicitly
SET "JAVA_HOME=C:\Program Files\Java\jdk-21"
SET "PATH=%JAVA_HOME%\bin;%PATH%"

:: Use Maven directly (from wrapper cache) to avoid parentheses-in-path bug with mvnw.cmd
SET "MVN=C:\Users\Admin\.m2\wrapper\dists\apache-maven-3.9.8-bin\337e6d14\apache-maven-3.9.8\bin\mvn.cmd"

SET SPRING_PROFILES_ACTIVE=h2

echo =====================================================================
echo   ATS Backend - H2 In-Memory Database
echo =====================================================================
echo JAVA_HOME  = %JAVA_HOME%
echo Profile    = %SPRING_PROFILES_ACTIVE%
echo Backend    : http://localhost:8080
echo H2 Console : http://localhost:8080/h2-console
echo Swagger UI : http://localhost:8080/swagger-ui/index.html
echo =====================================================================
echo.
echo Starting Spring Boot...
echo.

"%MVN%" spring-boot:run -Dspring-boot.run.profiles=h2

PAUSE
