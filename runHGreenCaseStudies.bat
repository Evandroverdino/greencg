@ECHO OFF
SET NUM_EXEC=%1
FOR /L %%i IN (1,1,%NUM_EXEC%) DO (
SET idScenario=%2
SET /A idScenario = !idScenario! + %%i
java -jar jars\HGreenCaseStudies.jar !idScenario! %3 %4
)
REM Abrir "cmd /v"
REM Exemplo chamada: runHGreenCaseStudies.bat 50 100 2 24 > HGreenCaseStudies.log