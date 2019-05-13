package util.analysis;

import java.util.List;

import org.encog.neural.neat.NEATNetwork;

public class RunData {
	public final List<List<GenomeRawData>> dpTrainingData;
	public final List<List<GenomeRawData>> dpTestingData;
	public final List<double[]> luData;
	public final NEATNetwork bestMethod;
	
	public RunData(List<List<GenomeRawData>> dpTrainingData, List<List<GenomeRawData>> dpTestingData, List<double[]> luData, NEATNetwork bestMethod) {
		this.dpTrainingData=dpTrainingData;
		this.dpTestingData=dpTestingData;
		this.luData=luData;
		this.bestMethod=bestMethod;
	}
}
