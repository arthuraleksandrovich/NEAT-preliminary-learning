
package tic_tac_toe.tree;

import java.util.BitSet;
import java.util.LinkedHashSet;
import java.util.Objects;

import static tic_tac_toe.tree.LINE_DIRECTION.*;
import static tic_tac_toe.tree.NODE_STATUS.*;

import java.awt.Point;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.neat.NEATNetwork;

public class Node {
    public static final int ROWS=6;
    public static final int COLS=7;
    static final int LINE_LENGTH=4;
    public static final int NETWORK_INPUT=ROWS*COLS+1;
    public static final int NETWORK_OUTPUT=1;
    private static final double NORM_0=-1;
	private static final double NORM_1=1;
    
    //red player begins
    static final boolean RED=true;
    static final boolean YELLOW=false;
    
    //value is set only if leaf node
    protected int value;
    protected LINE_DIRECTION lineDirection;
    protected int lineColumn;
    protected int lineRow;
    protected boolean isLeafNode;
    
    //needed for aplha-beta 
    public final LinkedHashSet<Node> descendants;
    public final Meta meta;
    
    //game field storing
    private final BitSet fieldFilling;
    private final BitSet tokensColors;
    
    public Node(){
        fieldFilling=new BitSet(ROWS*COLS);
        tokensColors=new BitSet(ROWS*COLS);
        descendants=new LinkedHashSet<>();
        isLeafNode=false;
        lineDirection=NONE;
        meta=new Meta();
    }
    
    //copy constructor
    public Node(Node other){
        this.fieldFilling=(BitSet)other.fieldFilling.clone();
        this.tokensColors=(BitSet)other.tokensColors.clone();
        descendants=new LinkedHashSet<>();
        isLeafNode=false;
        lineDirection=NONE;
        meta=new Meta();
    }
    
    //new node by adding one token
    public Node(Node other, int column, boolean player){
        this(other);
        addToken(column, player);
        getStatusAfterAdding(column);
    }
    
    public boolean isToken(int column, int row){
        return fieldFilling.get(row*COLS+column);
    }
    
    public int lastRow(int column){
        for (int r=ROWS-1; r>=0; r--){
            if (isToken(column, r)){
                return r;
            }
        }
        
        return -1;
    }
    
    public boolean getColor(int column, int row){
        return tokensColors.get(row*COLS+column);
    }
    
    public boolean isColor(int column, int row, boolean color){
        return (getColor(column, row)==color);
    }
    
    public void addToken(int column, boolean color){
        if (isFull(column)){
            throw new IllegalArgumentException("Can't add token");
        }
        
        for (int r=0; r<ROWS; r++){
            if (!isToken(column, r)){
                fieldFilling.set(r*COLS+column);
                tokensColors.set(r*COLS+column, color);
                
                break;
            }
        }
    }
    
    public NODE_STATUS getStatusAfterAdding(int column){
        for (int r=ROWS-1; r>=0; r--){
            if (isToken(column, r)){
                boolean color=getColor(column, r);
                
                if (isLineMade(column, r)){
                    if (color==RED){
                        isLeafNode=true;
                        value=1;
                        return FIRST_WON;
                    }else{
                        isLeafNode=true;
                        value=-1;
                        return SECOND_WON;
                    }
                }
                
                if (isFull()){
                    isLeafNode=true;
                    value=0;
                    return FILLED;
                }
                
                return NOT_FILLED;
            }
        }
        
        throw new IllegalArgumentException("Column is empty");
    }
    
    public void removeToken(int column){
        for (int r=ROWS-1; r>=0; r--){
            if (isToken(column, r)){
                fieldFilling.clear(r*COLS+column);
                tokensColors.clear(r*COLS+column);
                
                break;
            }
        }
    }
    
    public boolean isFull(int column){
        return (isToken(column, ROWS-1));
    }
    
    public boolean isFull(){
        for (int c=0; c<COLS; c++){
            if (!isFull(c)){
                return false;
            }
        }
        
        return true;
    }
    
