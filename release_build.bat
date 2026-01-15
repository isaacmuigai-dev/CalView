@echo off
echo Setting JAVA_HOME...
if exist "C:\Program Files\Android\Android Studio\jbr" (
    set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
) else if exist "C:\Program Files\Android\Android Studio\jre" (
    set "JAVA_HOME=C:\Program Files\Android\Android Studio\jre"
) else (
    echo "Could not find JBR or JRE in Android Studio installation."
    exit /b 1
)

echo Using JAVA_HOME: %JAVA_HOME%
call .\gradlew.bat clean :app:bundleRelease
