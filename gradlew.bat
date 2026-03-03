@rem Gradle startup script for Windows
@if "%DEBUG%"=="" @echo off
setlocal
set GRADLE_OPTS=
set APP_HOME=%~dp0
set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
"%JAVA_HOME%\bin\java.exe" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
endlocal
