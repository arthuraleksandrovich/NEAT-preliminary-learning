package util.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


import org.encog.ConsoleStatusReportable;
import org.encog.Encog;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.VersatileMLDataSet;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.columns.ColumnType;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.model.EncogModel;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.csv.CSVFormat;
import org.encog.util.csv.ReadCSV;
import org.encog.util.obj.SerializeObject;
import org.encog.util.simple.EncogUtility;

public class DifficultyPredicting {
	private static final String transformedDataFileName="Transformed";
	private static final String networkFileName="network";
	private static final String normalizationHelperFileName="normHelper";
	private static final String performedOpsFileName="perfOp";
	
	private final String dir;
	private final boolean report;
	private final boolean minimize;
	private List<String> performedOperations;
	
	public DifficultyPredicting(String dir, boolean minimize, List<String> performedOperations, boolean report) {
		this.dir=dir;
		this.minimize=minimize;
		this.performedOperations=performedOperations;
		this.report=report;
	}
	
	public DifficultyPredicting(String dir, boolean minimize) {
		this(dir, minimize, null, true);
	}
	
	private void generateTransformedData(boolean training) {
	
		List<GenomeData> data=loadRawData(training);
		
		if (performedOperations==null) {
			readPerformedOperations(data);
			savePerformedOperations();
		}
		
		saveTransformedData(data, training);
	}
	
	public void saveRawData(List<List<GenomeRawData>> training, List<List<GenomeRawData>> testing) {
		int run=getNextRunNumber();
		if (report) {System.out.println("DP: Saving data, run "+run);}
		saveRawData(training, run, true);
		saveRawData(testing, run, false);
	}
	
	public void generateTransformedData() {
		if (report) {System.out.println("DP: Generating transformed data");}
		generateTransformedData(true);
		generateTransformedData(false);
	}
	
	private void readPerformedOperations(List<GenomeData> data){
		HashSet<String> set=new HashSet<>();
		for (GenomeData gd: data) {
			set.add(gd.getPerformedOperationName());
		}
		
		performedOperations=new ArrayList<String>(set);
	}
	
	private int getNextRunNumber() {
		int run=0;
		
		while (Files.exists(Paths.get(dir+"tr"+(run)+".csv"))) {
			run++;
		}
		
		return run;
	}
	
