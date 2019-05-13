package util.train;

import org.encog.ml.CalculateScore;
import org.encog.ml.ea.opp.CompoundOperator;
import org.encog.ml.ea.opp.NamedCompoundOperator;
import org.encog.ml.ea.opp.selection.TournamentSelection;
import org.encog.ml.ea.opp.selection.TruncationSelection;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.neural.hyperneat.HyperNEATCODEC;
import org.encog.neural.neat.NEATCODEC;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.opp.NamedNEATCrossover;
import org.encog.neural.neat.training.opp.NamedNEATMutateAddLink;
import org.encog.neural.neat.training.opp.NamedNEATMutateAddNode;
import org.encog.neural.neat.training.opp.NamedNEATMutateRemoveLink;
import org.encog.neural.neat.training.opp.NamedNEATMutateWeights;
import org.encog.neural.neat.training.opp.links.MutatePerturbLinkWeight;
import org.encog.neural.neat.training.opp.links.MutateResetLinkWeight;
import org.encog.neural.neat.training.opp.links.SelectFixed;
import org.encog.neural.neat.training.opp.links.SelectProportion;
import org.encog.neural.neat.training.species.OriginalNEATSpeciation;

import util.selection.ProportionalSelection;
import util.selection.RankSelection;

public class TrainerConstructorUtil {
	public static enum SELECTION{
		TRUNCATION, TOURNAMENT, PROPORTIONAL, RANK
	}
	
	public static enum WEIGHT_MUTATION{
		DEFAULT, DEFAULT_MINIMIZED
	}
	
	public static enum OPERATIONS{
		DEFAULT, CROSSOVER, WEIGTH_MUTATION, STRUCTURE_MUTATIONS
	}
	
	public static TrainEA constructNEATTrainer(final NEATPopulation population,
				final CalculateScore calculateScore, 
				WEIGHT_MUTATION weightMutationType, SELECTION selectionType, OPERATIONS operationsType, 
				boolean calculateScoreAgain) {
		final TrainEA result = instantiateTrain(population, calculateScore);
		result.setCalculateScoreAgain(calculateScoreAgain);
		
		switch (selectionType) {
		case RANK:
			result.setSelection(new RankSelection(result));
			break;
		case TOURNAMENT:
			result.setSelection(new TournamentSelection(result, 5));
			break;
		case TRUNCATION:
			result.setSelection(new TruncationSelection(result, 0.3));
			break;
		case PROPORTIONAL:
		default:
			result.setSelection(new ProportionalSelection(result));
			break;
		}

		CompoundOperator weightMutation=getDefaultWeightMutation();
		
		switch(weightMutationType) {
		case DEFAULT_MINIMIZED:
			weightMutation=getMinimizedDefaultWeightMutation();
			break;
		case DEFAULT:
		default:
			weightMutation=getDefaultWeightMutation();
			break;
		}
		
		switch(operationsType) {
		case CROSSOVER:
			setOperationsWithMorePossibleCrossover(result, weightMutation);
			break;
		case STRUCTURE_MUTATIONS:
			setOperationsWithMorePosibleStructureMutations(result, weightMutation);
			break;
		case WEIGTH_MUTATION:
			setOperationsWithMorePossibleWeightMutation(result, weightMutation);
			break;
		case DEFAULT:
		default:
			setDefaultOperations(result, weightMutation);
			break;
		}

		if (population.isHyperNEAT()) {
			result.setCODEC(new HyperNEATCODEC());
		} else {
			result.setCODEC(new NEATCODEC());
		}

		return result;
	}
	
	private static TrainEA instantiateTrain(final NEATPopulation population,
			final CalculateScore calculateScore) {
		final TrainEA result = new TrainEA(population, calculateScore);
		
		OriginalNEATSpeciation speciation=new OriginalNEATSpeciation();
		speciation.setMaxNumberOfSpecies(population.size()/6);
		result.setSpeciation(speciation);
		
		return result;
	}
	
