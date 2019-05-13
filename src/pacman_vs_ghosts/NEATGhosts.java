package pacman_vs_ghosts;

import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Constants.DM;
import pacman.game.Game;

import org.encog.neural.neat.NEATNetwork;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;

import java.util.EnumMap;

public class NEATGhosts extends Controller<EnumMap<GHOST,MOVE>>{
	//number of directions
	private static final int DIRECTIONS_NUM=4;
	//normalize ANN input to range [NORM_0; NORM_1]
	private static final double NORM_0=-1;
	private static final double NORM_1=1;
	//maximal observable distance from PacMan to closest Power Pill
	private static final int PP_DISTANCE=10;
	//maximal observable distance from PacMan to Ghost or from Ghost to Ghost
	private static final int G_DISTANCE=200;
	//number of input neurons
	public static final int NETWORK_INPUT=26;
	//number of output neurons
	public static final int NETWORK_OUTPUT=5;
	
	private EnumMap<GHOST, NEATNetwork> ghostsNetworks;
	
	private EnumMap<GHOST,MOVE> myMoves;
	private MOVE[] moves;
	
	public NEATGhosts() {
		ghostsNetworks=new EnumMap<GHOST,NEATNetwork>(GHOST.class);
		moves=MOVE.values();
	}
	
	public void setGhostsNetworks(NEATNetwork[] networks) {
		if (networks.length<4) {
			throw new RuntimeException("ghosts networks array length<4");
		}
		
		ghostsNetworks.put(GHOST.BLINKY, networks[0]);
		ghostsNetworks.put(GHOST.PINKY, networks[1]);
		ghostsNetworks.put(GHOST.INKY, networks[2]);
		ghostsNetworks.put(GHOST.SUE, networks[3]);
	}

	@Override
	public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
		myMoves=new EnumMap<GHOST,MOVE>(GHOST.class);
		
		for(GHOST ghost: GHOST.values()) {
			if (game.doesGhostRequireAction(ghost)) {
				myMoves.put(ghost, 
						outputToMove(
								ghostsNetworks.get(ghost).compute(
										generateInput(game, ghost))));
			}
		}
		
