@ECHO OFF
SET NUM_EXEC=%1
FOR /L %%i IN (1,1,%NUM_EXEC%) DO (
SET idScenario=%2
SET /A idScenario = !idScenario! + %%i
java -jar jars\METCaseStudies.jar !idScenario! %3 output/C%3/%4/tasks_!idScenario!.txt
)
REM Abrir "cmd /v"
REM Exemplo chamada: runMETCaseStudies.bat 50 100 1 48 > METCaseStudies.log