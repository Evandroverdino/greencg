#!/bin/bash

# Exemplo chamada: runRandomCaseStudies.sh 50 1000 100 1 12 > output/C1/random/RandomCaseStudies_C1_12-tasks.log
num_sce=$1
num_exec=$2
for ((i=1;i<=num_sce;i++))
do
        idScenario=`expr $3 + $i`
        for ((j=1;j<=num_exec;j++))
        do
                java -jar jars/RandomCaseStudies.jar $idScenario $4 output/C$4/$5/tasks_$idScenario.txt
        done
echo '########################    End Scenario !idScenario!    ##########################'
done