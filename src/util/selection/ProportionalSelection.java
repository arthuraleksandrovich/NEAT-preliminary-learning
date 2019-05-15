package util.selection;

import java.io.Serializable;
import java.util.Random;

import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.opp.selection.SelectionOperator;
import org.encog.ml.ea.species.Species;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;

public class ProportionalSelection implements SelectionOperator, Serializable {
	private static final long serialVersionUID = 1L;
	
	private final EvolutionaryAlgorithm trainer;
	
	public ProportionalSelection(EvolutionaryAlgorithm theTrainer) {
		this.trainer = theTrainer;
	}
	
	@Override
	public EvolutionaryAlgorithm getTrainer() {
		return trainer;
	}

	@Override
	public int performAntiSelection(Random rnd, Species species) {
		double maximal=species.getMembers().stream().mapToDouble((m)->m.getScore()).max().getAsDouble()+0.01;
		
		double total = 0;
		for (Genome g : species.getMembers()) {
			total+=maximal-g.getScore();
		}
		
		final double r = rnd.nextDouble() * total;
		double current = 0;
		int index=0;
		for (Genome g : species.getMembers()) {
			current+=maximal-g.getScore();
			if (r < current) {
				return index;
			}
			index++;
		}
		
		
		return index-1;
	}

	
	@Override
	public int performSelection(Random rnd, Species species) {
		double minimal=species.getMembers().stream().mapToDouble((m)->m.getScore()).min().getAsDouble()-0.01;
		
		double total = 0;
		for (Genome g : species.getMembers()) {
			total+=(g.getScore()-minimal);
		}
		
		final double r = rnd.nextDouble() * total;
		double current = 0;
		int index=0;
		for (Genome g : species.getMembers()) {
			current += (g.getScore()-minimal);
			if (r < current) {
				return index;
			}
			index++;
		}

		return index-1;
	}
}
