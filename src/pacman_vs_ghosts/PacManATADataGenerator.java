package pacman_vs_ghosts;
import pacman.controllers.examples.StarterPacMan;
import util.analysis.data_generation.DefaultDataGenerator;
import util.train.TrainerConstructor;

public class PacManATADataGenerator extends DefaultDataGenerator{
	public PacManATADataGenerator(TrainerConstructor trainCnstr, 
			int populationSize, boolean reverse, boolean report) {
		super(()->new PacmanScore(
					new StarterPacMan(), 
					reverse, -1),
				trainCnstr, 
				(t, s)->{}, 
				NEATGhosts.NETWORK_INPUT, 
				NEATGhosts.NETWORK_OUTPUT, 
				populationSize, 
				report);
	}
}
