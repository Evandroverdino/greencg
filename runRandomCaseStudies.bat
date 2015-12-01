@ECHO OFF
SET NUM_SCE=%1
SET NUM_EXEC=%2
FOR /L %%i IN (1,1,%NUM_SCE%) DO (
SET idScenario=%3
SET /A idScenario = !idScenario! + %%i
FOR /L %%j IN (1,1,%NUM_EXEC%) DO (
java -jar jars\RandomCaseStudies.jar !idScenario! %4 output/C%4/%5/tasks_!idScenario!.txt
)
echo ########################    End Scenario !idScenario!    ##########################
)
REM Abrir "cmd /v"
REM Exemplo chamada: runRandomCaseStudies.bat 50 1000 100 1 12 > output\C1\random\RandomCaseStudies_C1_12-tasks.log