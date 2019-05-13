package util.analysis.data_generation;

import java.util.ArrayList;
import java.util.List;

import org.encog.Encog;
import org.encog.ml.CalculateScore;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.train.basic.BasicEA;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATGenome;

import util.analysis.GenomeRawData;
import util.analysis.RunData;
import util.train.TrainerConstructor;

public class DefaultDataGenerator implements DataGenerator{
	private static int iters=100;
	private static int iterTrainFrom=50;
	private static int itersTrain=5;
	private static int iterTestFrom=55;
	private static int itersTest=3;
	
	private final ScoreConstructor scoreCnstr;
	private final TrainerConstructor trainCnstr;
	private final GeneratingAction action;
	private final int inputNodes;
	private final int outputNodes;
	private final int populationSize;
	
	private final boolean report;
	
	public DefaultDataGenerator(ScoreConstructor scoreCnstr, TrainerConstructor trainCnstr, GeneratingAction action, 
			int inputNodes, int outputNodes, int populationSize, boolean report) {
		this.scoreCnstr=scoreCnstr;
		this.trainCnstr=trainCnstr;
		this.action=action;
		this.inputNodes=inputNodes;
		this.outputNodes=outputNodes;
		this.populationSize=populationSize;
		this.report=report;
	}
	
	public static void setParameters(int iterations, int iterationDPTrainingFrom, int iterationsDPTraining, 
			int iterationDPTestingFrom, int iterationsDPTesting) {
		iters=iterations;
		iterTrainFrom=iterationDPTrainingFrom;
		itersTrain=iterationsDPTraining;
		iterTestFrom=iterationDPTestingFrom;
		itersTest=iterationsDPTesting;
	}
	
	@Override
	public RunData generateData() {
		NEATPopulation pop=new NEATPopulation(inputNodes, outputNodes, populationSize);
		pop.setInitialConnectionDensity(1.0);
		pop.reset();
		
		final CalculateScore score=scoreCnstr.construct();
		final BasicEA train=trainCnstr.constructNEATTrainer(pop, score);
		
		List<List<GenomeRawData>> dpTraining=new ArrayList<>();
		List<List<GenomeRawData>> dpTesting=new ArrayList<>();
		List<double[]> lu=new ArrayList<>();
		do {
			train.iteration(); 
			
			if (report) {System.out.println("\t\titeration "+train.getIteration()+"; species: "+pop.getSpecies().size());}
			
			lu.add(pop.getSpecies().stream().flatMap((s)->s.getMembers().stream()).mapToDouble((g)->g.getScore()).toArray());
			
			boolean training=train.getIteration()>=iterTrainFrom && train.getIteration()<iterTrainFrom+itersTrain;
			boolean testing=train.getIteration()>=iterTestFrom && train.getIteration()<iterTestFrom+itersTest;
			
			if (training || testing) {
				List<GenomeRawData> list=new ArrayList<>();
				if (training) {
					dpTraining.add(list);
				}else {
					dpTesting.add(list);
				}
				
				for (Genome g: train.getChildrenTriedToAdd()) {
					NEATGenome ng=(NEATGenome)g;
					
					GenomeRawData grd;
					String name=ng.pollPerformedOperationName();
					
					if (name!=null) {
						NEATGenome[] parents=ng.pollParents();
						double fScore=parents[0].getScore();
						double sScore=(parents.length==1)?0:parents[1].getScore();
						grd=new GenomeRawData(ng.getScore(), name, fScore, sScore);
					}else {
						grd=new GenomeRawData(ng.getScore());
					}
					list.add(grd);
				}
			}
			
			action.act(train, score);
		}while(train.getIteration()<iters);
		
		NEATNetwork bestMethod=(NEATNetwork)train.getCODEC().decode(train.getBestGenome());
		
		Encog.getInstance().shutdown();
		
		return new RunData(dpTraining, dpTesting, lu, bestMethod);
	}
	
}
