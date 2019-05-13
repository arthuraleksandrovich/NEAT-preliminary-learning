package ais.newais;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.function.Predicate;

import galaxy.Action;
import galaxy.Fleet;
import galaxy.Planet;
import galaxy.Player;

import org.encog.neural.neat.NEATNetwork;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;

public class NEATAI extends Player{
	public static final int NETWORK_INPUT=3;
	public static final int NETWORK_OUTPUT=7;
	public static final double SEND_RATE=1;
	
	public enum STRATEGY{
		STRONGEST_ENEMY {
			@Override
			int getInt() {
				return 0;
			}
		}, 
		WEAKEST_ENEMY{
			@Override
			int getInt() {
				return 1;
			}
		}, 
		CLOSEST_ENEMY{
			@Override
			int getInt() {
				return 2;
			}
		}, 
		STRONGEST_NEUTRAL{
			@Override
			int getInt() {
				return 3;
			}
		}, 
		WEAKEST_NEUTRAL{
			@Override
			int getInt() {
				return 4;
			}
		}, 
		CLOSEST_NEUTRAL{
			@Override
			int getInt() {
				return 5;
			}
		}, 
		FOLLOW_ENEMY{
			@Override
			int getInt() {
				return 6;
			}
		};
		
		abstract int getInt();
	}
	
	private ArrayList<Planet> planets;
	private ArrayList<Fleet> fleets;
	private NEATNetwork network;
	
    public NEATAI() {
        this(Color.ORANGE);
    }

    public NEATAI(Color c) {
        super(c, "NEAT");
        setHandler(new PlayerHandler() {
            @Override
            public Collection<Action> turn(ArrayList<Fleet> fleets) {
                return makeTurn(fleets);
            }

            @Override
            public void newGame(ArrayList<Planet> newMap) {
                planets = newMap;
            }
        });
        
        network=null;
    }
    
    public void setNetwork(NEATNetwork network) {
    	this.network=network;
    }
    
    protected Collection<Action> makeTurn(ArrayList<Fleet> fleets) {
    	this.fleets=fleets;
    	
        return outputToActions(
        		network.compute(
        				generateInput()));
    }
    
    private Collection<Action> outputToActions(MLData output){
    	double[] arrayOutput=output.getData();
		
		if (arrayOutput.length!=NETWORK_OUTPUT) {
			throw new RuntimeException("network output not equals number of moves");
		}
		
		double max=arrayOutput[0];
		int n=0;
		for (int i=1; i<NETWORK_OUTPUT; i++) {
			if (arrayOutput[i]>max) {
				n=i;
				max=arrayOutput[i];
			}
		}

		Planet destination=getDestinationPlanet(n);
		if (destination==null) {
			destination=getDestinationPlanet(STRATEGY.FOLLOW_ENEMY.getInt());
		}
		
		LinkedList<Action> actions = new LinkedList<Action>();
		if (destination!=null) {
			for (Planet p: getMyPlanets(this, planets)) {
				if (p!=destination) {
					actions.add(makeAction(p, destination, (int)(SEND_RATE*p.getNumUnits())));
				}
			}
		}
		
		return actions;
    }
    
    private MLData generateInput() {
    	MLData input=new BasicMLData(NETWORK_INPUT);
    	int currentInput=0;
    	
    	input.setData(currentInput++, 
    			numUnitsDifferenceNormalized(this, planets, fleets));
    	
    	input.setData(currentInput++, 
    			productionFrequencySumDifferenceNormalized(this, planets));
    	
    	
    	input.setData(currentInput++, 1.0);
    	return input;
    }
    
    Planet getDestinationPlanet(int strategyNum) {
		switch (strategyNum) {
		case 0:
			return getStrongestEnemyPlanet(this, planets);
		case 1:
			return getWeakestEnemyPlanet(this, planets);
		case 2:
			return getClosestEnemyPlanet(this, planets);
		case 3:
			return getStrongestNeutralPlanet(planets);
		case 4:
			return getWeakestNeutralPlanet(planets);
		case 5:
			return getClosestNeutralPlanet(this, planets);
		case 6:
			return getPlanetEnemyIsSendingFleetsTo(this, fleets);
		default:
			return null;	
		}
	}
    
    
    private static int numUnitsDifference(Player me, List<Planet> planets, List<Fleet> fleets) {
       return planets.stream()
        			.collect(Collectors
        				.summingInt((u)->u.getNumUnits()*(u.ownedBy(me)?1:-1)))+
        	  fleets.stream()
        			.collect(Collectors
        				.summingInt((u)->u.getNumUnits()*(u.ownedBy(me)?1:-1)));
    }
    