	private static CompoundOperator getDefaultWeightMutation() {
		final CompoundOperator weightMutation = new NamedCompoundOperator();
		weightMutation.getComponents().add(
				0.1125,
				new NamedNEATMutateWeights(new SelectFixed(1),
						new MutatePerturbLinkWeight(0.02), 
						"NamedNEATMutateWeights_SelectFixed1_MutatePerturbLinkWeight0.02"));
		weightMutation.getComponents().add(
				0.1125,
				new NamedNEATMutateWeights(new SelectFixed(2),
						new MutatePerturbLinkWeight(0.02), 
						"NamedNEATMutateWeights_SelectFixed2_MutatePerturbLinkWeight0.02"));
		weightMutation.getComponents().add(
				0.1125,
				new NamedNEATMutateWeights(new SelectFixed(3),
						new MutatePerturbLinkWeight(0.02), 
						"NamedNEATMutateWeights_SelectFixed3_MutatePerturbLinkWeight0.02"));
		weightMutation.getComponents().add(
				0.1125,
				new NamedNEATMutateWeights(new SelectProportion(0.02),
						new MutatePerturbLinkWeight(0.02), 
						"NamedNEATMutateWeights_SelectProportion0.02_MutatePerturbLinkWeight0.02"));
		weightMutation.getComponents().add(
				0.1125,
				new NamedNEATMutateWeights(new SelectFixed(1),
						new MutatePerturbLinkWeight(1), 
						"NamedNEATMutateWeights_SelectFixed1_MutatePerturbLinkWeight1"));
		weightMutation.getComponents().add(
				0.1125,
				new NamedNEATMutateWeights(new SelectFixed(2),
						new MutatePerturbLinkWeight(1), 
						"NamedNEATMutateWeights_SelectFixed2_MutatePerturbLinkWeight1"));
		weightMutation.getComponents().add(
				0.1125,
				new NamedNEATMutateWeights(new SelectFixed(3),
						new MutatePerturbLinkWeight(1), 
						"NamedNEATMutateWeights_SelectFixed3_MutatePerturbLinkWeight1"));
		weightMutation.getComponents().add(
				0.1125,
				new NamedNEATMutateWeights(new SelectProportion(0.02),
						new MutatePerturbLinkWeight(1), 
						"NamedNEATMutateWeights_SelectProportion0.02_MutatePerturbLinkWeight1"));
		weightMutation.getComponents().add(
				0.03,
				new NamedNEATMutateWeights(new SelectFixed(1),
						new MutateResetLinkWeight(), 
						"NamedNEATMutateWeights_SelectFixed1_MutateResetLinkWeight"));
		weightMutation.getComponents().add(
				0.03,
				new NamedNEATMutateWeights(new SelectFixed(2),
						new MutateResetLinkWeight(), 
						"NamedNEATMutateWeights_SelectFixed2_MutateResetLinkWeight"));
		weightMutation.getComponents().add(
				0.03,
				new NamedNEATMutateWeights(new SelectFixed(3),
						new MutateResetLinkWeight(), 
						"NamedNEATMutateWeights_SelectFixed3_MutateResetLinkWeight"));
		weightMutation.getComponents().add(
				0.01,
				new NamedNEATMutateWeights(new SelectProportion(0.02),
						new MutateResetLinkWeight(), 
						"NamedNEATMutateWeights_SelectProportion0.02_MutateResetLinkWeight"));
		weightMutation.getComponents().finalizeStructure();
		
		return weightMutation;
	}
	
	private static CompoundOperator getMinimizedDefaultWeightMutation() {
		final CompoundOperator weightMutation = new NamedCompoundOperator();

		weightMutation.getComponents().add(
				0.45,
				new NamedNEATMutateWeights(new SelectFixed(3),
						new MutatePerturbLinkWeight(0.02), 
						"NamedNEATMutateWeights_SelectFixed2_MutatePerturbLinkWeight0.02"));

		weightMutation.getComponents().add(
				0.45,
				new NamedNEATMutateWeights(new SelectFixed(3),
						new MutatePerturbLinkWeight(1), 
						"NamedNEATMutateWeights_SelectFixed3_MutatePerturbLinkWeight1"));
		
		weightMutation.getComponents().add(
				0.1,
				new NamedNEATMutateWeights(new SelectFixed(3),
						new MutateResetLinkWeight(), 
						"NamedNEATMutateWeights_SelectFixed3_MutateResetLinkWeight"));
		
		weightMutation.getComponents().finalizeStructure();
		
		return weightMutation;
	}
	
	private static void setDefaultOperations(final TrainEA train, CompoundOperator weightMutation) {
		train.setChampMutation(weightMutation);
		train.addOperation(0.5, new NamedNEATCrossover("NEATCrossover"));
		train.addOperation(0.494, weightMutation);
		train.addOperation(0.0005, new NamedNEATMutateAddNode("NEATMutateAddNode"));
		train.addOperation(0.005, new NamedNEATMutateAddLink("NEATMutateAddLink"));
		train.addOperation(0.0005, new NamedNEATMutateRemoveLink("NEATMutateRemoveLink"));
		train.getOperators().finalizeStructure();
	}
	
	private static void setOperationsWithMorePossibleCrossover(final TrainEA train, CompoundOperator weightMutation) {
		train.setChampMutation(weightMutation);
		train.addOperation(0.7, new NamedNEATCrossover("NEATCrossover"));
		train.addOperation(0.294, weightMutation);
		train.addOperation(0.0005, new NamedNEATMutateAddNode("NEATMutateAddNode"));
		train.addOperation(0.005, new NamedNEATMutateAddLink("NEATMutateAddLink"));
		train.addOperation(0.0005, new NamedNEATMutateRemoveLink("NEATMutateRemoveLink"));
		train.getOperators().finalizeStructure();
	}
	
