package galaxy;

import java.util.Arrays;
import java.util.stream.Collectors;

import galaxy.Director;
import galaxy.GameSettings;
import ais.newais.NEATAI;

import org.encog.neural.neat.NEATNetwork;

public class NEATDirector extends Director{
	private double scoreSum;
	private NEATAI neatPlayer;
	
	public NEATDirector() {
		super();
		
		scoreSum=0;
		
		neatPlayer=null;
		for (Player p: players) {
			if (p instanceof NEATAI) {
				neatPlayer=(NEATAI)p;
				
				break;
			}
		}
	}
	
	public void setNetwork(NEATNetwork network) {
		neatPlayer.setNetwork(network);
	}
	
	public static double getGameResults(NEATNetwork network) {
		NEATDirector director = new NEATDirector();
		
		director.setNetwork(network);
    	
        while (!director.done()) {
        	director.next();
        }
        
        return director.scoreSum/GameSettings.NUM_ROUNDS;
	}
	
	@Override
	void finishGame(Player winner, Planet[] newMap) {
		scoreSum+=Arrays.stream(players)
						.collect(Collectors
		    					.summingDouble((p)->galaxy.numUnitsOwnedBy(p)*(p==neatPlayer?1:-1)));
				
		super.finishGame(winner, newMap);
	}
}