    //is 4-token line made after token adding
    public boolean isLineMade(int column, int row){
        int r, c, counter;
        
        boolean color=getColor(column, row);
        
        int offset;
        int sign;
        boolean direct;
        
        //check in 4 directions
        //if not found, change direction to opposite inside iteration
        for (int i=0; i<4; i++){
            offset=0;
            direct=true;
            sign=1;
            counter=1;
            r=c=0;
            do{
                switch (i){
                    case 0: 
                        c++;
                        break;
                    case 1:
                        c++;
                        r++;
                        break;
                    case 2:
                        r++;
                        break;
                    case 3:
                        c--;
                        r++;
                        break;
                }
                
                if ((column+sign*c>=0) && (column+sign*c<COLS) && (row+sign*r>=0) && (row+sign*r<ROWS) &&
                        ((isToken(column+sign*c, row+sign*r) && isColor(column+sign*c, row+sign*r, color)))){
                    counter++;
                    if (!direct){
                        offset++;
                    }
                }else if (direct){
                    direct=false;
                    sign=-1;
                    r=c=0;
                }else{
                    break;
                }
                if (direct && counter>=LINE_LENGTH){
                    direct=false;
                    sign=-1;
                    r=c=0;
                }
            }while (counter<LINE_LENGTH);
            if (counter>=LINE_LENGTH){
                switch (i){
                    case 0:
                        lineDirection=RIGHT;
                        lineColumn=column-offset;
                        lineRow=row;
                        break;
                    case 1:
                        lineDirection=UP_RIGHT;
                        lineColumn=column-offset;
                        lineRow=row-offset;
                        break;
                    case 2:
                        lineDirection=UP;
                        lineColumn=column;
                        lineRow=row-offset;
                        break;
                    case 3:
                        lineDirection=UP_LEFT;
                        lineColumn=column+offset;
                        lineRow=row-offset;
                        break;
                }
                
                return true;
            }
        }
        return false;
    }
    
    public void clearStatus(){
        isLeafNode=false;
        value=0;
        lineDirection=NONE;
    }
    
    public int getValue(){
        return value;
    }
    
    public void addDescendant(Node desc){
        if (!descendants.contains(desc)){
            descendants.add(desc);
        }
    }
    
    public boolean getIsLeafNode(){
        return isLeafNode;
    }
    
    public Point getLineStartPoint(){
        return new Point(lineColumn, lineRow);
    }
    
    public Point getLineEndPoint(){
        switch (lineDirection){
            case RIGHT:
                return new Point(lineColumn+LINE_LENGTH-1, lineRow);
            case UP_RIGHT:
                return new Point(lineColumn+LINE_LENGTH-1, lineRow+LINE_LENGTH-1);
            case UP:
                return new Point(lineColumn, lineRow+LINE_LENGTH-1);
            case UP_LEFT:
                return new Point(lineColumn-(LINE_LENGTH-1), lineRow+LINE_LENGTH-1);
            default:
                return new Point(lineColumn, lineRow);
        }
    }
    
    public double getHeuristicValue(){
        return (isLeafNode)?factor_ex4()*1000:
                factor_num3()*10+factor_sumPartOf();
    }
    
    public double getNetworkValue(NEATNetwork network) {
    	MLData input=new BasicMLData(ROWS*COLS);
    	input.setData(nodeToArray(this));
    	
    	MLData output=network.compute(input);
    	return output.getData()[0];
    }
    
    public int factor_ex4(){
        return value;
    }
    
    public int factor_num3(){
        int value=0;
        
        for (int c=0; c<COLS; c++){
            for (int r=0; r<ROWS; r++){
                if (isToken(c, r)){
                    value+=numLine3(c, r);
                }
            }
        }
        
        return value;
    }
    
    public int factor_sumPartOf(){
        int sum=0;
        for (int c=0; c<COLS; c++){
            for (int r=0; r<ROWS; r++){
                if (isToken(c, r)){
                    sum+=cellPartOf(c, r);
                }
            }
        }
        
        return sum;
    }
    
