package org.encog.neural.neat.training.opp;

import org.encog.ml.ea.opp.NamedEvolutionaryOperator;

public class NamedNEATMutateAddNode extends NEATMutateAddNode implements NamedEvolutionaryOperator{
	private String name=null;
	
	public NamedNEATMutateAddNode(String name) {
		super();
		this.name=name;
	}
	
	@Override
	public String getPerformedOperationName() {
		return name;
	}
}
