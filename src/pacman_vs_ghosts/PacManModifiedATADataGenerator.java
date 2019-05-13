package pacman_vs_ghosts;

import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.training.NEATGenome;

import pacman.controllers.examples.NearestPillPacMan;
import util.analysis.data_generation.DefaultDataGenerator;
import util.train.TrainerConstructor;

public class PacManModifiedATADataGenerator extends DefaultDataGenerator {

	public PacManModifiedATADataGenerator(TrainerConstructor trainCnstr, 
			int populationSize, boolean reverse, int testTeamSize, boolean report) {
		super(()->new PacmanScore(
					new NearestPillPacMan(), 
					reverse, testTeamSize), 
				trainCnstr, 
				(train, score)->{
					NEATGenome genome=(NEATGenome)train.getBestGenome();
					NEATNetwork bestNetwork=(NEATNetwork)train.getCODEC().decode(genome);
					((PacmanScore)score).setBestNetwork(bestNetwork);
				}, 
				NEATGhosts.NETWORK_INPUT, 
				NEATGhosts.NETWORK_OUTPUT, 
				populationSize, 
				report);
	}

}
