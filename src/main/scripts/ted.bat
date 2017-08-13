@echo off
:: -- Get the app home
if "%TED_HOME%" == "" set "TED_HOME=%~dp0.."

:: -- TED main class
set TED_CLASS=pw.phylame.ted.AppKt

:: -- Set extension JAR
setlocal EnableDelayedExpansion
set TED_CLASS_PATH=
for %%i in ("%TED_HOME%"\lib\*.jar) do set TED_CLASS_PATH=!TED_CLASS_PATH!;%%i
for %%i in ("%TED_HOME%"\lib\ext\*.jar) do set TED_CLASS_PATH=!TED_CLASS_PATH!;%%i
set TED_CLASS_PATH=%TED_CLASS_PATH:~1%

:: -- Run Jem SCI
start javaw -cp %TED_CLASS_PATH% %TED_CLASS% %*

endlocal
