package experiment;

import java.io.File;
import java.util.Arrays;

import org.encog.ml.CalculateScore;
import org.encog.neural.neat.NEATNetwork;

import galaxy.NEATDirector;
import galcon.GalconDataGenerator;
import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman_vs_ghosts.NEATGhosts;
import pacman_vs_ghosts.PacManATADataGenerator;
import pacman_vs_ghosts.PacManModifiedATADataGenerator;
import tic_tac_toe.TicTacToeDataGenerator;
import util.analysis.Analyzer;
import util.analysis.data_generation.DataGenerator;
import util.analysis.data_generation.DefaultDataGenerator;
import util.train.TrainerConstructor;
import util.train.TrainerConstructorUtil;
import static util.train.TrainerConstructorUtil.*;


public class RunExperiment {
	private static final int MIN_POPULATION=1;
	private static final int MAX_POPULATION=10000;
	
	private static final int MIN_RUNS=0;
	private static final int MAX_RUNS=100;
	
	public static void main(String args[]) {
		DefaultDataGenerator.setParameters(
				500, 
				250, 
				5, 
				255, 
				3);
		try {
			boolean runGame=args[0].equalsIgnoreCase("runGame");
			boolean withHuman=Boolean.parseBoolean(args[2]);
			
			Analyzer analyzer=constructAnalyzer(args, 0, true);
			if (runGame) {
				analyzer=constructAnalyzer(Arrays.copyOfRange(args, 2, args.length), 0, true);
			}else {
				analyzer=constructAnalyzer(args, 0, true);
			}
			
			if (runGame) {
				runGame(args[5], analyzer, Integer.parseInt(args[6]), withHuman);
			}else {
				analyzer.analyze();
				//analyzer.analyzeWithoutGenerating();
			}
			
		}catch(IndexOutOfBoundsException e) {
			System.err.println("wrong number of arguments");
		}catch(IllegalArgumentException e) {
			System.out.println("wrong argument: "+e.getMessage());
		}
	}
	
