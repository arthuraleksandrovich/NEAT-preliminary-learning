package org.encog.neural.neat.training.opp;

import org.encog.ml.ea.opp.NamedEvolutionaryOperator;
import org.encog.neural.neat.training.opp.links.MutateLinkWeight;
import org.encog.neural.neat.training.opp.links.SelectLinks;

public class NamedNEATMutateWeights extends NEATMutateWeights implements NamedEvolutionaryOperator{
	private String name=null;
	
	public NamedNEATMutateWeights(SelectLinks theLinkSelection, 
			MutateLinkWeight theWeightMutation, 
			String name) {
		super(theLinkSelection, theWeightMutation);

		this.name=name;
	}
	
	@Override
	public String getPerformedOperationName() {
		return name;
	}
}