	private static void setOperationsWithMorePossibleWeightMutation(final TrainEA train, CompoundOperator weightMutation) {
		train.setChampMutation(weightMutation);
		train.addOperation(0.2, new NamedNEATCrossover("NEATCrossover"));
		train.addOperation(0.794, weightMutation);
		train.addOperation(0.0005, new NamedNEATMutateAddNode("NEATMutateAddNode"));
		train.addOperation(0.005, new NamedNEATMutateAddLink("NEATMutateAddLink"));
		train.addOperation(0.0005, new NamedNEATMutateRemoveLink("NEATMutateRemoveLink"));
		train.getOperators().finalizeStructure();
	}
	
	private static void setOperationsWithMorePosibleStructureMutations(final TrainEA train, CompoundOperator weightMutation) {
		train.setChampMutation(weightMutation);
		train.addOperation(0.3, new NamedNEATCrossover("NEATCrossover"));
		train.addOperation(0.294, weightMutation);
		train.addOperation(0.0805, new NamedNEATMutateAddNode("NEATMutateAddNode"));
		train.addOperation(0.245, new NamedNEATMutateAddLink("NEATMutateAddLink"));
		train.addOperation(0.0805, new NamedNEATMutateRemoveLink("NEATMutateRemoveLink"));
		train.getOperators().finalizeStructure();
	}
	
	public static TrainEA constructNEATTrainer1(final NEATPopulation population,
			final CalculateScore calculateScore) {
		final TrainEA result = new TrainEA(population, calculateScore);
		
		OriginalNEATSpeciation speciation=new OriginalNEATSpeciation();
		speciation.setMaxNumberOfSpecies(population.size()/3);
		result.setSpeciation(speciation);

		result.setSelection(new TruncationSelection(result, 0.3));
		final CompoundOperator weightMutation = new NamedCompoundOperator();
		weightMutation.getComponents().add(
				0.4,
				new NamedNEATMutateWeights(new SelectProportion(0.02),
						new MutatePerturbLinkWeight(0.02), "MutatePerturbLinkWeight0.02"));
		weightMutation.getComponents().add(
				0.4,
				new NamedNEATMutateWeights(new SelectProportion(0.02),
						new MutatePerturbLinkWeight(1), "MutatePerturbLinkWeight1"));
		weightMutation.getComponents().add(
				0.2,
				new NamedNEATMutateWeights(new SelectProportion(0.02),
						new MutateResetLinkWeight(), "MutateResetLinkWeigth"));
		weightMutation.getComponents().finalizeStructure();

		result.setChampMutation(weightMutation);
		result.addOperation(0.5, new NamedNEATCrossover("NEATCrossover"));
		result.addOperation(0.494, weightMutation);
		result.addOperation(0.0005, new NamedNEATMutateAddNode("NEATMutateAddNode"));
		result.addOperation(0.005, new NamedNEATMutateAddLink("NEATMutateAddLink"));
		result.addOperation(0.0005, new NamedNEATMutateRemoveLink("NEATMutateRemoveLink"));
		result.getOperators().finalizeStructure();

		if (population.isHyperNEAT()) {
			result.setCODEC(new HyperNEATCODEC());
		} else {
			result.setCODEC(new NEATCODEC());
		}

		return result;
	}
	
	public static TrainEA constructNEATTrainer2(final NEATPopulation population,
			final CalculateScore calculateScore) {
		final TrainEA result = new TrainEA(population, calculateScore);
		
		OriginalNEATSpeciation speciation=new OriginalNEATSpeciation();
		speciation.setMaxNumberOfSpecies(population.size()/6);
		result.setSpeciation(speciation);

		result.setSelection(new RankSelection(result));
		final CompoundOperator weightMutation = new NamedCompoundOperator();
		weightMutation.getComponents().add(
				0.8,
				new NamedNEATMutateWeights(new SelectProportion(0.02),
						new MutatePerturbLinkWeight(0.05), "MutatePerturbLinkWeight0.02"));
		weightMutation.getComponents().add(
				0.2,
				new NamedNEATMutateWeights(new SelectProportion(0.02),
						new MutateResetLinkWeight(), "MutateResetLinkWeigth"));
		weightMutation.getComponents().finalizeStructure();

		result.setChampMutation(weightMutation);
		result.addOperation(0.4, new NamedNEATCrossover("NEATCrossover"));
		result.addOperation(0.594, weightMutation);
		result.addOperation(0.001, new NamedNEATMutateAddNode("NEATMutateAddNode"));
		result.addOperation(0.004, new NamedNEATMutateAddLink("NEATMutateAddLink"));
		result.addOperation(0.001, new NamedNEATMutateRemoveLink("NEATMutateRemoveLink"));
		result.getOperators().finalizeStructure();

		if (population.isHyperNEAT()) {
			result.setCODEC(new HyperNEATCODEC());
		} else {
			result.setCODEC(new NEATCODEC());
		}

		return result;
	}
}