    //number of tree-token lines
    private int numLine3(int column, int row){
        int value=0;
        boolean color=getColor(column, row);
        
        boolean left=column+1>=LINE_LENGTH;
        boolean right=COLS-column>=LINE_LENGTH;
        boolean above=ROWS-row>=LINE_LENGTH;
        
        boolean check;
        
        if (right){
            check=true;
            for (int i=1; i<LINE_LENGTH-1; i++){
                if (!(isToken(column, row+i) && isColor(column, row+i, color))){
                    check=false;
                    break;
                }
            }
            if (check){
                if (isToken(column, row+LINE_LENGTH-1)){
                    check=false;
                }
            }
            
            if (check) value++; 
        }
        
        if (above){
            check=true;
            for (int i=1; i<LINE_LENGTH-1; i++){
                if (!(isToken(column+i, row) && isColor(column+i, row, color))){
                    check=false;
                    break;
                }
            }
            if (check){
                if (isToken(column+LINE_LENGTH-1, row)){
                    check=false;
                }
            }
            
            if (check) value++; 
            
            if (right){
                check=true;
                for (int i=1; i<LINE_LENGTH-1; i++){
                    if (!(isToken(column+i, row+i) && isColor(column+i, row+i, color))){
                        check=false;
                        break;
                    }
                }
                if (check){
                    if (isToken(column+LINE_LENGTH-1, row+LINE_LENGTH-1)){
                        check=false;
                    }
                }
                
                if (check) value++; 
            }
            
            if (left){
                check=true;
                for (int i=1; i<LINE_LENGTH-1; i++){
                    if (!(isToken(column-i, row+i) && isColor(column-i, row+i, color))){
                        check=false;
                        break;
                    }
                }
                if (check){
                    if (isToken(column-LINE_LENGTH-1, row+LINE_LENGTH-1)){
                        check=false;
                    }
                }
                
                if (check) value++; 
            }
        }
        
        if (!color) value*=-1;
        return value;
    }
    
    //number of lines token can be part of 
    private int cellPartOf(int column, int row){
        int value=0;
        boolean color=getColor(column, row);
        
        int r, c, counter;
        boolean direct;
        int sign;
        
        for (int i=0; i<4; i++){
            direct=true;
            sign=1;
            counter=1;
            r=c=0;
            do{
                switch (i){
                    case 0: 
                        c++;
                        break;
                    case 1:
                        c++;
                        r++;
                        break;
                    case 2:
                        r++;
                        break;
                    case 3:
                        c--;
                        r++;
                        break;
                }
                
                if ((column+sign*c>=0) && (column+sign*c<COLS) && (row+sign*r>=0) && (row+sign*r<ROWS) &&
                        ((isToken(column+sign*c, row+sign*r) && isColor(column+sign*c, row+sign*r, color)) || 
                        (!isToken(column+sign*c, row+sign*r)))){
                    counter++;
                }else if (direct){
                    direct=false;
                    sign=-1;
                    r=c=0;
                }else{
                    break;
                }
                if (direct && counter>=LINE_LENGTH){
                    direct=false;
                    sign=-1;
                    r=c=0;
                }
            }while (counter<(LINE_LENGTH*2-1));
            if (counter>=LINE_LENGTH) value++;
            if (counter==(LINE_LENGTH*2-1)) value++;
        }
        
        if (!color) value*=-1;
        return value;
    }
    
    @Override
    public boolean equals(Object oth){
        try{
            if (oth!=null){
                Node other=(Node)oth;
                if (this.fieldFilling.equals(other.fieldFilling) && 
                    this.tokensColors.equals(other.tokensColors) ||
                    isReflected(other)){
                    return true;
                }
            }
            
            return false;
        }catch (ClassCastException e){
            return false;
        }
    }
    
    public boolean isReflected(Node other){
        for (int c=0; c<COLS; c++){
            for (int r=0; r<ROWS; r++){
                if (this.isToken(c, r)!=other.isToken(COLS-c-1, r) || 
                    this.getColor(c, r)!=other.getColor(COLS-c-1, r)){
                    return false;
                }
            }
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.fieldFilling);
        hash = 79 * hash + Objects.hashCode(this.tokensColors);
        return hash;
    }
    
    public static double[] nodeToArray(Node node) {
		double[] array=new double[NETWORK_INPUT];
		
		int counter=0;
		for (int i=0; i<ROWS; i++) {
			for (int j=0; j<COLS; j++) {
				if (!node.isToken(j, i)) {
					array[counter]=0;
				}else {
					if (node.getColor(j, i)) {
						array[counter]=NORM_1;
					}else {
						array[counter]=NORM_0;
					}
				}
				counter++;
			}
		}
		//bias
		array[NETWORK_INPUT-1]=1;
		
		return array;
	}
    
}
