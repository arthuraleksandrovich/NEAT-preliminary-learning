package util.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.opp.selection.SelectionOperator;
import org.encog.ml.ea.species.Species;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;

public class RankSelection implements SelectionOperator, Serializable{
private static final long serialVersionUID = 1L;
	
	private final EvolutionaryAlgorithm trainer;
	
	public RankSelection(EvolutionaryAlgorithm theTrainer) {
		this.trainer = theTrainer;
	}
	
	@Override
	public EvolutionaryAlgorithm getTrainer() {
		return trainer;
	}

	@Override
	public int performAntiSelection(Random rnd, Species species) {
		List<Pair<Genome, Integer>> genomes=new ArrayList<>();
		int index=0;
		for (Genome g: species.getMembers()) {
			genomes.add(new Pair<>(g, index));
			index++;
		}
		genomes.sort((a, b)->Double.compare(a.getKey().getScore(), b.getKey().getScore()));
		
		double total = 0;
		int rank=1;
		for (Pair<Genome, Integer> pair: genomes) {
			total+=rank++;
		}
		
		final double r = rnd.nextDouble() * total;
		double current = 0;
		rank=1;
		for (Pair<Genome, Integer> pair: genomes) {
			current += rank;
			if (r < current) {
				return pair.getValue();
			}
			rank++;
		}
		
		return genomes.get(genomes.size()-1).getValue();
	}

	@Override
	public int performSelection(Random rnd, Species species) {
		List<Pair<Genome, Integer>> genomes=new ArrayList<>();
		int index=0;
		for (Genome g: species.getMembers()) {
			genomes.add(new Pair<>(g, index));
			index++;
		}
		genomes.sort((a, b)->Double.compare(a.getKey().getScore(), b.getKey().getScore()));
		
		double total = 0;
		int rank=genomes.size();
		for (Pair<Genome, Integer> pair: genomes) {
			total+=rank--;
		}
		
		final double r = rnd.nextDouble() * total;
		double current = 0;
		rank=genomes.size();
		for (Pair<Genome, Integer> pair: genomes) {
			current += rank;
			if (r < current) {
				return pair.getValue();
			}
			rank--;
		}
		
		return genomes.get(genomes.size()-1).getValue();
	}
}
