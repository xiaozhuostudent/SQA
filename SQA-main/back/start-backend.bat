@echo off
echo Stopping existing Java process...
taskkill /F /IM java.exe 2>nul

echo Starting backend service...
cd /d "%~dp0"
start "Backend Service" java -jar target\back-0.0.1-SNAPSHOT.war

echo Backend service started!
echo You can check the terminal window for logs.
pause
