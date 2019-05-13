package mapmakers;

import java.util.LinkedList;

import galaxy.Coords;
import galaxy.GameSettings;
import galaxy.Player;
import galaxy.MapMaker;

public class PreGeneratedRandomMapMaker extends MapMaker{
	private static final int NUM_PLANETS = GameSettings.NUM_PLANETS, MAX_RADIUS = GameSettings.MAX_RADIUS,
            MIN_RADIUS = GameSettings.MIN_RADIUS, MAX_NEUTRAL_UNITS = GameSettings.MAX_NEUTRAL_UNITS,
            MIN_PRODUCE_TIME = GameSettings.MIN_PRODUCE_TIME, MAX_PRODUCE_TIME = GameSettings.MAX_PRODUCE_TIME;
	
	private static final Coords DIMENSIONS = GameSettings.DIMENSIONS;
	
	private static final PlanetData[][] planetDataArray;
	private static int mapCount;
	private static final int PLAYER_COUNT=2;
	
	static {
		mapCount=GameSettings.NUM_ROUNDS;
		if (GameSettings.REVERSE_EACH_MAP) {
			mapCount=(mapCount+1)/2;
		}
		
		planetDataArray=new PlanetData[mapCount][NUM_PLANETS];
		generateAllPlanets();
	}
	
	private int index;
	
	public PreGeneratedRandomMapMaker() {
		super();
		
		index=0;
	}
	
	@Override
    protected void makeMap(LinkedList<Player> active) {
		int p=0;
		PlanetData data;
		
		for (int i=0; i<PLAYER_COUNT; i++) {
			data=planetDataArray[index][p++];
			makePlanet(
					active.get(i), 
					data.numUnits, 
					data.radius, 
					data.prodTime, 
					data.coords);
		}

        for (int i = active.size(); i < NUM_PLANETS; i++) {
        	data=planetDataArray[index][p++];
        	makePlanet(
					data.owner, 
					data.numUnits, 
					data.radius, 
					data.prodTime, 
					data.coords);
        }
        
        index=(index+1)%mapCount;
    }
	
	protected static void generateAllPlanets() {
		for (int m=0; m<mapCount; m++) {
			int p=0;
			
			for (int i=0; i<PLAYER_COUNT; i++) {
				planetDataArray[m][p++]=generateStartingPlanet(m);
			}

			for (int i = PLAYER_COUNT; i < NUM_PLANETS; i++) {
				planetDataArray[m][p++]=generateRandomPlanet(m);
			}
		}
    }
	
	private static PlanetData generateRandomPlanet(int mapNum) {
        int numUnits = (int)(Math.random() * MAX_NEUTRAL_UNITS);
        int radius = (int)(Math.random() * (MAX_RADIUS - MIN_RADIUS) + MIN_RADIUS);
        int prodTime = (int)((1 - ((double)radius - MIN_RADIUS) / (MAX_RADIUS - MIN_RADIUS))
                * (MAX_PRODUCE_TIME - MIN_PRODUCE_TIME) + MIN_PRODUCE_TIME);
        Coords coords = makeLocation(mapNum, radius);
        
        return new PlanetData(null, numUnits, radius, prodTime, coords);
    }

    private static PlanetData generateStartingPlanet(int mapNum) {
    	return new PlanetData(null, 100, MAX_RADIUS, MIN_PRODUCE_TIME, makeLocation(mapNum, MAX_RADIUS));
    }
    
    private static Coords makeLocation(int mapNum, int radius) {
        double[] coords = DIMENSIONS.getCoords();
        do {
            for (int i = 0; i < coords.length; i++) {
                coords[i] = Math.random() * (coords[i] - radius * 2) + radius;
            }
        } while (checkOverlappingPlanets(mapNum, radius, coords));
        
        return new Coords(coords);
    }
    
    protected static boolean checkOverlappingPlanets(int mapNum, int radius, double... coords) {
        for (PlanetData data: planetDataArray[mapNum]) {
        	if (data==null) {
        		break;
        	}else {
        		if (data.coords.distanceTo(coords) < radius + data.radius + 10) {
                    return true;
                }
        	}
        }
        
        return false;
    }
}
