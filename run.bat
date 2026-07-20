@echo off
TITLE Recruitment Management System (ATS) Launcher
CLS

:: =====================================================================
::  FIX: Do NOT use mvnw.cmd — it fails when the project path contains
::  parentheses like "(ATS)". We resolve Maven from the wrapper cache
::  and call mvn.cmd directly.
:: =====================================================================

:: ── 1. Auto-detect JAVA_HOME ─────────────────────────────────────────
IF NOT DEFINED JAVA_HOME (
    IF EXIST "C:\Program Files\Java\jdk-21" (
        SET "JAVA_HOME=C:\Program Files\Java\jdk-21"
    )
)
IF NOT DEFINED JAVA_HOME (
    FOR /D %%D IN ("C:\Program Files\Java\jdk-21*") DO SET "JAVA_HOME=%%D"
)
IF NOT DEFINED JAVA_HOME (
    FOR /D %%D IN ("C:\Program Files\Amazon Corretto\jdk21*") DO SET "JAVA_HOME=%%D"
)
IF NOT DEFINED JAVA_HOME (
    FOR /D %%D IN ("C:\Program Files\Eclipse Adoptium\jdk-21*") DO SET "JAVA_HOME=%%D"
)

:: Strip trailing quote/space
IF DEFINED JAVA_HOME SET "JAVA_HOME=%JAVA_HOME:"=%"
IF DEFINED JAVA_HOME IF "%JAVA_HOME:~-1%"==" " SET "JAVA_HOME=%JAVA_HOME:~0,-1%"

IF NOT DEFINED JAVA_HOME (
    ECHO [ERROR] Java 21 not found. Please install JDK 21 and set JAVA_HOME.
    PAUSE & EXIT /B 1
)
SET "PATH=%JAVA_HOME%\bin;%PATH%"

:: ── 2. Auto-detect Maven from wrapper cache ───────────────────────────
::  Structure: %USERPROFILE%\.m2\wrapper\dists\apache-maven-X.X.X-bin\<hash>\apache-maven-X.X.X\bin\mvn.cmd
SET "MVN="
SET "MAVEN_DISTS=%USERPROFILE%\.m2\wrapper\dists"

FOR /F "delims=" %%F IN ('DIR /B /S "%MAVEN_DISTS%\mvn.cmd" 2^>nul') DO (
    SET "MVN=%%F"
)

IF NOT DEFINED MVN (
    ECHO [ERROR] Maven not found in %MAVEN_DISTS%.
    ECHO         Tip: Run mvnw.cmd once from a path WITHOUT parentheses
    ECHO              e.g.  cd D:\  ^&^&  "D:\Recruitment Management System (ATS)\mvnw.cmd" --version
    PAUSE & EXIT /B 1
)

:: ── 3. Project root (folder where this .bat lives) ───────────────────
SET "PROJECT_DIR=%~dp0"
IF "%PROJECT_DIR:~-1%"=="\" SET "PROJECT_DIR=%PROJECT_DIR:~0,-1%"

