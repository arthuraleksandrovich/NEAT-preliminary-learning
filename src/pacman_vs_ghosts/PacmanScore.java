package pacman_vs_ghosts;

import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import org.encog.neural.neat.NEATNetwork;

import pacman.game.Game;
import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import static pacman.game.Constants.AWARD_LIFE_LEFT;

import java.util.Random;

public class PacmanScore implements CalculateScore{

	private int mazeIndex=0;
	private int seed;
	//number of trials
	private int trials=5;
	private Controller<MOVE> pacManController;
	
	private boolean withoutReverse=false;
	
	private boolean useBestNetwork=false;
	private int testTeamSize=0;
	private NEATNetwork bestNetwork;
	private boolean bestNetworkSet=false;
	
	public PacmanScore(Controller<MOVE> pcControl, boolean reverse, int testTeamSize) {
		pacManController=pcControl;
		withoutReverse=!reverse;
		if (testTeamSize<0 || testTeamSize>=4) {
			setUseBestNetwork(false, 4);
		}else {
			setUseBestNetwork(true, testTeamSize);
		}
	}
	
	public void setPacManController(Controller<MOVE> controller) {
		this.pacManController=controller;
	}
	
	public void setWithoutReverse(boolean b) {
		withoutReverse=b;
	}
	
	public void setUseBestNetwork(boolean b, int testTeamSize) {
		useBestNetwork=b;
		this.testTeamSize=testTeamSize;
		bestNetworkSet=false;
	}
	
	public void setBestNetwork(NEATNetwork bestNetwork) {
		this.bestNetwork=bestNetwork;
		bestNetworkSet=true;
	}
	
	@Override
	public double calculateScore(MLMethod network) {
		double averageScore=0;
		
		Random rand=new Random(seed);
		
		Game game;
		
		NEATGhosts ghostsController=getGhostsController((NEATNetwork)network);
		
		for (int i=0; i<trials; i++) {
			game=new Game(rand.nextLong(), mazeIndex);
			
			while(!game.gameOver() && (game.getMazeIndex()==mazeIndex)) {
				if (withoutReverse) {
					game.advanceGameWithoutReverse(pacManController.getMove(game.copy(), -1), 
							ghostsController.getMove(game.copy(), -1));
				}else {
					game.advanceGame(pacManController.getMove(game.copy(), -1), 
							ghostsController.getMove(game.copy(), -1));
				}
			}
			
			
			averageScore+=game.getScore();
			
			averageScore+=game.getPacmanNumberOfLivesRemaining()*AWARD_LIFE_LEFT;
		}
		
		return averageScore/trials;
	}
	
	private void setGhostsNetworks(NEATGhosts ghostsController, NEATNetwork network){
		//number of ghosts
		int gs=GHOST.values().length;
		
		NEATNetwork[] ghostsNetworks=new NEATNetwork[gs];
		
		for (int i=0; i<gs; i++) {
			ghostsNetworks[i]=network;
		}
		
		ghostsController.setGhostsNetworks(ghostsNetworks);
	}
	
	private void setGhostsNetworks(NEATGhosts ghostsController, NEATNetwork testNetwork, NEATNetwork bestNetwork, int testTeamSize) {
		//number of ghosts
		int gs=GHOST.values().length;
		
		if (testTeamSize>gs) {
			testTeamSize=gs;
		}
		
		NEATNetwork[] ghostsNetworks=new NEATNetwork[gs];
		int currGhost=0;
		
		while (currGhost<testTeamSize) {
			ghostsNetworks[currGhost++]=testNetwork;
		}
		
		while(currGhost<gs) {
			ghostsNetworks[currGhost++]=bestNetwork;
		}
		
		ghostsController.setGhostsNetworks(ghostsNetworks);
	}
	
	private NEATGhosts getGhostsController(NEATNetwork network) {
		NEATGhosts ghostsController=new NEATGhosts();
		
		if (useBestNetwork && bestNetworkSet) {
			setGhostsNetworks(ghostsController, network, bestNetwork, testTeamSize);
		}else {
			setGhostsNetworks(ghostsController, network);
		}
		
		return ghostsController;
	}

	@Override
	public boolean requireSingleThreaded() {
		return false;
	}

	@Override
	public boolean shouldMinimize() {
		return true;
	}

}
