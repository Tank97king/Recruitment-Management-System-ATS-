@echo off
TITLE Recruitment Management System (ATS) Launcher
CLS

:: Auto-detect JAVA_HOME if not defined
IF NOT DEFINED JAVA_HOME (
    IF EXIST "C:\Program Files\Java\jdk-21" (
        SET "JAVA_HOME=C:\Program Files\Java\jdk-21"
    ) ELSE IF EXIST "C:\Program Files\Amazon Corretto\jdk21*" (
        FOR /D %%D IN ("C:\Program Files\Amazon Corretto\jdk21*") DO SET "JAVA_HOME=%%D"
    ) ELSE IF EXIST "C:\Program Files\Eclipse Adoptium\jdk-21*" (
        FOR /D %%D IN ("C:\Program Files\Eclipse Adoptium\jdk-21*") DO SET "JAVA_HOME=%%D"
    ) ELSE (
        FOR /D %%D IN ("C:\Program Files\Java\jdk-21*") DO SET "JAVA_HOME=%%D"
    )
)

:: Trim quotes and trailing spaces from JAVA_HOME
IF DEFINED JAVA_HOME (
    SET "JAVA_HOME=%JAVA_HOME:"=%"
)
IF DEFINED JAVA_HOME (
    IF "%JAVA_HOME:~-1%"==" " SET "JAVA_HOME=%JAVA_HOME:~0,-1%"
)

IF DEFINED JAVA_HOME (
    SET "PATH=%JAVA_HOME%\bin;%PATH%"
)

:MENU
ECHO =====================================================================
ECHO           Recruitment Management System (ATS) Launcher
ECHO =====================================================================
ECHO.
ECHO  [1] Start Both (H2 In-Memory DB - No PostgreSQL Required) [RECOMMENDED]
ECHO  [2] Start Both (PostgreSQL Database on Port 5432)
ECHO  [3] Start Backend Only (H2 In-Memory DB)
ECHO  [4] Start Backend Only (PostgreSQL Database)
ECHO  [5] Start Frontend Only (Port 3000)
ECHO  [6] Exit
ECHO.
ECHO =====================================================================
SET /P OPTION="Choose an option [1-6]: "

IF "%OPTION%"=="1" GOTO BOTH_H2
IF "%OPTION%"=="2" GOTO BOTH_POSTGRES
IF "%OPTION%"=="3" GOTO BACKEND_H2
IF "%OPTION%"=="4" GOTO BACKEND_POSTGRES
IF "%OPTION%"=="5" GOTO FRONTEND
IF "%OPTION%"=="6" GOTO EXIT

ECHO Invalid choice. Please try again.
TIMEOUT /T 2 >NUL
CLS
GOTO MENU

:BOTH_H2
ECHO.
ECHO Launching Spring Boot Backend with H2 In-Memory Database...
START "ATS Backend (Spring Boot H2)" cmd /k "cd /d "%~dp0" && set SPRING_PROFILES_ACTIVE=h2 && mvnw.cmd spring-boot:run"
ECHO Launching React Frontend...
START "ATS Frontend (Vite)" cmd /k "cd /d "%~dp0frontend" && npm run dev"
ECHO.
ECHO Opening web browser...
TIMEOUT /T 12 >NUL
START http://localhost:3000
ECHO.
ECHO Both services are starting in separate windows.
ECHO Frontend URL : http://localhost:3000
ECHO Backend URL  : http://localhost:8080
ECHO Swagger UI   : http://localhost:8080/swagger-ui/index.html
ECHO H2 Console   : http://localhost:8080/h2-console
ECHO.
PAUSE
GOTO EXIT

:BOTH_POSTGRES
ECHO.
ECHO Launching Spring Boot Backend with PostgreSQL Database...
START "ATS Backend (Spring Boot Postgres)" cmd /k "cd /d "%~dp0" && mvnw.cmd spring-boot:run"
ECHO Launching React Frontend...
START "ATS Frontend (Vite)" cmd /k "cd /d "%~dp0frontend" && npm run dev"
ECHO.
ECHO Both services are starting in separate windows.
ECHO Frontend URL : http://localhost:3000
ECHO Backend URL  : http://localhost:8080
ECHO Swagger UI   : http://localhost:8080/swagger-ui/index.html
ECHO.
PAUSE
GOTO EXIT

:BACKEND_H2
ECHO.
ECHO Starting Spring Boot Backend with H2 In-Memory DB...
cd /d "%~dp0"
set SPRING_PROFILES_ACTIVE=h2
mvnw.cmd spring-boot:run
PAUSE
GOTO EXIT

:BACKEND_POSTGRES
ECHO.
ECHO Starting Spring Boot Backend with PostgreSQL...
cd /d "%~dp0"
mvnw.cmd spring-boot:run
PAUSE
GOTO EXIT

:FRONTEND
ECHO.
ECHO Starting React Frontend...
cd /d "%~dp0frontend"
npm run dev
PAUSE
GOTO EXIT

:EXIT
EXIT
