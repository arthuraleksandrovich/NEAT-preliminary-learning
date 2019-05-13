package util.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

public class LearningUniformity {
	private final String dir;
	private final boolean report;
	private final boolean minimize;
	
	private double[] best=null;
	private double[] worst=null;
	private double[] average=null;
	private double[] deviation=null;
	
	private double bestValue;
	private double standardDeviationMedian;
	private double bestAverageCorrelation;
	private double bestWorstCorrelation;
	
	public LearningUniformity(String dir, boolean minimize, boolean report) {
		this.dir=dir;
		this.minimize=minimize;
		this.report=report;
	}
	
	public LearningUniformity(String dir, boolean minimize) {
		this(dir, minimize, true);
	}
	
	public void saveRawData(List<double[]> data) {
		int run=getNextRunNumber();
		if (report) {System.out.println("LU: Saving data, run "+run);}
		saveRawData(data, run);
	}
	
	private int getNextRunNumber() {
		int run=0;
		
		while (Files.exists(Paths.get(dir+(run)+".csv"))) {
			run++;
		}
		
		return run;
	}
	
	public void generateMedians() {
		if (report) {System.out.println("LU: Generating medians");}
		
		loadMediansFromRawData();
		saveMedians();
	}
	
	public void generateCoefficients() {
		if (report) {System.out.println("LU: Computing coefficients");}
		if (best==null) {
			loadMedians();
		}
		
		computeCoefficients();
		if (report) {
			System.out.println("\tbest value="+bestValue);
			System.out.println("\tstandard deviation median="+standardDeviationMedian);
			System.out.println("\tbest-average correlation="+bestAverageCorrelation);
			System.out.println("\tbest-worst correlation="+bestWorstCorrelation);
		}
		
		saveCoefficients();
		if (report) {System.out.println("LU: Coefficients computed\n");}
	}
	
	private void saveRawData(List<double[]> data, int num) {
		try(PrintWriter pw=new PrintWriter(dir+num+".csv")){
			boolean first;
			for (double[] pop: data) {
				first=true;
				for (double fitness: pop) {
					if (first) {
						first=false;
					}else {
						pw.print(",");
					}
					pw.print(fitness);
				}
				pw.println();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void loadMediansFromRawData() {
		int runs=getNextRunNumber();
		
		List<List<double[]>> data=new ArrayList<>();
		for (int i=0; i<runs; i++) {
			try(Scanner sc=new Scanner(new File(dir+i+".csv"))){
				List<double[]> values;
				Iterator<List<double[]>> iterator=data.iterator();
				double[] fitnesses;
				while(sc.hasNextLine()) {
					if (i==0) {
						values=new ArrayList<>();
						//best fitness
						values.add(new double[runs]);
						//worst fitness
						values.add(new double[runs]);
						//average fitness
						values.add(new double[runs]);
						//standard deviation
						values.add(new double[runs]);
						
						data.add(values);
					}else {
						values=iterator.next();
					}
					fitnesses=Arrays.stream(sc.nextLine().split(","))
							.mapToDouble(Double::parseDouble)
							.sorted()
							.toArray();
					
					//set best fitness
					if (minimize)
						values.get(0)[i]=Arrays.stream(fitnesses).min().getAsDouble();
					else
						values.get(0)[i]=Arrays.stream(fitnesses).max().getAsDouble();
					//set worst fitness
					if (minimize)
						values.get(1)[i]=Arrays.stream(fitnesses).max().getAsDouble();
					else
						values.get(1)[i]=Arrays.stream(fitnesses).min().getAsDouble();
					//set average fitness
					double average=Arrays.stream(fitnesses).average().getAsDouble();
					values.get(2)[i]=average;
					//set standard deviation
					values.get(3)[i]=Math.sqrt(Arrays.stream(fitnesses).map((x)->Math.pow(x-average, 2)).average().getAsDouble());
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		best=data.
				stream().
				map((i)->i.get(0)).
				mapToDouble(LearningUniformity::median).
				toArray();
		
		worst=data.
				stream().
				map((i)->i.get(1)).
				mapToDouble(LearningUniformity::median).
				toArray();
		
		average=data.
				stream().
				map((i)->i.get(2)).
				mapToDouble(LearningUniformity::median).
				toArray();
		
		deviation=data.
				stream().
				map((i)->i.get(3)).
				mapToDouble(LearningUniformity::median).
				toArray();
	}
	
	private void saveMedians() {
		try(PrintWriter pw=new PrintWriter(dir+"medians.csv")){
			int len=best.length;
			for (int i=0; i<len; i++) {
				pw.println((i+1)+","+best[i]+","+average[i]+","+worst[i]+","+deviation[i]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void loadMedians() {
		try(Scanner sc=new Scanner(new File(dir+"medians.csv"))){
			List<double[]> list=new ArrayList<>();
			
			while (sc.hasNextLine()) {
				list.add(Arrays.stream(sc.nextLine().split(",")).mapToDouble(Double::parseDouble).toArray());
			}
			
			best=list.stream().mapToDouble(a->a[1]).toArray();
			average=list.stream().mapToDouble(a->a[2]).toArray();
			worst=list.stream().mapToDouble(a->a[3]).toArray();
			deviation=list.stream().mapToDouble(a->a[4]).toArray();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void computeCoefficients() {
		bestValue=bestValue(best, minimize);
		standardDeviationMedian=median(deviation);
		bestAverageCorrelation=correlation(best, average);
		bestWorstCorrelation=correlation(best, worst);
	}
	
	private static double correlation(double[] x, double[] y) {
		int n=x.length;
		if (n!=y.length) {
			throw new NumberFormatException("correlation: arrays are not equals");
		}
		if (n==0) {
			throw new NumberFormatException("correlation: arrays empty");
		}
		
		double averX=Arrays.stream(x).average().getAsDouble();
		double averY=Arrays.stream(y).average().getAsDouble();
		double cov=IntStream.range(0, n).mapToDouble((i)->(x[i]-averX)*(y[i]-averY)).sum();
		double disX=IntStream.range(0, n).mapToDouble((i)->Math.pow((x[i]-averX), 2)).sum();
		double disY=IntStream.range(0, n).mapToDouble((i)->Math.pow((y[i]-averY), 2)).sum();
		
		return cov/Math.sqrt(disX*disY);
	}
	
	private static double bestValue(double[] a, boolean minimize) {
		int percent=5;
		int n=a.length;
		if (n==0) {
			throw new NumberFormatException("correlation: arrays empty");
		}
		
		double best;
		if (minimize) {
			best=Arrays.stream(a).skip((100-percent)*n/100).min().getAsDouble();
		}else {
			best=Arrays.stream(a).skip((100-percent)*n/100).max().getAsDouble();
		}
		
		return best;
	}
	
	private static double median(double[] array) {
		Arrays.sort(array);
		int l=array.length;
		if (l%2==0) {
			return (array[l/2]+array[l/2-1])/2.0;
		}else {
			return array[l/2];
		}
	}
	
	private void saveCoefficients() {
		try(PrintWriter pw=new PrintWriter(dir+"cofficients.csv")){
			pw.println("best value,standard deviation median,best-average correlation,best-worst correlation");
			pw.println(bestValue+","+standardDeviationMedian+","+bestAverageCorrelation+","+bestWorstCorrelation);
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
