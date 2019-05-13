package galcon;

import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import org.encog.neural.neat.NEATNetwork;


import galaxy.NEATDirector;

public class GalconScore implements CalculateScore{

	@Override
	public double calculateScore(MLMethod arg0) {
		return NEATDirector.getGameResults((NEATNetwork)arg0);
	}

	@Override
	public boolean requireSingleThreaded() {
		return true;
	}

	@Override
	public boolean shouldMinimize() {
		return false;
	}

}