		return myMoves;
	}

	//transform ANN output to game move
	private MOVE outputToMove(MLData output) {
		double[] arrayOutput=output.getData();
		
		if (arrayOutput.length!=moves.length) {
			throw new RuntimeException("network output not equals number of moves");
		}
		
		double max=arrayOutput[0];
		int n=0;
		for (int i=1; i<moves.length; i++) {
			if (arrayOutput[i]>max) {
				n=i;
				max=arrayOutput[i];
			}
		}
		
		return moves[n];
	}
	
	//read and normalize input data for ANN
	private static MLData generateInput(Game game, GHOST ghost) {
		MLData input=new BasicMLData(NETWORK_INPUT);
		
		int currentInput=0;
		
		NormalizedField directionTAStats=new NormalizedField(
				NormalizationAction.Normalize, 
				"directionTA", 
				1, -1, 
				NORM_1, NORM_0);
		
		NormalizedField directionStats=new NormalizedField(
				NormalizationAction.Normalize, 
				"direction", 
				1, 0, 
				NORM_1, NORM_0);
		
		NormalizedField ppDistanceStats=new NormalizedField(
				NormalizationAction.Normalize, 
				"ppDistance", 
				PP_DISTANCE, 0, 
				NORM_1, NORM_0);
		
		NormalizedField gDistanceStats=new NormalizedField(
				NormalizationAction.Normalize, 
				"gDistance", 
				G_DISTANCE, 0, 
				NORM_1, NORM_0);
		
		NormalizedField ppNumberStats=new NormalizedField(
				NormalizationAction.Normalize, 
				"ppNumber", 
				game.getNumberOfPowerPills(), 0, 
				NORM_1, NORM_0);
		
		NormalizedField pNumberStats=new NormalizedField(
				NormalizationAction.Normalize, 
				"pNumber", 
				game.getNumberOfPills(), 0, 
				NORM_1, NORM_0);
		
		/////////////////////////////////////////////////////////////////////////
		//////////////////////////Directed sensors///////////////////////////////
		/////////////////////////////////////////////////////////////////////////
		
		//direction towards, away from PacMan
		for (int d: getDirectionsTowardsAndAwayFromPacMan(game, ghost)) {
			input.setData(currentInput++, directionTAStats.normalize(d));
		}
		
		//ghost direction
		for (int d: getCurrentGhostDirection(game, ghost)) {
			input.setData(currentInput++, directionStats.normalize(d));
		}
		
		/////////////////////////////////////////////////////////////////////////
		/////////////////////////unDirected sensors//////////////////////////////
		/////////////////////////////////////////////////////////////////////////
		
		//PacMan distance to closest power pill
		input.setData(currentInput++, ppDistanceStats.normalize(
				getPacManPathDistanceToClosestPowerPill(game)));
		
		//distances between PacMan and Ghosts
		for (int d: getPacManDistancesToGhosts(game, ghost)) {
			input.setData(currentInput++, gDistanceStats.normalize(d));
		}
		
		//distances to other Ghosts
		for (int d: getDistancesToOtherGhosts(game, ghost)) {
			input.setData(currentInput++, gDistanceStats.normalize(d));
		}
		
		//number of available Power Pills
		input.setData(currentInput++, ppNumberStats.normalize(
				game.getNumberOfActivePowerPills()));
		
		//number of available Pills
		input.setData(currentInput++, pNumberStats.normalize(
				game.getNumberOfActivePills()));
		
		//are ghosts edible
		for (boolean e: getAreGhostsEdible(game, ghost)) {
			input.setData(currentInput++, 
					(e) ? NORM_1 : NORM_0);
		}
		
		//are other ghosts in lair
		for (boolean l: getAreOtherGhostsInLair(game, ghost)) {
			input.setData(currentInput++, 
					(l) ? NORM_1 : NORM_0);
		}
		
		/////////////////////////////////////////////////////////////////////////
		////////////////////////////////bias/////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////
		input.setData(currentInput++, 1);
		
		return input;
	}
	
	private static int getPacManPathDistanceToClosestPowerPill(Game game) {
		int[] activePowerPills=game.getActivePowerPillsIndices();
		
		if (activePowerPills.length>0) {
			int pacManNode=game.getPacmanCurrentNodeIndex();
			
			int closestPowerPill=game.getClosestNodeIndexFromNodeIndex(pacManNode, activePowerPills, DM.PATH);
			
			return game.getShortestPathDistance(pacManNode, closestPowerPill);
		}else {
			return Integer.MAX_VALUE;
		}
	}
	
	private static int[] getPacManDistancesToGhosts(Game game, GHOST currentGhost) {
		//number of ghosts
		int gs=GHOST.values().length;
		//distances from PacMan to ghosts
		int[] distances=new int[gs];
		
		int pacManNode=game.getPacmanCurrentNodeIndex();
		
		//number of current ghost
		int currentGhostN=0;
		
		int i=0;
		for (GHOST ghost: GHOST.values()) {
			if (currentGhost==ghost) {
				currentGhostN=i;
			}
			
			distances[i++]=game.getShortestPathDistance(pacManNode, game.getGhostCurrentNodeIndex(ghost));
		}
		
		//set distances from PacMan to ghost from current ghost perspective
		int temp=distances[currentGhostN];
		distances[currentGhostN]=distances[0];
		distances[0]=temp;
		
		return distances;
	}
	
	private static int[] getDistancesToOtherGhosts(Game game, GHOST currentGhost){
		//number of ghosts
		int gs=GHOST.values().length;
		
		int[] ghostsNodes=new int[gs];
		
		//number of current ghost
		int currentGhostN=0;
		
		int i=0;
		for (GHOST ghost: GHOST.values()) {
			if (currentGhost==ghost) {
				currentGhostN=i;
			}
			
			ghostsNodes[i++]=game.getGhostCurrentNodeIndex(ghost);
		}
		
		//move current ghost's node to the 0th position 
		int temp=ghostsNodes[currentGhostN];
		ghostsNodes[currentGhostN]=ghostsNodes[0];
		ghostsNodes[0]=temp;
		
		//distances to other ghosts from current ghost perspective
		int[] distancesToGhosts=new int[gs-1];
		
		for (i=1; i<gs; i++) {
			distancesToGhosts[i-1]=game.getShortestPathDistance(ghostsNodes[0], ghostsNodes[i]);
		}
		
		return distancesToGhosts;
	}
	
	private static int[] getDirectionsTowardsAndAwayFromPacMan(Game game, GHOST ghost) {
		int[] directions=new int[DIRECTIONS_NUM];
		
		MOVE towards=game.getApproximateNextMoveTowardsTarget(
				game.getGhostCurrentNodeIndex(ghost), 
				game.getPacmanCurrentNodeIndex(), 
				game.getGhostLastMoveMade(ghost), 
				DM.PATH);
		
		switch(towards) {
		case UP:
			directions[0]++;
			break;
		case RIGHT: 
			directions[1]++;
			break;
		case DOWN: 
			directions[2]++;
			break;
		case LEFT: 
			directions[3]++;
			break;
		}
		
		MOVE away=game.getApproximateNextMoveAwayFromTarget(
				game.getGhostCurrentNodeIndex(ghost), 
				game.getPacmanCurrentNodeIndex(), 
				game.getGhostLastMoveMade(ghost), 
				DM.PATH);
		
		switch(away) {
		case UP:
			directions[0]--;
			break;
		case RIGHT: 
			directions[1]--;
			break;
		case DOWN: 
			directions[2]--;
			break;
		case LEFT: 
			directions[3]--;
			break;
		}
		
		return directions;
	}
	
	private static int[] getCurrentGhostDirection(Game game, GHOST ghost) {
		int[] directions=new int[DIRECTIONS_NUM];
		for (int i=0; i<DIRECTIONS_NUM; i++) {
			directions[i]=-1;
		}
		
		MOVE lastMove=game.getGhostLastMoveMade(ghost);
		
		switch(lastMove) {
		case UP:
			directions[0]=1;
			break;
		case RIGHT: 
			directions[1]=1;
			break;
		case DOWN: 
			directions[2]=1;
			break;
		case LEFT: 
			directions[3]=1;
			break;
		}
		
		return directions;
	}
	
	private static boolean[] getAreGhostsEdible(Game game, GHOST currentGhost) {
		//number of ghosts
		int gs=GHOST.values().length;
		
		boolean[] ghostsEdible=new boolean[gs];
		
		//number of current ghost
		int currentGhostN=0;
		
		int i=0;
		for (GHOST ghost: GHOST.values()) {
			if (currentGhost==ghost) {
				currentGhostN=i;
			}
			
			ghostsEdible[i++]=game.isGhostEdible(ghost);
		}
		
		//move current ghost to the 0th position 
		boolean temp=ghostsEdible[currentGhostN];
		ghostsEdible[currentGhostN]=ghostsEdible[0];
		ghostsEdible[0]=temp;
		
		return ghostsEdible;
	}
	
	private static boolean[] getAreOtherGhostsInLair(Game game, GHOST currentGhost) {
		//number of ghosts
		int gs=GHOST.values().length;
		
		boolean[] ghostsInLair=new boolean[gs-1];
		
		int i=0;
		for (GHOST ghost: GHOST.values()) {
			if (currentGhost!=ghost) {
				ghostsInLair[i++]=game.getGhostLairTime(ghost)>0;
			}
		}
		
		return ghostsInLair;
	}
}
