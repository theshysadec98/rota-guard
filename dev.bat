@echo off
setlocal

set "ROOT=%~dp0"
cd /d "%ROOT%"

if "%~1"=="" goto start
if /I "%~1"=="start" goto start
if /I "%~1"=="build" goto build
if /I "%~1"=="down" goto down
if /I "%~1"=="help" goto help
if /I "%~1"=="-h" goto help
if /I "%~1"=="--help" goto help

echo Lenh khong hop le: %~1
echo.
goto help

:wait_mysql_docker
echo Dang cho MySQL Docker khoi dong...
docker compose exec -T mysql mysqladmin ping -h localhost -uroot -protaguard_root >nul 2>&1
if errorlevel 1 (
  timeout /t 1 /nobreak >nul
  goto wait_mysql_docker
)
echo MySQL Docker da san sang.
goto :eof

:try_local_mysql
mysql --protocol=tcp -h 127.0.0.1 -P 3306 -u rotaguard -protaguard -e "SELECT 1" >nul 2>&1
if errorlevel 1 exit /b 1
exit /b 0

:start
call :try_local_mysql
if errorlevel 1 goto use_docker_mysql

echo Dang dung MySQL local tren may Windows...
mysql --protocol=tcp -h 127.0.0.1 -P 3306 -u rotaguard -protaguard -e "CREATE DATABASE IF NOT EXISTS rotaguard;" >nul 2>&1
if errorlevel 1 exit /b 1
type "%ROOT%be\sql\schema.sql" | mysql --protocol=tcp -h 127.0.0.1 -P 3306 -u rotaguard -protaguard rotaguard
if errorlevel 1 exit /b 1

set "ROTAGUARD_DB_URL=jdbc:mysql://127.0.0.1:3306/rotaguard?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh"
set "ROTAGUARD_DB_USERNAME=rotaguard"
set "ROTAGUARD_DB_PASSWORD=rotaguard"
cd /d "%ROOT%be"
call gradlew.bat run
exit /b %errorlevel%

:use_docker_mysql
echo Khong dung duoc MySQL local. Chuyen sang MySQL Docker...
docker compose up -d mysql
if errorlevel 1 exit /b 1

call :wait_mysql_docker

type "%ROOT%be\sql\schema.sql" | docker compose exec -T mysql mysql -urotaguard -protaguard rotaguard
if errorlevel 1 exit /b 1

set "ROTAGUARD_DB_URL=jdbc:mysql://127.0.0.1:3307/rotaguard?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh"
set "ROTAGUARD_DB_USERNAME=rotaguard"
set "ROTAGUARD_DB_PASSWORD=rotaguard"
cd /d "%ROOT%be"
call gradlew.bat run
exit /b %errorlevel%

:build
echo Dang format va build project...
cd /d "%ROOT%be"
call gradlew.bat spotlessApply build
exit /b %errorlevel%

:down
docker compose down
exit /b %errorlevel%

:help
echo RotaGuard - Script chay project cho Windows
echo.
echo Chay ung dung:
echo   dev.bat
echo.
echo Uu tien MySQL local voi tai khoan rotaguard/rotaguard.
echo Neu MySQL local khong dung duoc, script se tu mo MySQL bang Docker.
echo.
echo Lenh phu:
echo   dev.bat build
echo   dev.bat down
exit /b 0
