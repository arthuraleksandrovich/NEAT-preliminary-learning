package util.train;

import org.encog.ml.CalculateScore;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.neural.neat.NEATPopulation;

public interface TrainerConstructor {
	public TrainEA constructNEATTrainer(final NEATPopulation population,
			final CalculateScore calculateScore);
}