:: ── 4. Menu ──────────────────────────────────────────────────────────
:MENU
CLS
ECHO.
ECHO  +=============================================================+
ECHO  ^|      Recruitment Management System (ATS) Launcher          ^|
ECHO  +=============================================================+
ECHO  ^|  Java  : %JAVA_HOME%
ECHO  ^|  Maven : %MVN%
ECHO  +=============================================================+
ECHO  ^|                                                             ^|
ECHO  ^|  [1]  Start Both  -  H2 In-Memory DB  [RECOMMENDED]        ^|
ECHO  ^|  [2]  Start Both  -  PostgreSQL (port 5432)                ^|
ECHO  ^|  [3]  Backend Only  -  H2 In-Memory DB                     ^|
ECHO  ^|  [4]  Backend Only  -  PostgreSQL                          ^|
ECHO  ^|  [5]  Frontend Only  (http://localhost:3000)                ^|
ECHO  ^|  [6]  Exit                                                  ^|
ECHO  ^|                                                             ^|
ECHO  +=============================================================+
ECHO.
SET /P OPTION="  Choose an option [1-6]: "

IF "%OPTION%"=="1" GOTO BOTH_H2
IF "%OPTION%"=="2" GOTO BOTH_POSTGRES
IF "%OPTION%"=="3" GOTO BACKEND_H2
IF "%OPTION%"=="4" GOTO BACKEND_POSTGRES
IF "%OPTION%"=="5" GOTO FRONTEND
IF "%OPTION%"=="6" GOTO EXIT_NOW

ECHO  [!] Invalid choice. Please try again.
TIMEOUT /T 2 >NUL
GOTO MENU

:: ─────────────────────────────────────────────────────────────────────
:BOTH_H2
ECHO.
ECHO  Starting Backend (H2) in a new window...
START "ATS Backend - H2" cmd /k "cd /d "%PROJECT_DIR%" && set "JAVA_HOME=%JAVA_HOME%" && set "PATH=%JAVA_HOME%\bin;%PATH%" && "%MVN%" spring-boot:run -Dspring-boot.run.profiles=h2"
ECHO  Starting Frontend in a new window...
START "ATS Frontend" cmd /k "cd /d "%PROJECT_DIR%\frontend" && npm run dev"
ECHO.
ECHO  Waiting 15s for services to initialize...
TIMEOUT /T 15 >NUL
START http://localhost:3000
ECHO.
ECHO  +--------------------------------------------------+
ECHO  ^|  Frontend  : http://localhost:3000               ^|
ECHO  ^|  Backend   : http://localhost:8080               ^|
ECHO  ^|  Swagger   : http://localhost:8080/swagger-ui/   ^|
ECHO  ^|  H2 Console: http://localhost:8080/h2-console    ^|
ECHO  +--------------------------------------------------+
ECHO.
PAUSE
GOTO EXIT_NOW

:: ─────────────────────────────────────────────────────────────────────
:BOTH_POSTGRES
ECHO.
ECHO  [!] Make sure PostgreSQL is running on port 5432.
ECHO.
START "ATS Backend - PostgreSQL" cmd /k "cd /d "%PROJECT_DIR%" && set "JAVA_HOME=%JAVA_HOME%" && set "PATH=%JAVA_HOME%\bin;%PATH%" && "%MVN%" spring-boot:run"
START "ATS Frontend" cmd /k "cd /d "%PROJECT_DIR%\frontend" && npm run dev"
ECHO.
ECHO  +--------------------------------------------------+
ECHO  ^|  Frontend  : http://localhost:3000               ^|
ECHO  ^|  Backend   : http://localhost:8080               ^|
ECHO  ^|  Swagger   : http://localhost:8080/swagger-ui/   ^|
ECHO  +--------------------------------------------------+
ECHO.
PAUSE
GOTO EXIT_NOW

:: ─────────────────────────────────────────────────────────────────────
:BACKEND_H2
ECHO.
ECHO  Starting Backend with H2 In-Memory DB...
ECHO  Press Ctrl+C to stop.
ECHO.
cd /d "%PROJECT_DIR%"
"%MVN%" spring-boot:run -Dspring-boot.run.profiles=h2
PAUSE
GOTO EXIT_NOW

:: ─────────────────────────────────────────────────────────────────────
:BACKEND_POSTGRES
ECHO.
ECHO  [!] Make sure PostgreSQL is running on port 5432.
ECHO  Starting Backend with PostgreSQL...
ECHO  Press Ctrl+C to stop.
ECHO.
cd /d "%PROJECT_DIR%"
"%MVN%" spring-boot:run
PAUSE
GOTO EXIT_NOW

:: ─────────────────────────────────────────────────────────────────────
:FRONTEND
ECHO.
ECHO  Starting Frontend (Vite dev server)...
ECHO  Press Ctrl+C to stop.
ECHO.
cd /d "%PROJECT_DIR%\frontend"
npm run dev
PAUSE
GOTO EXIT_NOW

:: ─────────────────────────────────────────────────────────────────────
:EXIT_NOW
EXIT /B 0
