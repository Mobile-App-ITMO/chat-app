@echo off
echo Chat Application Setup
echo.

REM Get local IP address
for /f "tokens=2 delims=:" %%i in ('ipconfig ^| findstr "IPv4" ^| findstr /v "169.254"') do (
    set IP=%%i
    goto :found_ip
)
:found_ip
set IP=%IP:~1%

if "%IP%"=="" set IP=10.0.2.2
echo Detected IP: %IP%

REM Create/update app.properties
(
echo # Example app.properties file for local testing
echo.
echo android.server.url=http://%IP%:8080
echo android.turn.url=turn:%IP%:3478
echo android.turn.username=user
echo android.turn.credential=pass
echo.
echo web.server.url=http://localhost:8080
echo web.turn.url=turn:localhost:3478
echo.
echo desktop.server.url=http://localhost:8080
echo.
echo android.ai.url=http://%IP%:11434
echo desktop.ai.url=http://localhost:11434
echo web.ai.url=http://localhost:11434
) > app.properties

echo Configuration updated.
echo Server URL: http://%IP%:8080
echo AI URL: http://%IP%:11434
echo.
echo Run: gradlew clean build
pause