@echo off
rem
rem This script set classpath
rem 	Usage: classpath [run|build] [set] [queit]
rem By default it sets CLASSPATH to execute direct java invocations (all included)
rem option build prepares LOCALCLASSPATH to use in build.bat
rem option build prepares LOCALCLASSPATH to use in run.bat
rem by using set CLASSPATH also be set by run|build
rem by using queit no echo of set CLASSOATH well be visible
rem
rem written by Aleksander Slominski [http://www.extreme.indiana.edu/~aslom]

set LOCALCLASSPATH=
rem changes below made by Nirmal Mukhi, resulting from new lib directory structure
rem for %%i in (lib\junit\*.jar) do call lcp.bat %%i
rem for %%i in (lib\wsdl4j\*.jar) do call lcp.bat %%i

rem for %%i in (lib\log4j\*.jar) do call lcp.bat %%i
rem for %%i in (lib\xerces2\*.jar) do call lcp.bat %%i

rem for %%i in (lib\jms_api\*.jar) do call lcp.bat %%i

rem for %%i in (lib\j2ee\*.jar) do call lcp.bat %%i

rem for %%i in (lib\apache_soap\*.jar) do call lcp.bat %%i
rem for %%i in (lib\javamail\*.jar) do call lcp.bat %%i
rem for %%i in (lib\activation\*.jar) do call lcp.bat %%i

rem for %%i in (lib\axis\*.jar) do call lcp.bat %%i
rem for %%i in (lib\commons_discovery\*.jar) do call lcp.bat %%i
rem for %%i in (lib\commons_logging\*.jar) do call lcp.bat %%i
rem for %%i in (lib\jaxrpc\*.jar) do call lcp.bat %%i
rem for %%i in (lib\saaj\*.jar) do call lcp.bat %%i

rem for %%i in (lib\soaprmi11\*.jar) do call lcp.bat %%i
for %%i in (lib\*.jar) do call lcp.bat %%i

if "%1" == "build" goto build_classpath
if "%1" == "run" goto run_classpath
if "%1" == "clean" goto clean_classpath

REM otherwise set user classpath

REM set CLASSPATH=%LOCALCLASSPATH%

REM if "%1" == "quiet" goto end

REM echo %CLASSPATH%

REM goto end

goto run_classpath


REM --------------------------
:clean_classpath
set CLASSPATH=
set LOCALCLASSPATH=

if "%2" == "quiet" goto end

echo set CLASSPATH=%CLASSPATH%
echo set LOCALCLASSPATH=%LOCALCLASSPATH%

goto end

REM ------------------------
:build_classpath
rem for %%i in (lib\ant\*.jar) do call lcp.bat %%i
if exist %JAVA_HOME%\lib\tools.jar set LOCALCLASSPATH=%LOCALCLASSPATH%;%JAVA_HOME%\lib\tools.jar

goto extra_args


REM -----------------------
:run_classpath
REM set LOCALCLASSPATH=build\api;build\classes;build\samples;build\tests;%LOCALCLASSPATH%
set LOCALCLASSPATH=build\classes;build\samples;build\tests;%LOCALCLASSPATH%
for %%i in (build\lib\*.jar) do call lcp.bat %%i


:extra_args

if not "%1" == "" goto set_classpath
if not "%1" == "set" goto set_classpath
if not "%2" == "set" goto check_echo

:set_classpath

set CLASSPATH=%LOCALCLASSPATH%

:check_echo

if "%1" == "quiet" goto end
if "%2" == "quiet" goto end
if "%3" == "quiet" goto end

echo %LOCALCLASSPATH%

goto end

:end
