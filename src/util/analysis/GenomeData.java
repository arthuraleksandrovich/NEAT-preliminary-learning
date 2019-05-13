package util.analysis;

public class GenomeData {
	public static final int FIELDS=4;
	public static final int DIFF_LEVELS=3;
	
	private int ownDifficulty=-1;
	private double ownFitness;
	private String performedOperationName;
	private double firstParentRelativeFitness;
	private double secondParentRelativeFitness;
	
	public GenomeData(double ownFitness, String performedOperationName, double firstParentRelativeFitness, double secondParentRelativeFitness) {
		this.ownFitness=ownFitness;
		this.performedOperationName=performedOperationName;
		this.firstParentRelativeFitness=firstParentRelativeFitness;
		this.secondParentRelativeFitness=secondParentRelativeFitness;
	}
	
	public int getOwnDifficulty() {
		return ownDifficulty;
	}
	
	public void setOwnDifficulty(int ownDifficulty) {
		this.ownDifficulty=ownDifficulty;
	}
	
	public double getOwnFitness() {
		return ownFitness;
	}
	
	public String getPerformedOperationName() {
		return performedOperationName;
	}
	
	public double getFirstParentRelativeFitness() {
		return firstParentRelativeFitness;
	}
	
	public double getSecondParentRelativeFitness() {
		return secondParentRelativeFitness;
	}
}
