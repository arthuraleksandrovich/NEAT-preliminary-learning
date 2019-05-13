package util.analysis;

public class GenomeRawData {
	public final double ownFitness;
	public final boolean fitnessOnly;
	public final String performedOperator;
	public final double firstParentFitness;
	public final double secondParentFitness;
	
	public GenomeRawData(double ownFitness) {
		this.ownFitness=ownFitness;
		this.fitnessOnly=true;
		this.performedOperator=null;
		this.firstParentFitness=0;
		this.secondParentFitness=0;
	}
	
	public GenomeRawData(double ownFitness, String performedOperator, double firstParentFitness, double secondParentFitness) {
		this.ownFitness=ownFitness;
		this.fitnessOnly=false;
		this.performedOperator=performedOperator;
		this.firstParentFitness=firstParentFitness;
		this.secondParentFitness=secondParentFitness;
	}
}
