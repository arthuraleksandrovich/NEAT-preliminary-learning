package org.encog.ml.ea.opp;

import java.util.Random;

import org.encog.ml.ea.genome.Genome;

public class NamedCompoundOperator extends CompoundOperator implements NamedEvolutionaryOperator{
	private String name=null;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void performOperation(final Random rnd, final Genome[] parents,
			final int parentIndex, final Genome[] offspring,
			final int offspringIndex) {
		final EvolutionaryOperator opp = this.getComponents().pick(rnd);
		opp.performOperation(rnd, parents, parentIndex, offspring,
				offspringIndex);
		
		if (opp instanceof NamedEvolutionaryOperator) {
			name=((NamedEvolutionaryOperator)opp).getPerformedOperationName();
		}else {
			name=null;
		}
	}
	
	@Override
	public String getPerformedOperationName() {
		return name;
	}
	
}
