package galcon;

import ais.newais.NEATAI;
import util.analysis.data_generation.DefaultDataGenerator;
import util.train.TrainerConstructor;

public class GalconDataGenerator extends DefaultDataGenerator {

	public GalconDataGenerator(TrainerConstructor trainCnstr, 
			int populationSize, boolean report) {
		super(GalconScore::new, 
				trainCnstr, 
				(t, s)->{}, 
				NEATAI.NETWORK_INPUT, 
				NEATAI.NETWORK_OUTPUT, 
				populationSize, 
				report);
	}

}