	private static Analyzer constructAnalyzer(String args[], int counter, boolean report) 
			throws IllegalArgumentException, IndexOutOfBoundsException{

		String dir=args[counter++];
		int runs=0;
		try {
			runs=Integer.parseInt(args[counter++]);
			if (runs<MIN_RUNS ||runs>MAX_RUNS) {
				throw new NumberFormatException();
			}
		}catch(NumberFormatException e) {
			throw new IllegalArgumentException("number of runs \""+args[counter-1]+"\" is not allowed");
		}
		
		String folder="";
		
		boolean calculateScoreAgain=Boolean.parseBoolean(args[counter++]);
		
		String game=args[counter++];
		switch(game.toLowerCase()) {
		case "tictactoe":
		case "pacman":
		case "galcon":
			folder=folder+game+File.separator;
			break;
		default: 
			throw new IllegalArgumentException("Game \""+game+"\" is not defined");
		}
		
		String gameRegime=null;
		if (game.equalsIgnoreCase("pacman")) {
			gameRegime=args[counter++];
			switch (gameRegime) {
			case "ATA":
			case "ATARandom":
			case "modifiedATA3":
			case "modifiedATA2":
			case "modifiedATA3Random":
			case "modifiedATA2Random":
				folder=folder+gameRegime+File.separator;
				break;
			default:
				throw new IllegalArgumentException("PacMan game regime \""+gameRegime+"\" is not defined");
			}
		}
		
		TrainerConstructor trainCnstr;
		int populationSize;
		try {
			populationSize=Integer.parseInt(args[counter++]);
			if (populationSize<MIN_POPULATION || populationSize>MAX_POPULATION) {
				throw new NumberFormatException();
			}
		}catch(NumberFormatException e) {
			throw new IllegalArgumentException("population size value \""+args[counter-1]+"\" is not allowed");
		}
		
		String experimentType=args[counter++];
		
		String subtype;
		switch(experimentType) {
		case "weightMutationOperatorsSize": 
			folder=folder+"weigthMutation"+File.separator;
			
			subtype=args[counter++];
			switch (subtype) {
			case "default": 
				trainCnstr=(p, s)->{
					return TrainerConstructorUtil.constructNEATTrainer(p, s, 
							WEIGHT_MUTATION.DEFAULT, SELECTION.PROPORTIONAL, OPERATIONS.DEFAULT, 
							calculateScoreAgain);
				};
				break;
			case "minimized": 
				trainCnstr=(p, s)->{
					return TrainerConstructorUtil.constructNEATTrainer(p, s, 
							WEIGHT_MUTATION.DEFAULT_MINIMIZED, SELECTION.PROPORTIONAL, OPERATIONS.DEFAULT, 
							calculateScoreAgain);
				};
				break;
			default:
				throw new IllegalArgumentException("weight mutation \""+args[counter-1]+"\" is not defined");
			}
			
			folder=folder+subtype+File.separator;
			
			break;
		case "populationSize": 
			folder=folder+"population"+File.separator+Integer.toString(populationSize)+File.separator;
			
			trainCnstr=(p, s)->{
				return TrainerConstructorUtil.constructNEATTrainer(p, s, 
						WEIGHT_MUTATION.DEFAULT, SELECTION.PROPORTIONAL, OPERATIONS.DEFAULT, 
						calculateScoreAgain);
			};
			
			break;
		case "selectionType":
			folder=folder+"selection"+File.separator;
			
			subtype=args[counter++];
			switch(subtype) {
			case "truncation":
				trainCnstr=(p, s)->{
					return TrainerConstructorUtil.constructNEATTrainer(p, s, 
							WEIGHT_MUTATION.DEFAULT, SELECTION.TRUNCATION, OPERATIONS.DEFAULT, 
							calculateScoreAgain);
				};
				break;
			case "tournament":
				trainCnstr=(p, s)->{
					return TrainerConstructorUtil.constructNEATTrainer(p, s, 
							WEIGHT_MUTATION.DEFAULT, SELECTION.TOURNAMENT, OPERATIONS.DEFAULT, 
							calculateScoreAgain);
				};
				break;
			case "proportional":
				trainCnstr=(p, s)->{
					return TrainerConstructorUtil.constructNEATTrainer(p, s, 
							WEIGHT_MUTATION.DEFAULT, SELECTION.PROPORTIONAL, OPERATIONS.DEFAULT, 
							calculateScoreAgain);
				};
				break;
			case "rank":
				trainCnstr=(p, s)->{
					return TrainerConstructorUtil.constructNEATTrainer(p, s, 
							WEIGHT_MUTATION.DEFAULT, SELECTION.RANK, OPERATIONS.DEFAULT, 
							calculateScoreAgain);
				};
				break;
			default:
				throw new IllegalArgumentException("selection \""+args[counter-1]+"\" is not defined");
			}
			
			folder=folder+subtype+File.separator;
			
			break;
		case "operatorsPossibilities":
			folder=folder+"operators"+File.separator;
			
			subtype=args[counter++];
			switch(subtype) {
			case "default":
				trainCnstr=(p, s)->{
					return TrainerConstructorUtil.constructNEATTrainer(p, s, 
							WEIGHT_MUTATION.DEFAULT, SELECTION.PROPORTIONAL, OPERATIONS.DEFAULT, 
							calculateScoreAgain);
				};
				break;
			case "crossover":
				trainCnstr=(p, s)->{
					return TrainerConstructorUtil.constructNEATTrainer(p, s, 
							WEIGHT_MUTATION.DEFAULT, SELECTION.PROPORTIONAL, OPERATIONS.CROSSOVER, 
							calculateScoreAgain);
				};
				break;
			case "weightMutation":
				trainCnstr=(p, s)->{
					return TrainerConstructorUtil.constructNEATTrainer(p, s, 
							WEIGHT_MUTATION.DEFAULT, SELECTION.PROPORTIONAL, OPERATIONS.WEIGTH_MUTATION, 
							calculateScoreAgain);
				};
				break;
			case "structureMutations":
				trainCnstr=(p, s)->{
					return TrainerConstructorUtil.constructNEATTrainer(p, s, 
							WEIGHT_MUTATION.DEFAULT, SELECTION.PROPORTIONAL, OPERATIONS.STRUCTURE_MUTATIONS, 
							calculateScoreAgain);
				};
				break;
			default:
				throw new IllegalArgumentException("operators \""+args[counter-1]+"\" is not defined");
			}
			
			folder=folder+subtype+File.separator;
			
			break;
		default: 
			throw new IllegalArgumentException("experiment type \""+experimentType+"\" is not defined");
		}
		
		DataGenerator dataGenerator=null;
		boolean minimize=false;
		switch(game.toLowerCase()) {
		case "tictactoe":
			minimize=false;
			dataGenerator=new TicTacToeDataGenerator(trainCnstr, populationSize, report);
			break;
		case "pacman":
			minimize=true;
			switch(gameRegime) {
			case "ATA":
				dataGenerator=new PacManATADataGenerator(trainCnstr, populationSize, false, report);
				break;
			case "ATARandom":
				dataGenerator=new PacManATADataGenerator(trainCnstr, populationSize, true, report);
				break;
			case "modifiedATA3":
				dataGenerator=new PacManModifiedATADataGenerator(trainCnstr, populationSize, false, 3, report);
				break;
			case "modifiedATA2":
				dataGenerator=new PacManModifiedATADataGenerator(trainCnstr, populationSize, false, 2, report);
				break;
			case "modifiedATA3Random":
				dataGenerator=new PacManModifiedATADataGenerator(trainCnstr, populationSize, true, 3, report);
				break;
			case "modifiedATA2Random":
				dataGenerator=new PacManModifiedATADataGenerator(trainCnstr, populationSize, true, 2, report);
				break;
			}
			break;
		case "galcon":
			minimize=false;
			dataGenerator=new GalconDataGenerator(trainCnstr, populationSize, report);
			break;
		}
		
		return new Analyzer(dir+folder, dataGenerator, minimize, runs, null, report);
	}
	
	private static void runGame(String game, Analyzer analyzer, int run, boolean withHuman) {
		NEATNetwork network=analyzer.loadNetwork(0);
		
		switch(game.toLowerCase()) {
		case "tictactoe":
			tic_tac_toe.gui.GameWindow.runGame(network, withHuman);
			break;
		case "pacman":
			NEATGhosts ghosts=new NEATGhosts();
			NEATNetwork[] networks=new NEATNetwork[GHOST.values().length];
			for (int i=0; i<GHOST.values().length; i++) {
				networks[i]=network;
			}
			ghosts.setGhostsNetworks(networks);
			
			Controller<MOVE> pacman;
			if (withHuman) {
				pacman=new pacman.controllers.HumanController(new pacman.controllers.KeyBoardInput());
			}else {
				pacman=new pacman.controllers.examples.NearestPillPacMan();
			}
			
			Executor exec=new Executor();
			exec.runGameTimed(pacman, ghosts, true);
			break;
		case "galcon":
			galaxy.GameSettings.setProperty("visualizer", "twodimensiondefault.DefaultVisualizer");
			galaxy.GameSettings.setProperty("frametime", "10");
			galaxy.GameSettings.setProperty("numrounds", "2");
			
			NEATDirector.getGameResults(network);
			break;
		}
	}
}
