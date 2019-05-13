
package tic_tac_toe.tree;

import java.util.ArrayList;
import java.util.LinkedList;
import org.encog.neural.neat.NEATNetwork;

public class GameTree {
    private LinkedList<ArrayList<Node>> levels;
    
    private boolean useNetwork;
    private NEATNetwork network;
    
    public GameTree(boolean useNetwork, NEATNetwork network) {
    	this.useNetwork=useNetwork;
    	if (useNetwork) {
    		this.network=network;
    	}else {
    		this.network=null;
    	}
    }
    
    private LinkedList<Integer> getBestPath(Node root, boolean player, int numLevels){
        levels=new LinkedList<>();
        
        //create zero level and add root node
        levels.add(new ArrayList<>());
        levels.getLast().add(root);
        Node node=root;
        
        //recursive function
        getNodeValue(node, null, player, 0, numLevels)/*)*/;
        int i;
        
        LinkedList<Integer> path=new LinkedList<>();
        
        while (!node.descendants.isEmpty()){
            i=0;
            for (Node desc: node.descendants){
                if (node.meta.value==desc.meta.value){
                    for (int j=0; j<=i; j++){
                        if (node.isFull(j)){
                            i++;
                        }
                    }
                    
                    path.add(i);
                    node=desc;
                    break;
                }
                i++;
            }
        }

        return path;
    }
    
    //return best action
    public int chooseAction(Node root, boolean player, int numLevels){
        return getBestPath(root, player, numLevels).getFirst();
    }

    //return node heuristic value
    //alpha-beta algotithm recursive 
    //leaf node's value divided by distance allows to find shortest path
    private double getNodeValue(Node node, Node pred, boolean player, int level, int numLayers){
        //if node is leaf
        //compute heuristic value
        if (node.getIsLeafNode() || level==numLayers){
            node.meta.visited=true;
            node.meta.strict=true;
            
            int divider;
            if (level==0) {
            	divider=1;
            }else {
            	divider=level;
            }
            
            if (useNetwork) {
            	node.meta.value=node.getNetworkValue(network);
            }else {
            	node.meta.value=node.getHeuristicValue();
            }
        }
        //if not leaf node and value is not known, visit offsprings
        else if (!node.meta.strict){
            double temp;
            int i;
            Node desc;
            
            for (int c=0; c<Node.COLS; c++){
                if (!node.isFull(c)){
                    //generate offspring
                    node.addToken(c, player);
                    
                    if (levels.size()<=level+1){
                        levels.add(new ArrayList<>());
                    }
                    
                    i=levels.get(level+1).indexOf(node);
                    if (i==-1){
                        //unique
                        desc=new Node(node);
                        desc.getStatusAfterAdding(c);
                        levels.get(level+1).add(desc);
                    }else{
                        //not unique
                        desc=levels.get(level+1).get(i);
                    }
                    node.addDescendant(desc);
                    node.removeToken(c);
                    
                    
                    //call recursively and get offspring's
                    //heuristic value
                    temp=getNodeValue(desc, node, !player, level+1, numLayers);
                    //set new value if needed
                    if (!node.meta.visited ||
                            ((player && temp>node.meta.value) ||
                            (!player && temp<node.meta.value))){
                        node.meta.visited=true;
                        node.meta.value=temp;
                        
                        //prunning if needed
                        if (pred!=null && pred.meta.visited &&
                                ((player && node.meta.value>=pred.meta.value) ||
                                (!player && node.meta.value<=pred.meta.value))){
                            break;
                        }
                    }
                }
            }
        }
        
        node.meta.strict=true;
        return node.meta.value;
    }
}