    private static double numUnitsDifferenceNormalized(Player me, List<Planet> planets, List<Fleet> fleets) {
    	final double K=0.0333;
    	final double A=-5;
    	
    	return Math.tanh(K*(numUnitsDifference(me, planets, fleets)-A));
    }
    
    private static double productionFrequencySumDifference(Player me, List<Planet> planets) {
    	return planets.stream()
    				.collect(Collectors
    					.summingDouble((u)->u.getProductionFrequency()*(u.ownedBy(me)?1:-1)));
    }
    
    private static double productionFrequencySumDifferenceNormalized(Player me, List<Planet> planets) {
    	final double K=1;
    	final double A=0;
    	
    	return Math.tanh(K*(productionFrequencySumDifference(me, planets)-A));
    }
    

    
    
    private static Planet getStrongestEnemyPlanet(Player me, List<Planet> planets) {
    	return strongestPlanet(planets, (p)->!p.ownedBy(me));
    }
    
    private static Planet getWeakestEnemyPlanet(Player me, List<Planet> planets) {
    	return weakestPlanet(planets, (p)->!p.ownedBy(me));
    }
    
    private static Planet getClosestEnemyPlanet(Player me, List<Planet> planets) {
    	return closestPlanetFrom(
    			getMyPlanets(me, planets), 
    			getEnemyPlanets(me, planets));
    }
    
    private static Planet getStrongestNeutralPlanet(List<Planet> planets) {
    	return strongestPlanet(planets, Planet::isNeutral);
    }
    
    private static Planet getWeakestNeutralPlanet(List<Planet> planets) {
    	return weakestPlanet(planets, Planet::isNeutral);
    }
    
    private static Planet getClosestNeutralPlanet(Player me, List<Planet> planets) {
    	return closestPlanetFrom(
    			getMyPlanets(me, planets), 
    			getNeutralPlanets(planets));
    }
    
    private static Planet getPlanetEnemyIsSendingFleetsTo(Player me, List<Fleet> fleets) {
    	HashMap<Planet, Integer> pMap=new HashMap<>();
    	
    	for (Fleet f: fleets) {
    		pMap.put(f.DESTINATION, 
    				pMap.getOrDefault(f, 0));
    	}
    	
    	return pMap.entrySet().stream()
    			.max(Map.Entry.comparingByValue())
    			.map(Map.Entry::getKey).orElse(null);
    }
    
    
    
    
    private static Planet strongestPlanet(List<Planet> planets, Predicate<Planet> pred) {
    	return planets.stream()
				.filter(pred)
				.max(Comparator.comparing(Planet::getNumUnits))
				.orElse(null);
    }
    
    private static Planet weakestPlanet(List<Planet> planets, Predicate<Planet> pred) {
    	return planets.stream()
				.filter(pred)
				.min(Comparator.comparing(Planet::getNumUnits))
				.orElse(null);
    }
    
    private static Planet closestPlanetFrom(List<Planet> from, List<Planet> to) {
    	return to.stream()
    			.map((p)->new Pair<Planet, Double>(p, averageDistance(p, from)))
    			.min(Comparator.comparing(Pair::getValue))
    			.map(Pair::getKey).orElse(null);
    }
    
    private static double averageDistance(Planet planet, List<Planet> planets) {
    	return planets.stream()
    			.collect(Collectors
    					.averagingDouble((p)->p.distanceTo(planet)));
    }
    
    private static List<Planet> getMyPlanets(Player me, List<Planet> planets){
    	return planets.stream()
    			.filter((p)->p.ownedBy(me))
    			.collect(Collectors.toList());
    }
    
    private static List<Planet> getEnemyPlanets(Player me, List<Planet> planets){
    	return planets.stream()
    			.filter((p)->!p.ownedBy(me))
    			.collect(Collectors.toList());
    }
    
    private static List<Planet> getNeutralPlanets(List<Planet> planets){
    	return planets.stream()
    			.filter((p)->p.isNeutral())
    			.collect(Collectors.toList());
    }
}
