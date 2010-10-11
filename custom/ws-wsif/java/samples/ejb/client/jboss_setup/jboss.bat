@echo off

REM Written by Nirmal Mukhi and Alek Slominski
REM Purpose of script: add JBoss client JARs to the classpath 
REM so that WSIF EJB sample can be run using JBoss J2EE client jars

REM check for JBoss home
if "%JBOSS_HOME%" == "" goto jbosshomeerror
set JBOSS_HOME=%JBOSS_HOME%

REM setup classpath to run WSIF sample using JBoss EJB client JARs
call classpath.bat quiet

REM add to JBOss client JARs to classpath
for %%i in (%JBOSS_HOME%\client\*.jar) do call lcp.bat %%i

REM set the classpath
set CLASSPATH=%LOCALCLASSPATH%

echo %CLASSPATH%

goto end

:jbosshomeerror
echo "ERROR: JBOSS_HOME not found in your environment."
echo "Please, set the JBOSS_HOME variable in your environment to match the"
echo "location of your JBoss installation."

:end
