# Readme #
This repository is a part of Arthur Aleksandrovich’s bachelor work and contains testing programme’s source files written in Java of programme for experiments doing.    
This project uses/contains code from these sources: 
* https://github.com/encog/encog-java-core
* https://github.com/davidrobles/pacman-vs-ghosts
* https://github.com/alelazar/galcon
### Installation ###
1. Copy file from directory src to your java project
2. Download beforementioned packages and add to your project’s build path. 
3. Add/replace files from add-to-encog directory to Encog package. 
4. Add/replace files from add-to-galcon directory to Galcon package. 
5. Copy assets and setting directories from Galcon package to your project’s root. 
6. Copy data package directory from Pac-Man package to your project’s root.
7. Replace SETTINGS file. 
### How to use ###
Main file location is NEAT-preliminary-learning/src/experiment/RunExperiment.java. 
Execution parameters of experiments:
1.	java -jar ./preliminary.jar ./experiments/ 20 false tictactoe 100 weightMutationOperatorsSize default
2.	java -jar ./preliminary.jar ./experiments/ 20 false tictactoe 100 weightMutationOperatorsSize minimized
3.	java -jar ./preliminary.jar ./experiments/ 20 false tictactoe 25 populationSize
4.	java -jar ./preliminary.jar ./experiments/ 20 false tictactoe 50 populationSize
5.	java -jar ./preliminary.jar ./experiments/ 20 false tictactoe 200 populationSize
6.	java -jar ./preliminary.jar ./experiments/ 20 false tictactoe 100 selectionType truncation
7.	java -jar ./preliminary.jar ./experiments/ 20 false tictactoe 100 selectionType tournament
8.	java -jar ./preliminary.jar ./experiments/ 20 false tictactoe 100 selectionType rank
9.	java -jar ./preliminary.jar ./experiments/ 20 false tictactoe 100 operatorsPossibilities crossover
10.	java -jar ./preliminary.jar ./experiments/ 20 false tictactoe 100 operatorsPossibilities weightMutation
11.	java -jar ./preliminary.jar ./experiments/ 20 false tictactoe 100 operatorsPossibilities structureMutations
12.	java -jar ./preliminary.jar ./experiments/ 20 false pacman ATA 100 weightMutationOperatorsSize default
13.	java -jar ./preliminary.jar ./experiments/ 20 false pacman ATA 100 weightMutationOperatorsSize minimized
14.	java -jar ./preliminary.jar ./experiments/ 20 false pacman ATA 25 populationSize
15.	java -jar ./preliminary.jar ./experiments/ 20 false pacman ATA 50 populationSize
16.	java -jar ./preliminary.jar ./experiments/ 20 false pacman ATA 200 populationSize
17.	java -jar ./preliminary.jar ./experiments/ 20 false pacman ATA 100 selectionType truncation
18.	java -jar ./preliminary.jar ./experiments/ 20 false pacman ATA 100 selectionType tournament
19.	java -jar ./preliminary.jar ./experiments/ 20 false pacman ATA 100 selectionType rank
20.	java -jar ./preliminary.jar ./experiments/ 20 false pacman ATA 100 operatorsPossibilities crossover
21.	java -jar ./preliminary.jar ./experiments/ 20 false pacman ATA 100 operatorsPossibilities weightMutation
22.	java -jar ./preliminary.jar ./experiments/ 20 false pacman ATA 100 operatorsPossibilities structureMutations
23.	java -jar ./preliminary.jar ./experiments/ 20 true pacman ATARandom 100 weightMutationOperatorsSize default
24.	java -jar ./preliminary.jar ./experiments/ 20 false pacman modifiedATA3 100 weightMutationOperatorsSize default
25.	java -jar ./preliminary.jar ./experiments/ 20 false pacman modifiedATA2 100 weightMutationOperatorsSize default
26.	java -jar ./preliminary.jar ./experiments/ 20 true galcon 100 weightMutationOperatorsSize default
27.	java -jar ./preliminary.jar ./experiments/ 20 true galcon 100 weightMutationOperatorsSize minimized
28.	java -jar ./preliminary.jar ./experiments/ 20 true galcon 25 populationSize
29.	java -jar ./preliminary.jar ./experiments/ 20 true galcon 50 populationSize
30.	java -jar ./preliminary.jar ./experiments/ 20 true galcon 200 populationSize
31.	java -jar ./preliminary.jar ./experiments/ 20 true galcon 100 selectionType truncation
32.	java -jar ./preliminary.jar ./experiments/ 20 true galcon 100 selectionType tournament
33.	java -jar ./preliminary.jar ./experiments/ 20 true galcon 100 selectionType rank
34.	java -jar ./preliminary.jar ./experiments/ 20 true galcon 100 operatorsPossibilities crossover
35.	java -jar ./preliminary.jar ./experiments/ 20 true galcon 100 operatorsPossibilities weightMutation
36.	java -jar ./preliminary.jar ./experiments/ 20 true galcon 100 operatorsPossibilities structureMutations
