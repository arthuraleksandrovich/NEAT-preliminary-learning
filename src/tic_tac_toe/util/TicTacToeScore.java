package tic_tac_toe.util;

import java.util.Scanner;

import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import org.encog.neural.neat.NEATNetwork;

import tic_tac_toe.tree.GameTree;
import tic_tac_toe.tree.Node;

public class TicTacToeScore implements CalculateScore{
	private static final int HEURISTIC_LEVELS=4;
	private static final int NETWORK_LEVELS=1;
	
	public double calculateScore(MLMethod network, boolean networkPlayer) {
		double score=0;
		int turn=0;
		
		boolean player=true;
        boolean trainerFirst=!networkPlayer;
        Node node=new Node();
        GameTree treeHeuristic=new GameTree(false, null);
        GameTree treeNetwork=new GameTree(true, (NEATNetwork)network);
        do{
            if (player==trainerFirst){
                node=new Node(node, treeHeuristic.chooseAction(node, player, HEURISTIC_LEVELS), player);
            }else{
            	node=new Node(node, treeNetwork.chooseAction(node, player, NETWORK_LEVELS), player);
            }
            player=!player;
            turn++;
        }while (!node.getIsLeafNode());
        
        player=!player;
        if (player==networkPlayer) {
        	score=42-turn;
        }else {
        	score=turn-42;
        }
        
        return score;
	}

	@Override
	public boolean requireSingleThreaded() {
		return false;
	}

	@Override
	public boolean shouldMinimize() {
		return false;
	}

	@Override
	public double calculateScore(MLMethod arg0) {
		return (calculateScore(arg0, true)+calculateScore(arg0, false))/2;
	}
}