	public void trainNetwork() {
		loadPerformedOperations();
		
		PrintStream defaultStream=System.out;
		try {
			if (report) {defaultStream.println("DP: Training network");}
			
			System.setOut(new PrintStream(dir+"trOutput.txt"));
			
			// Download the data that we will attempt to model.
			File dataFile = new File(dir+"tr"+transformedDataFileName+".csv");
			
			// Define the format of the data file.
			// This area will change, depending on the columns and 
			// format of the file that you are trying to model.
			VersatileDataSource source = new CSVDataSource(dataFile, false,
					CSVFormat.DECIMAL_POINT);
			VersatileMLDataSet data = new VersatileMLDataSet(source);
			int col=0;
			for (String op: performedOperations) {
				data.defineSourceColumn(op, col++, ColumnType.continuous);
			}
			data.defineSourceColumn("firstParent", col++, ColumnType.continuous);
			data.defineSourceColumn("secondParent", col++, ColumnType.continuous);
			
			// Define the column that we are trying to predict.
			ColumnDefinition outputColumn = data.defineSourceColumn("difficulty", col++,
					ColumnType.nominal);
			
			// Analyze the data, determine the min/max/mean/sd of every column.
			data.analyze();
			
			// Map the prediction column to the output of the model, and all
			// other columns to the input.
			data.defineSingleOutputOthersInput(outputColumn);
			
			// Create feedforward neural network as the model type. MLMethodFactory.TYPE_FEEDFORWARD.
			// You could also other model types, such as:
			// MLMethodFactory.SVM:  Support Vector Machine (SVM)
			// MLMethodFactory.TYPE_RBFNETWORK: RBF Neural Network
			// MLMethodFactor.TYPE_NEAT: NEAT Neural Network
			// MLMethodFactor.TYPE_PNN: Probabilistic Neural Network
			EncogModel model = new EncogModel(data);
			model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);
			
			// Send any output to the console.
			model.setReport(new ConsoleStatusReportable());
			
			// Now normalize the data.  Encog will automatically determine the correct normalization
			// type based on the model you chose in the last step.
			data.normalize();
			
			// Hold back some data for a final validation.
			// Shuffle the data into a random ordering.
			// Use a seed of 1001 so that we always use the same holdback and will get more consistent results.
			model.holdBackValidation(0.3, true, 1001);
			
			// Choose whatever is the default training type for this model.
			model.selectTrainingType(data);
			
			// Use a 5-fold cross-validated train.  Return the best method found.
			MLRegression bestMethod = (MLRegression)model.crossvalidate(5, true);

			// Display the training and validation errors.
			System.out.println( "Training error: " + EncogUtility.calculateRegressionError(bestMethod, model.getTrainingDataset()));
			System.out.println( "Validation error: " + EncogUtility.calculateRegressionError(bestMethod, model.getValidationDataset()));
			
			// Display our normalization parameters.
			NormalizationHelper helper = data.getNormHelper();
			System.out.println(helper.toString());
			
			// Display the final model.
			System.out.println("Final model: " + bestMethod);
			
			// Loop over the entire, original, dataset and feed it through the model.
			// This also shows how you would process new data, that was not part of your
			// training set.  You do not need to retrain, simply use the NormalizationHelper
			// class.  After you train, you can save the NormalizationHelper to later
			// normalize and denormalize your data.
			ReadCSV csv = new ReadCSV(dataFile, false, CSVFormat.DECIMAL_POINT);
			String[] line = new String[performedOperations.size()+2];
			MLData input = helper.allocateInputVector();
			
			int counter=0;
			long error=0;
			long randomError=0;
			Random rand=new Random();
			while(csv.next()) {
				StringBuilder result = new StringBuilder();
				for (int i=0; i<line.length; i++) {
					line[i]=csv.get(i);
				}
				String correct = csv.get(line.length);
				helper.normalizeInputVector(line,input.getData(),false);
				MLData output = bestMethod.compute(input);
				String difficultyChosen = helper.denormalizeOutputVectorToString(output)[0];
				
				error+=Math.abs(Integer.parseInt(correct)-Integer.parseInt(difficultyChosen));
				randomError+=Math.abs(Integer.parseInt(correct)-(rand.nextInt(3)+1));
				counter++;
				
				result.append(Arrays.toString(line));
				result.append(" -> predicted: ");
				result.append(difficultyChosen);
				result.append("(correct: ");
				result.append(correct);
				result.append(")");
				
				System.out.println(result.toString());
			}
			
			saveNetwork(bestMethod);
			saveNormHelper(helper);
			
			// shut down.
			Encog.getInstance().shutdown();

			if (report) {
				System.out.println("coefficient="+error/2.0/counter);
				System.out.println("random coefficient="+randomError/2.0/counter);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}finally {
			System.setOut(defaultStream);
			if (report) {System.out.println("DP: Network trained\n");}
		}
	}
	
