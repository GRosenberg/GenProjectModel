@echo off
rem Generate the project files

SETLOCAL
set STARTTIME=%TIME%

set workspace=D:\DevFiles\Java\WorkSpaces\Main
set projectName=TomlTest
set packageName=net.certiv.toml
set grammarName=Toml

rem well-known locations
set ruleSet=%workspace%\GenProject\GenProjectRuleSet.json
set projConfigFile=%workspace%\%projectName%\%grammarName%GenConfig.json
set genprjar=D:\DevFiles\Java\WorkSpaces\Main\GenProject\jars\GenProject-2.1-complete.jar
set antlrjar=D:\DevFiles\Java\WorkSpaces\Main\GenProject\lib\antlr-4.5-complete.jar
set javahome=C:\Program Files\Java\jre1.8
set javapgm="%javahome%\bin\java"

set CLASSPATH=%genprjar%;%antlrjar%;%CLASSPATH%

cd /d %workspace%
%javapgm% net.certiv.antlr.project.gen.GenProject -c -g %grammarName% -n %packageName% -p %workspace%\%projectName% -r %ruleSet% %projConfigFile% 

set ENDTIME=%TIME%
set /A STARTTIME=(1%STARTTIME:~6,2%-100)*100 + (1%STARTTIME:~9,2%-100)
set /A ENDTIME=(1%ENDTIME:~6,2%-100)*100 + (1%ENDTIME:~9,2%-100)

if %ENDTIME% LSS %STARTTIME% (
	set /A DURATION=%STARTTIME%-%ENDTIME%
) else (
	set /A DURATION=%ENDTIME%-%STARTTIME%
)

set /A SECS=%DURATION% / 100
set /A REMAINDER=%DURATION% %% 100
echo %SECS%.%REMAINDER% s
ENDLOCAL

timeout 4
