package util.analysis;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.encog.neural.neat.NEATNetwork;
import org.encog.util.obj.SerializeObject;

import util.analysis.data_generation.DataGenerator;

public class Analyzer {
	private final String dir;
	private final DataGenerator dataGenerator;
	private final DifficultyPredicting dp;
	private final LearningUniformity lu;
	private final int runs;
	
	public Analyzer(String dir, DataGenerator dataGenerator, boolean minimize, int runs, List<String> performedOperations, boolean report) {
		this.dir=dir;
		new File(dir+"bestMethods").mkdirs();
		this.dataGenerator=dataGenerator;
		new File(dir+"dp").mkdirs();
		this.dp=new DifficultyPredicting(dir+"dp"+File.separator, minimize, performedOperations, report);
		new File(dir+"lu").mkdirs();
		this.lu=new LearningUniformity(dir+"lu"+File.separator, minimize, report);
		this.runs=runs;
	}
	
	public Analyzer(String dir, DataGenerator dataGenerator, boolean minimize, int runs) {
		this(dir, dataGenerator, minimize, runs, null, true);
	}
	
	public void analyze() {
		for (int r=0; r<runs; r++) {
			RunData data=dataGenerator.generateData();
			dp.saveRawData(data.dpTrainingData, data.dpTestingData);
			lu.saveRawData(data.luData);
			saveNetwork(data.bestMethod, r);
		}
		
		dp.generateTransformedData();
		dp.trainNetwork();
		dp.computeCoefficient();
		
		lu.generateMedians();
		lu.generateCoefficients();
	}
	
	public void analyzeWithoutGenerating() {
		dp.generateTransformedData();
		dp.trainNetwork();
		dp.computeCoefficient();
		
		lu.generateMedians();
		lu.generateCoefficients();
	}
	
	private void saveNetwork(NEATNetwork network, int num) {
		try {
			SerializeObject.save(new File(dir+"bestMethods"+File.separator+num), network);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public NEATNetwork loadNetwork(int num) {
		try {
			return (NEATNetwork)SerializeObject.load(new File(dir+"bestMethods"+File.separator+num));
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