	public double computeCoefficient() {
		loadPerformedOperations();
		
		double coefficient=-1;
		
		PrintStream defaultStream=System.out;
		try {
			if (report) {defaultStream.println("DP: Computing coefficient");}
			
			System.setOut(new PrintStream(dir+"tsOutput.txt"));
			// Download the data that we will attempt to model.
			File dataFile = new File(dir+"ts"+transformedDataFileName+".csv");
			
			MLRegression bestMethod=loadNetwork();
			NormalizationHelper helper=loadNormHelper();
			
			ReadCSV csv = new ReadCSV(dataFile, false, CSVFormat.DECIMAL_POINT);
			String[] line = new String[performedOperations.size()+2];
			MLData input = helper.allocateInputVector();
			
			
			int counter=0;
			long error=0;
			long randomError=0;
			Random rand=new Random();
			while(csv.next()) {
				StringBuilder result = new StringBuilder();
				for (int i=0; i<line.length; i++) {
					line[i]=csv.get(i);
				}
				String correct = csv.get(line.length);
				helper.normalizeInputVector(line,input.getData(),false);
				MLData output = bestMethod.compute(input);
				String difficultyChosen = helper.denormalizeOutputVectorToString(output)[0];
				
				error+=Math.abs(Integer.parseInt(correct)-Integer.parseInt(difficultyChosen));
				randomError+=Math.abs(Integer.parseInt(correct)-(rand.nextInt(3)+1));
				counter++;
				
				result.append(Arrays.toString(line));
				result.append(" -> predicted: ");
				result.append(difficultyChosen);
				result.append("(correct: ");
				result.append(correct);
				result.append(")");
				
				System.out.println(result.toString());
			}
			
			// shut down.
			Encog.getInstance().shutdown();
			
			coefficient=error/2.0/counter;
			if (report) {
				System.out.println("coefficient="+coefficient);
				System.out.println("random coefficient="+randomError/2.0/counter);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}finally {
			System.setOut(defaultStream);
			if (report) {
				System.out.println("\tcoefficient="+coefficient);
				System.out.println("DP: Coefficient computed\n");
			}
		}
		
		return coefficient;
	}
	
	private void saveTransformedData(List<GenomeData> data, boolean training) {
		final String fileName=((training)?"tr":"ts")+transformedDataFileName;
		
		try(PrintWriter pw=new PrintWriter(dir+fileName+".csv")){
			for (GenomeData gd: data) {
				int operIndex=performedOperations.indexOf(gd.getPerformedOperationName());
				int size=performedOperations.size();
				for (int i=0; i<size; i++) {
					if (i==operIndex) {
						pw.print("1,");
					}else {
						pw.print("0,");
					}
				}
				
				pw.println(
						gd.getFirstParentRelativeFitness()+","+gd.getSecondParentRelativeFitness()+","+
						gd.getOwnDifficulty());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void savePerformedOperations() {
		try(PrintWriter pw=new PrintWriter(dir+performedOpsFileName+".csv")){
			boolean first=true;
			for (String op: performedOperations) {
				if (first) {
					first=false;
				}else {
					pw.print(",");
				}
				pw.print(op);
			}
			pw.println();
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void loadPerformedOperations() {
		if (performedOperations==null) {
			try (Scanner sc=new Scanner(new File(dir+performedOpsFileName+".csv"))){
				performedOperations=Arrays.asList(sc.nextLine().split(","));
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void saveRawData(List<List<GenomeRawData>> data, int num, boolean training) {
		try(PrintWriter pw=new PrintWriter(dir+((training)?"tr":"ts")+num+".csv")){
			for (List<GenomeRawData> pop: data) {
				for (GenomeRawData gen: pop) {
					pw.print(gen.ownFitness);
					if (gen.fitnessOnly) {
						pw.println();
					}else {
						pw.println(","+gen.performedOperator+","+gen.firstParentFitness+","+gen.secondParentFitness);
					}
				}
				pw.println();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private List<GenomeData> loadRawData(boolean training){
		
		ArrayList<GenomeData> result=new ArrayList<>();
		
		
		boolean fileExists=true;
		int run=0;
		
		while (fileExists) {
			File file=new File(dir+((training)?"tr":"ts")+(run++)+".csv");
			if (!file.exists()) {
				fileExists=false;
				continue;
			}
			
			try(Scanner sc=new Scanner(file)){
				ArrayList<GenomeData> data=new ArrayList<>();
				boolean firstPopulation=true;
				boolean nextPopulation=true;
				double minPreviousPop=Integer.MAX_VALUE;
				double maxPreviousPop=Integer.MIN_VALUE;
				ArrayList<Double> currentPopFitnesses=null;
				double minCurrentPop=Integer.MAX_VALUE;
				double maxCurrentPop=Integer.MIN_VALUE;
				int populationFrom=-1;
				int counter=0;
				
				
				while (sc.hasNextLine()) {
					if (nextPopulation) {
						populationFrom=counter;
						nextPopulation=false;
						minCurrentPop=Integer.MAX_VALUE;
						maxCurrentPop=Integer.MIN_VALUE;
						currentPopFitnesses=new ArrayList<>();
					}
					
					String[] array=sc.nextLine().split(",");
					
					double ownFitness=0;
					if (!array[0].isEmpty()){
						ownFitness=Double.parseDouble(array[0]);
						currentPopFitnesses.add(ownFitness);
						minCurrentPop=Math.min(minCurrentPop, ownFitness);
						maxCurrentPop=Math.max(maxCurrentPop, ownFitness);
					}
					
					if (!firstPopulation && array.length>1) {
						String name=array[1];
						
						double firstFitness=normalize(Double.parseDouble(array[2]), 
								minPreviousPop, maxPreviousPop);
						
						double secondFitness=0;
						if (array.length==GenomeData.FIELDS) {
							secondFitness=normalize(Double.parseDouble(array[3]), 
									minPreviousPop, maxPreviousPop);
						}
						
						if (!firstPopulation) {
							data.add(new GenomeData(ownFitness, name, firstFitness, secondFitness));
							counter++;
						}
					}else if (array[0].isEmpty()){
						nextPopulation=true;
						
						double[] fitnessLimitsCurrentPop=new double[GenomeData.DIFF_LEVELS-1];
						if (minimize) {
							Collections.sort(currentPopFitnesses, (d1, d2)->-Double.compare(d1, d2));
						}else {
							Collections.sort(currentPopFitnesses);
						}
						for (int d=1; d<GenomeData.DIFF_LEVELS; d++) {
							fitnessLimitsCurrentPop[d-1]=currentPopFitnesses.get(
									d*(currentPopFitnesses.size()-1)/GenomeData.DIFF_LEVELS);
						}
						
						if (firstPopulation) {
							firstPopulation=false;
						}else {
							for (int g=populationFrom; g<counter; g++) {
								data.get(g).setOwnDifficulty(
										fitnessToDifficulty(data.get(g).getOwnFitness(), fitnessLimitsCurrentPop));
							}
						}
						
						minPreviousPop=minCurrentPop;
						maxPreviousPop=maxCurrentPop;
					}
				}
				
				result.addAll(data);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	private void saveNetwork(MLRegression network) {
		EncogDirectoryPersistence.saveObject(new File(dir+networkFileName), network);
	}
	
	private MLRegression loadNetwork() {
		return (MLRegression)EncogDirectoryPersistence.loadObject(new File(dir+networkFileName));
	}
	
	private void saveNormHelper(NormalizationHelper normHelper) {
		try {
			SerializeObject.save(new File(dir+normalizationHelperFileName), normHelper);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private NormalizationHelper loadNormHelper() {
		try {
			return (NormalizationHelper)SerializeObject.load(new File(dir+normalizationHelperFileName));
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static double normalize(double x, double datal, double datah) {
		final double norml=0;
		final double normh=1;
		if (x<datal) return norml;
		if (x>datah) return normh;
		return (x-datal)*(normh-norml)/(datah-datal)+norml;
	}
	
	private static int fitnessToDifficulty(double fitness, double[] limits) {
		int difficulty=GenomeData.DIFF_LEVELS;
		for (int d=1; d<GenomeData.DIFF_LEVELS; d++) {
			if (fitness<=limits[d-1]) {
				difficulty=d;
			}
		}
		
		return difficulty;
	}
}
