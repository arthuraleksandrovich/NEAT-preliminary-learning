
package tic_tac_toe.gui;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

import tic_tac_toe.tree.Node;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Line2D;

public class DrawPanel extends JPanel{
    private Node node=null;
    
    private int selectedColumn=-1;
    private int selectedRow=-1;
    
    public static final int CELL=50;
    public static final int BORDER=5;
    private static final Color BACK_C=Color.WHITE;
    private static final Color BORDER_C=Color.BLACK;
    private static final Color FIRST_C=Color.RED;
    private static final Color FIRST_NC=new Color(255, 100, 100);
    private static final Color SECOND_C=Color.YELLOW;
    private static final Color SECOND_NC=new Color(255, 255, 100);
    
    

    
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        if (node!=null){
            int w=node.COLS*CELL+(node.COLS+1)*BORDER;
            int h=node.ROWS*CELL+(node.ROWS+1)*BORDER;
            g.setColor(BACK_C);
            g.fillRect(0, 0, w, h);
            
            int x=0; 
            int y=0;
            g.setColor(BORDER_C);
            for (int i=0; i<=node.COLS; i++){
                g.fillRect(x, y, BORDER, h);
                x+=BORDER+CELL;
            }
            x=0;
            for (int i=0; i<=node.ROWS; i++){
                g.fillRect(x, y, w, BORDER);
                y+=BORDER+CELL;
            }
            
            for (int c=0; c<node.COLS; c++){
                for (int r=0; r<node.ROWS; r++){
                    if (node.isToken(c, r)){
                        if (node.getColor(c, r)){
                            if (c==selectedColumn && r==selectedRow)
                                g.setColor(FIRST_NC);
                            else
                                g.setColor(FIRST_C);
                        }else{
                            if (c==selectedColumn && r==selectedRow)
                                g.setColor(SECOND_NC);
                            else
                                g.setColor(SECOND_C);
                        }
                        x=(c+1)*BORDER+c*CELL;
                        y=(node.ROWS-r);
                        y=y*BORDER+(y-1)*CELL;
                        g.fillRect(x, y, CELL, CELL);
                    }
                }
            }
            
            if (node.getIsLeafNode()){
                Point p=node.getLineStartPoint();
                int x1=p.x;
                x1=(x1+1)*BORDER+x1*CELL+CELL/2;
                int y1=node.ROWS-p.y;
                y1=y1*BORDER+(y1-1)*CELL +CELL/2;
                
                p=node.getLineEndPoint();
                int x2=p.x;
                x2=(x2+1)*BORDER+x2*CELL+CELL/2;
                int y2=node.ROWS-p.y;
                y2=y2*BORDER+(y2-1)*CELL +CELL/2;
                
                Graphics2D g2=(Graphics2D)g;
                if (node.getColor(selectedColumn, selectedRow)){
                    g2.setColor(FIRST_NC);
                }else{
                    g2.setColor(SECOND_NC);
                }
                g2.setStroke(new BasicStroke(BORDER));
                g2.draw(new Line2D.Float(x1, y1, x2, y2));
            }
        }
    }
    
    public void setNode(Node node){
        setNode(node, -1, -1);
    }
    
    public void setNode(Node node, int column, int row){
        this.node=node;
        selectedColumn=column;
        selectedRow=row;
        repaint();
    }
    
    public Node getNode(){
        return node;
    }
}
