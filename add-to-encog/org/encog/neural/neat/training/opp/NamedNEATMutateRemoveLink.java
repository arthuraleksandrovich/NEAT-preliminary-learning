package org.encog.neural.neat.training.opp;

import org.encog.ml.ea.opp.NamedEvolutionaryOperator;

public class NamedNEATMutateRemoveLink extends NEATMutateRemoveLink implements NamedEvolutionaryOperator{
	private String name=null;
	
	public NamedNEATMutateRemoveLink(String name) {
		super();
		this.name=name;
	}
	
	@Override
	public String getPerformedOperationName() {
		return name;
	}
}
