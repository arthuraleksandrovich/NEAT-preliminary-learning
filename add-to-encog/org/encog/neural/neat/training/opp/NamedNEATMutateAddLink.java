package org.encog.neural.neat.training.opp;

import org.encog.ml.ea.opp.NamedEvolutionaryOperator;

public class NamedNEATMutateAddLink extends NEATMutateAddLink implements NamedEvolutionaryOperator{
	private String name=null;
	
	public NamedNEATMutateAddLink(String name) {
		super();
		this.name=name;
	}
	
	@Override
	public String getPerformedOperationName() {
		return name;
	}
}
