package tic_tac_toe;

import tic_tac_toe.tree.Node;
import tic_tac_toe.util.TicTacToeScore;
import util.analysis.data_generation.DefaultDataGenerator;
import util.train.TrainerConstructor;

public class TicTacToeDataGenerator extends DefaultDataGenerator {

	public TicTacToeDataGenerator( TrainerConstructor trainCnstr, 
			int populationSize, boolean report) {
		super(TicTacToeScore::new, 
				trainCnstr, 
				(p, s)->{}, 
				Node.NETWORK_INPUT, 
				Node.NETWORK_OUTPUT, 
				populationSize, 
				report);
	}

}
