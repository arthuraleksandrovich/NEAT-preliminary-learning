package util.analysis.data_generation;

import org.encog.ml.CalculateScore;
import org.encog.ml.ea.train.basic.BasicEA;

public interface GeneratingAction {
	public void act(BasicEA train, CalculateScore score);
}
