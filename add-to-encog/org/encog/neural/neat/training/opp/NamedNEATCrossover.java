package org.encog.neural.neat.training.opp;

import org.encog.ml.ea.opp.NamedEvolutionaryOperator;

public class NamedNEATCrossover extends NEATCrossover implements NamedEvolutionaryOperator{
	private String name=null;
	
	public NamedNEATCrossover(String name) {
		super();
		this.name=name;
	}
	
	@Override
	public String getPerformedOperationName() {
		return name;
	}
}
