
package tic_tac_toe.gui;

import javax.swing.JOptionPane;

import org.encog.neural.neat.NEATNetwork;

import tic_tac_toe.tree.GameTree;
import tic_tac_toe.tree.Node;
import static tic_tac_toe.gui.REGIME.*;

import java.util.Timer;
import java.util.TimerTask;

public class GameWindow extends javax.swing.JFrame {
	// Variables declaration
    private DrawPanel drawPanel1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration
	
    private boolean gameStarted;
    private boolean gameWon;
    private boolean trainerFirst;
    private boolean standoff;
    private int treeDepth;
    private boolean player;
    private int chosenColumn;
    private Node currentNode;
    private REGIME regime;
    private GameTree trainerTree;
    private GameTree networkTree;
    private int delay=1000;
    private Timer timer=null;
    private int networkTreeDepth=1;
    
    
    public static void runGame() {
    	java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GameWindow().setVisible(true);
            }
        });
    }
    
    public static void runGame(NEATNetwork network, boolean withHuman) {
    	java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GameWindow(network, withHuman).setVisible(true);
            }
        });
    }
    
    /**
     * Creates new form Form
     */
    private GameWindow() {
        initComponents();
        
        jTextField1.setText("5");
        jCheckBox1.setSelected(true);
        regime=HUMAN_TRAINER;
        trainerTree=new GameTree(false, null);
        networkTree=null;
        
        endGame();
    }
    
    private GameWindow(NEATNetwork network, boolean withHuman) {
        initComponents();
        
        jTextField1.setText("5");
        jCheckBox1.setSelected(true);
        regime=(withHuman)?HUMAN_NETWORK:NETWORK_TRAINER;
        trainerTree=new GameTree(false, null);
        networkTree=new GameTree(true, (NEATNetwork)network);
        
        endGame();
    }
    
    @SuppressWarnings("unchecked")
    private void initComponents() {

        drawPanel1 = new DrawPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        drawPanel1.setPreferredSize(new java.awt.Dimension(390, 335));
        drawPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                drawPanel1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout drawPanel1Layout = new javax.swing.GroupLayout(drawPanel1);
        drawPanel1.setLayout(drawPanel1Layout);
        drawPanel1Layout.setHorizontalGroup(
            drawPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 390, Short.MAX_VALUE)
        );
        drawPanel1Layout.setVerticalGroup(
            drawPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 335, Short.MAX_VALUE)
        );

        jCheckBox1.setText("Treneris pirmais");

        jLabel1.setText("Spēles koka dziļums");

        jButton1.setText("SĀKT SPĒLI");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });

        jButton2.setText("IZDARĪT GĀJIENU");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(drawPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(42, 42, 42)
                                .addComponent(jButton1)
                                .addGap(92, 92, 92)
                                .addComponent(jButton2)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBox1)
                        .addGap(66, 66, 66))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox1))
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addComponent(drawPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }

    private void drawPanel1MouseClicked(java.awt.event.MouseEvent evt) {
        int cols=drawPanel1.getNode().COLS;
        int cell=drawPanel1.CELL;
        int border=drawPanel1.BORDER;
        int x=border;
        
        int c=-1;
        for (int i=0; i<cols; i++){
            if (evt.getPoint().x>=x &&
                    evt.getPoint().x<=x+cell){
                c=i;
                break;
            }
            x+=border+cell;
        }
        
        if (c>=0 && !currentNode.isFull(c) && gameStarted && !gameWon){
            chosenColumn=c;
            drawPanel1.setNode(new Node(currentNode, chosenColumn, player),
                    chosenColumn, 
                    currentNode.lastRow(chosenColumn)+1); 
        }
    }

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {
        switch (jButton1.getText()){
            case "SĀKT SPĒLI":
                startGame();
                break;
            case "BEIGT SPĒLI": 
                endGame();
                break;
        }
    }

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {
        makeTurn();
    }

    private void endGame(){
        gameStarted=false;
        gameWon=false;
        if (timer!=null) {
        	timer.cancel();
        	timer=null;
        }
        jTextField1.setEnabled(true);
        jCheckBox1.setEnabled(true);
        jButton2.setEnabled(false);
        jButton1.setText("SĀKT SPĒLI");
        currentNode=new Node();
        drawPanel1.setNode(currentNode);
    }
    
    private void startGame(){
        trainerFirst=jCheckBox1.isSelected();
        try{
            treeDepth=Integer.parseInt(jTextField1.getText());
            
            if (treeDepth<1) throw new IllegalArgumentException();
        }catch (NumberFormatException e){
            JOptionPane.showMessageDialog(null, 
                    "'Koka Dziļums' nav skaitlis!", 
                    "Bridinājums", 
                    JOptionPane.WARNING_MESSAGE);
            
            return;
        }catch (IllegalArgumentException e){
            JOptionPane.showMessageDialog(null, 
                    "'Koka Dziļums'<1!", 
                    "Bridijājums", 
                    JOptionPane.WARNING_MESSAGE);
            
            return;
        }
        
        jTextField1.setEnabled(false);
        jCheckBox1.setEnabled(false);
        jButton2.setEnabled(true);
        player=true;
        gameStarted=true;
        standoff=false;
        currentNode=new Node();
        drawPanel1.setNode(currentNode);
        jButton1.setText("BEIGT SPĒLI");
        chosenColumn=-1;
        gameWon=false;
        
        if (regime==NETWORK_TRAINER) {
        	timer=new Timer();
        	timer.schedule(new TimerTask() {
				@Override
				public void run() {
					makeTurn();
				}
        	}, delay, delay);
        	jButton2.setEnabled(false);
        }else {
        	makeTurn();
        }
    }
    
    private void makeTurn(){
        if (!gameWon){
            if (player==trainerFirst){
            	int column;
            	if (regime==HUMAN_NETWORK) {
            		column=networkTree.chooseAction(currentNode, player, networkTreeDepth);
            	}else {
            		column=trainerTree.chooseAction(currentNode, player, treeDepth);
            	}
            	
            	currentNode=new Node(currentNode, column, player);
            	drawPanel1.setNode(currentNode, column, currentNode.lastRow(column));               
            }else{
            	if (regime==NETWORK_TRAINER) {
            		int column=networkTree.chooseAction(currentNode, player, networkTreeDepth);
            		currentNode=new Node(currentNode, column, player);
                	drawPanel1.setNode(currentNode, column, currentNode.lastRow(column));    
            	}
            	else {
            		if (chosenColumn!=-1){
            			currentNode=new Node(currentNode, chosenColumn, player);
            			chosenColumn=-1;        
            		}
            	}
            }
            
            player=!player;
            if (currentNode.getIsLeafNode()){
            	if (currentNode.getValue()==0) {
            		standoff=true;
            	}
            	showGameWon();
            }
        }
    }
    
    private void showGameWon(){
        gameWon=true;
        if (timer!=null) {
        	timer.cancel();
        	timer=null;
        }
        String message;
        if (standoff){
            message="Neizšķirts rezultāts.";
        }else if (player==trainerFirst){
            if (regime==NETWORK_TRAINER) {
            	message="MNT uzvārēja!";
            }else {
            	message="Jūs uzvārējat!";
            }
        }else{
        	if (regime==HUMAN_NETWORK) {
        		message="MNT uzvārēja.";
        	}else {
        		message="Treneris uzvārēja.";
        	}
        }
        JOptionPane.showMessageDialog(null, 
                    message, 
                    "Spēle ir pabeigta", 
                    JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        /*try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GameWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GameWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GameWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GameWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }*/
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GameWindow().setVisible(true);
            }
        });
    }
}
