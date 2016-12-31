/**
 * Programmer:   Shane Bishop
 * Date:         December 24, 2016
 * Program Name: Battleship2
 * Class:        Battleship2
 * 
 * Next steps:
 * - The user can currently place ships on top each other
 * - The user cannot rotate the ships yet
 * - The user cannot attack
 * - The AI cannot attack
 * - The program does not check for game over
 */
package battleship2;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class Battleship2 extends javax.swing.JFrame {
    // Variables related to the graphics
    private Container container;
    private JLabel[][] grid;
    private static final int GRID_DIMENSION =  10;
    private static final int CELL_DIMENSION =  40;
    
    // JLabel variables (graphics)
    private JLabel aircraftCarrier;
    private JLabel battleship;
    private JLabel destroyer;
    private JLabel patrolBoat;
    private JLabel submarine;
    private JLabel playingField;
    
    // Rectangle variables (used to store where the JLabels are initially positioned)
    private Rectangle patrolBoatRec;
    private Rectangle destroyerRec;
    private Rectangle submarineRec;
    private Rectangle battleshipRec;
    private Rectangle aircraftCarrierRec;
    
    // Variables related to event handling
    private Point initialClick;
    volatile private boolean mouseDown = false;
    volatile private boolean isRunning = false;
    
    // Variables related to the AI
    private AIShip aipatrolBoat;
    private AIShip aidestroyer;
    private AIShip aisubmarine;
    private AIShip aibattleship;
    private AIShip aiaircraftCarrier;
    private ArrayList<int[]> aishipPositions;
    private int[] aialiveShips;
    
    // Variables related to game logic
    private int shipsPlaced;
    private boolean playersTurn;
    private boolean setupDone;
    private Random r;
    
    /**
     * Constructor
     */
    public Battleship2() {
        initComponents(); // Initializes the graphical components
        
        // Instatiates a new Random object to allow for generating random numbers
        r = new Random();
        
        // Initializes some field variables related to game logic
        playersTurn = false;
        setupDone   = false;
        shipsPlaced = 0;
        
        // Stores the content pane in a field variable
        container = getContentPane();
        
        // Initializes the grid array to a 10x10 two dimensional array 
        grid = new JLabel[10][10];
        
        // Initializes the values of the grid array
        for (int i = 0; i < GRID_DIMENSION; i++) {
            for (int j = 0; j < GRID_DIMENSION; j++) {
                grid[i][j] = new JLabel();
                grid[i][j].setBounds(CELL_DIMENSION * i + 20, 
                        CELL_DIMENSION * j + 20, 
                        CELL_DIMENSION, CELL_DIMENSION);
                grid[i][j].setOpaque(true);
                grid[i][j].setBackground(new Color(135, 206, 250));
                grid[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
                container.add(grid[i][j]);
            }
        }
        
        // Instatiates aishipPositions to a new ArrayList
        aishipPositions = new ArrayList();
        
        // Initializes the values of aishipPositions
        for (int i = 0; i < aishipPositions.size(); i++) {
            ArrayList temp = new ArrayList();
            temp.add(-1);
            aishipPositions.add(i, temp);
        }
        
        // Initializes aialiveShips to an array of length 5
        aialiveShips = new int[5];
        
        // Initializes the values of aialiveShips
        for (int i = 0; i < aialiveShips.length; i++) {
            aialiveShips[i] = i + 1;
        }
        
        // Generates some rectangles to store the initial bounds of the JLabels
        patrolBoatRec      = new Rectangle( 20, 440,  80, 40);
        destroyerRec       = new Rectangle(140, 440, 120, 40);
        submarineRec       = new Rectangle(300, 440, 120, 40);
        battleshipRec      = new Rectangle( 20, 505, 160, 40);
        aircraftCarrierRec = new Rectangle(225, 505, 200, 40);
        
        // Instantiates all of the AI's ships
        aipatrolBoat       = new AIShip(AIShip.PATROL_BOAT);
        aidestroyer        = new AIShip(AIShip.DESTROYER);
        aisubmarine        = new AIShip(AIShip.SUBMARINE);
        aibattleship       = new AIShip(AIShip.BATTLESHIP);
        aiaircraftCarrier  = new AIShip(AIShip.AIRCRAFT_CARRIER);
    }
    
    /**
     * Allows the user to drag a ship, represented by a JLabel, around the window.
     * 
     * Retrieved from http://stackoverflow.com/questions/26227046/moving-image-with-mouse-java
     * 
     * @param label The JLabel that is being dragged
     * @param evt   The MouseEvent that caused the ship to be dragged
     */
    private void dragJLabel(JLabel label, MouseEvent evt) {
        // Retrieves the initial location of the JLabel
        int thisX = label.getLocation().x;
        int thisY = label.getLocation().y;

        // Determines how much the mouse moved since the initial click
        int xMoved = (thisX + evt.getX()) - (thisX + initialClick.x);
        int yMoved = (thisY + evt.getY()) - (thisY + initialClick.y);

        // Moves picture to this position
        int X = thisX + xMoved;
        int Y = thisY + yMoved;

        // Sets the new location of the JLabel and repaints it
        label.setLocation(X, Y);
        label.repaint();
    }
    
    /**
     * Increments shipsPlaced, and then checks to see if all of the player's
     * ships have been placed. If all of the player's ships have been placed,
     * calls aiSetup, and then sets setupDone and playersTurn to true, allowing
     * for play to begin.
     */
    private void checkIfAllShipsPlaced() {
        // Increments shipsPlaced
        shipsPlaced++;
        
        // If all 5 ships have been placed, let the AI setup its ships, and set
        // setupDone and playersTurn to true
        if (shipsPlaced >= 5) {
            aiSetup();
            setupDone = true;
            playersTurn = true;
        }
    }
    
    /**
     * The logic for setup by the AI
     */
    private void aiSetup() {
        // Declares some required variables
        int col;
        int row;
        int rot;
        int shp = 5;
        ArrayList<int[]> positions;
        
        // Run while loop if col and row do not match an occupied space, and if
        // none of the spaces the ship would occupy would occupy an occupied
        // space, if any of the spaces the ship would occupy out of bounds
        // area, and if shp > 0
        while (shp > 0) {
            // Initializes the required variables
            col = r.nextInt(grid.length) + 1;
            row = r.nextInt(grid.length) + 1;
            rot = r.nextInt(1); // 0 is L>R, 1 is T>B
            positions = positions(shp, col, row, rot);
            
            while (permitted(positions)) {
                // Add positions of ship to aishipPositions
                for (int i = 0; i < positions.size(); i++) {
                    aishipPositions.add(positions.get(i));
                }

                shp--; // Decrement the ship count (for while loop monitoring)
            }
        }
        
        // Prints AI ship positions so that the programmer knows what's going on
        for (int i = 0; i < aishipPositions.size(); i++) {
            System.out.println(aishipPositions.get(i)[0] + ", " + aishipPositions.get(i)[1]);
        }
    }
    
    /**
     * Returns a boolean indicating whether or not a potential position
     * determined by the computer is valid.
     * 
     * @param positions An ArrayList of positions determined by the computer
     * 
     * @return True if all of the positions are valid, false if otherwise
     */
    private boolean permitted(ArrayList positions) {
        // Occupies an already occupied space
        for (int i = 0; i < positions.size(); i++) {
            if (aishipPositions.contains(positions.get(i))) {
                return false;
            }
        }
        
        int[] temp = (int[]) positions.get(positions.size() - 1);
        
        // Out of bounds
        if (temp[0] > 10 || temp[1] > 10) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Returns an ArrayList of positions for a ship for the AI.
     * 
     * @param ship     The ship (according to the AIShip constants)
     * @param col      The column location of the ship
     * @param row      The row location of the ship
     * @param rotation The rotation of the ship
     * 
     * @return ArrayList of positions for an AI ship
     */
    private ArrayList positions(int ship, int col, int row, int rotation) {
        // Instantiates the return value
        ArrayList<int[]> retval = new ArrayList();
        
        // Converts the ship number to the length of the ship
        // (ship numbers 3 to 5 are ignored, since the length and ship numbers
        // are the same)
        if (ship == 1) {
            ship = 2;
        }
        else if (ship == 2) {
            ship = 3;
        }
        
        // Build the return value according to the ship's rotation
        if (rotation == 0) { // L>R
            for (int i = 0; i < ship; i++) {
                int[] temp = {col, row + i};
                retval.add(temp);
            }
        }
        else {               // T>B
            for (int i = 0; i < ship; i++) {
                int[] temp = {col + i, row};
                retval.add(temp);
            }
        }
        return retval;
    }
    
    /**
     * Places a ship on the grid according to its location when the user
     * released the mouse after dragging the ship.
     * 
     * @param label The JLabel that the user dragged into position
     */
    private void placeShip(JLabel label) {
        // If the user is properly placing the ship in the grid
        if (inGrid(label)) {
            // Retrieves some information about the mouse and the JLabel
            int x = label.getLocation().x;
            int row;
            Dimension dimension = label.getSize();
            
            // A bit if-else if block for determining where the ship should go
            if (x > 20 && x < 40) {
                row = rowLocation(label);
                label.setBounds(CELL_DIMENSION * 0 + 20, 
                        CELL_DIMENSION * row + 20, 
                        dimension.width, dimension.height);
                checkIfAllShipsPlaced();
            }
            else if (x > 60 && x < 100) {
                row = rowLocation(label);
                label.setBounds(CELL_DIMENSION * 1 + 20, 
                        CELL_DIMENSION * row + 20, 
                        dimension.width, dimension.height);
                checkIfAllShipsPlaced();
            }
            else if (x > 100 && x < 140) {
                row = rowLocation(label);
                label.setBounds(CELL_DIMENSION * 2 + 20, 
                        CELL_DIMENSION * row + 20, 
                        dimension.width, dimension.height);
                checkIfAllShipsPlaced();
            }
            else if (x > 140 && x < 180) {
                row = rowLocation(label);
                label.setBounds(CELL_DIMENSION * 3 + 20, 
                        CELL_DIMENSION * row + 20, 
                        dimension.width, dimension.height);
                checkIfAllShipsPlaced();
            }
            else if (x > 180 && x < 220) {
                row = rowLocation(label);
                label.setBounds(CELL_DIMENSION * 4 + 20, 
                        CELL_DIMENSION * row + 20, 
                        dimension.width, dimension.height);
                checkIfAllShipsPlaced();
            }
            else if (x > 220 && x < 260) {
                row = rowLocation(label);
                label.setBounds(CELL_DIMENSION * 5 + 20, 
                        CELL_DIMENSION * row + 20, 
                        dimension.width, dimension.height);
                checkIfAllShipsPlaced();
            }
            else if (x > 260 && x < 300) {
                row = rowLocation(label);
                label.setBounds(CELL_DIMENSION * 6 + 20, 
                        CELL_DIMENSION * row + 20, 
                        dimension.width, dimension.height);
                checkIfAllShipsPlaced();
            }
            else if (x > 300 && x < 340) {
                row = rowLocation(label);
                label.setBounds(CELL_DIMENSION * 7 + 20, 
                        CELL_DIMENSION * row + 20, 
                        dimension.width, dimension.height);
                checkIfAllShipsPlaced();
            }
            else if (x > 340 && x < 380) {
                row = rowLocation(label);
                label.setBounds(CELL_DIMENSION * 8 + 20, 
                        CELL_DIMENSION * row + 20, 
                        dimension.width, dimension.height);
                checkIfAllShipsPlaced();
            }
            else if (x > 380 && x < 420) {
                row = rowLocation(label);
                label.setBounds(CELL_DIMENSION * 9 + 20, 
                        CELL_DIMENSION * row + 20, 
                        dimension.width, dimension.height);
                checkIfAllShipsPlaced();
            }
            else {
                // If for some reason the method inGrid evalulates to true (the
                // ship is in the playing grid), but is not within the areas
                // defined in the above else if blocks, reset the ship to its
                // initial position
                System.out.println("Error. In inner else of placeShip.");
                resetShipPosition(label);
            }
        }
        else {
            // If the ship is placed outside the playing grid, reset the ship to
            // its initial position
            resetShipPosition(label);
        }
    }
    
    /**
     * Repositions a ship back to its initial starting position.
     * 
     * @param label The JLabel that represents the ship
     */
    private void resetShipPosition(JLabel label) {
        if (label.equals(patrolBoat)) {
            label.setBounds(patrolBoatRec);
        }
        else if (label.equals(destroyer)) {
            label.setBounds(destroyerRec);
        }
        else if (label.equals(submarine)) {
            label.setBounds(submarineRec);
        }
        else if (label.equals(battleship)) {
            label.setBounds(battleshipRec);
        }
        else if (label.equals(aircraftCarrier)) {
            label.setBounds(aircraftCarrierRec);
        }
        else { // An error message, just in case
            System.out.println("Error. In else of resetShipPosition.");
        }
    }
    
    /**
     * Returns a boolean value indicating whether or not a ship is placed
     * inside the grid.
     * 
     * @param label The JLabel that the user dragged into position
     * 
     * @return True if the ship is placed inside the grid, false if otherwise
     */
    private boolean inGrid(JLabel label) {
        // Retrieves information regarding the mouse
        int x = label.getLocation().x;
        int y = label.getLocation().y;
        
        // Determines whether or not the ship is being placed inside the grid
        return x > 20 && x < 420 && y > 20 && y < 420;
    }
    
    /**
     * Returns the row the ship is being placed in by the user.
     * 
     * @param label The JLabel that the user dragged into position
     * 
     * @return The row the ship is being placed in by the user
     */
    private int rowLocation(JLabel label) {
        // Retrieves the y coordinate of the JLabel
        int y = label.getLocation().y;
        
        // Returns an int indicating the row location of the ship
        if (y > 20 && y < 60) {
            return 0;
        }
        else if (y > 60 && y < 100) {
            return 1;
        }
        else if (y > 100 && y < 140) {
            return 2;
        }
        else if (y > 140 && y < 180) {
            return 3;
        }
        else if (y > 180 && y < 220) {
            return 4;
        }
        else if (y > 220 && y < 260) {
            return 5;
        }
        else if (y > 260 && y < 300) {
            return 6;
        }
        else if (y > 300 && y < 340) {
            return 7;
        }
        else if (y > 340 && y < 380) {
            return 8;
        }
        else if (y > 380 && y < 420) {
            return 9;
        }
        else {
            // This exception is required to allow for the source code to
            // compile, since all return statements are inside if statements
            throw new Exception("Undefined exception. No appropriate return statement for rowLocation. Thrown in else of rowLocation.");
        }
    }
    
    /**
     * Used to allow for dragging JLabels
     * 
     * Retrieved from http://stackoverflow.com/questions/6828684/java-mouseevent-check-if-pressed-down
     */
    private void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseDown = true;
            initThread();
        }
    }
    
    /**
     * Used to allow for dragging JLabels
     * 
     * Retrieved from http://stackoverflow.com/questions/6828684/java-mouseevent-check-if-pressed-down
     */
    private void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseDown = false;
        }
    }
    
    /**
     * Used to allow for dragging JLabels
     * 
     * Retrieved from http://stackoverflow.com/questions/6828684/java-mouseevent-check-if-pressed-down
     */
    private synchronized boolean checkAndMark() {
        if (isRunning) {
            return false;
        }
        isRunning = true;
        return true;
    }
    
    /**
     * Used to allow for dragging JLabels
     * 
     * Retrieved from http://stackoverflow.com/questions/6828684/java-mouseevent-check-if-pressed-down
     */
    private void initThread() {
        if (checkAndMark()) {
            new Thread() {
                @Override
                public void run() {
                    do {
                        // Do something
                    }
                    while (mouseDown);
                    isRunning = false;
                }
            }.start();
        }
    }
    
    /**
     * Initializes the graphical components
     */
    @SuppressWarnings("unchecked")                         
    private void initComponents() {
        // Instatiates the ship JLabels
        patrolBoat      = new JLabel();
        destroyer       = new JLabel();
        submarine       = new JLabel();
        battleship      = new JLabel();
        aircraftCarrier = new JLabel();
        
        // Instatiates playingField
        playingField    = new JLabel();
        playingField.setBounds(0, 0, 400, 400);

        // Sets close operation and title
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Battleship");

        patrolBoat.setIcon(new javax.swing.ImageIcon("len2.jpg")); // NOI18N
        patrolBoat.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                patrolBoatMousePressed(evt);
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                patrolBoatMouseReleased(evt);
            }
        });
        patrolBoat.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                patrolBoatMouseDragged(evt);
            }
        });

        destroyer.setIcon(new javax.swing.ImageIcon("len3.jpg")); // NOI18N
        destroyer.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                destroyerMousePressed(evt);
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                destroyerMouseReleased(evt);
            }
        });
        destroyer.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                destroyerMouseDragged(evt);
            }
        });

        submarine.setIcon(new javax.swing.ImageIcon("len3.jpg")); // NOI18N
        submarine.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                submarineMousePressed(evt);
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                submarineMouseReleased(evt);
            }
        });
        submarine.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                submarineMouseDragged(evt);
            }
        });

        battleship.setIcon(new javax.swing.ImageIcon("len4.jpg")); // NOI18N
        battleship.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                battleshipMousePressed(evt);
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                battleshipMouseReleased(evt);
            }
        });
        battleship.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                battleshipMouseDragged(evt);
            }
        });

        aircraftCarrier.setIcon(new javax.swing.ImageIcon("len5.jpg")); // NOI18N
        aircraftCarrier.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                aircraftCarrierMousePressed(evt);
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                aircraftCarrierMouseReleased(evt);
            }
        });
        aircraftCarrier.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                aircraftCarrierMouseDragged(evt);
            }
        });
        
        playingField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                playingFieldMouseClicked(event);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(patrolBoat)
                        .addGap(41, 41, 41)
                        .addComponent(destroyer)
                        .addGap(34, 34, 34)
                        .addComponent(submarine))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(battleship)
                        .addGap(35, 35, 35)
                        .addComponent(aircraftCarrier)))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(451, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(patrolBoat, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(submarine, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(destroyer, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(battleship)
                    .addComponent(aircraftCarrier))
                .addGap(19, 19, 19))
        );

        pack();
    }

    private void patrolBoatMousePressed(java.awt.event.MouseEvent evt) {                                        
        initialClick = evt.getPoint();
        mousePressed(evt);
    }                                       

    private void patrolBoatMouseDragged(java.awt.event.MouseEvent evt) {                                        
        dragJLabel(patrolBoat, evt);
    }                                       

    private void destroyerMousePressed(java.awt.event.MouseEvent evt) {                                       
        initialClick = evt.getPoint();
        mousePressed(evt);
    }                                      

    private void destroyerMouseDragged(java.awt.event.MouseEvent evt) {                                       
        dragJLabel(destroyer, evt);
    }                                      

    private void submarineMousePressed(java.awt.event.MouseEvent evt) {                                       
        initialClick = evt.getPoint();
        mousePressed(evt);
    }                                      

    private void submarineMouseDragged(java.awt.event.MouseEvent evt) {                                       
        dragJLabel(submarine, evt);
    }                                      

    private void battleshipMousePressed(java.awt.event.MouseEvent evt) {                                        
        initialClick = evt.getPoint();
        mousePressed(evt);
    }                                       

    private void battleshipMouseDragged(java.awt.event.MouseEvent evt) {                                        
        dragJLabel(battleship, evt);
    }                                       

    private void aircraftCarrierMousePressed(java.awt.event.MouseEvent evt) {                                             
        initialClick = evt.getPoint();
        mousePressed(evt);
    }                                            

    private void aircraftCarrierMouseDragged(java.awt.event.MouseEvent evt) {                                             
        dragJLabel(aircraftCarrier, evt);
    }                                            

    private void patrolBoatMouseReleased(java.awt.event.MouseEvent evt) {                                         
        placeShip(patrolBoat);
    }                                        

    private void destroyerMouseReleased(java.awt.event.MouseEvent evt) {                                        
        placeShip(destroyer);
    }                                       

    private void submarineMouseReleased(java.awt.event.MouseEvent evt) {                                        
        placeShip(submarine);
    }                                       

    private void battleshipMouseReleased(java.awt.event.MouseEvent evt) {                                         
        placeShip(battleship);
    }                                        

    private void aircraftCarrierMouseReleased(java.awt.event.MouseEvent evt) {                                              
        placeShip(aircraftCarrier);
    }

    /**
     * Handles MouseClicked events for playingField. Only executes meaningful
     * code if both playersTurn and setupDone are true.
     * 
     * NOTE: This method is not fully implemented yet.
     */
    private void playingFieldMouseClicked(MouseEvent event) {
        if (playersTurn && setupDone) {
            // Get mouse location
            int x = MouseInfo.getPointerInfo().getLocation().x;
            int y = MouseInfo.getPointerInfo().getLocation().y;
            
            // Record appropriate coordinates
            int[] coordinates = getCoordinates(x, y);
            
            // Let user know if they hit or missed
            
            // Check for game over
            
            // AI turn
            
            // Check for game over
        }
    }
    
    /**
     * Returns an array of integers indicating the coordinates of a mouse click
     * as indicated and required by playingFieldMouseClicked.
     * 
     * @param x The x position of the mouse at the time of the click
     * @param y The y position of the mouse at the time of the click
     * 
     * @return Array of integers indicating the coordinates of the mouse click
     */
    private int[] getCoordinates(int x, int y) {
        // Instantiates the return value
        int[] retval = new int[2];
        
        if (x > 20 && x < 40) {
            retval[0] = 1;
            retval[1] = whichColumn(y);
            return retval;
        }
        else if (x > 60 && x < 100) {
            retval[0] = 2;
            retval[1] = whichColumn(y);
            return retval;
        }
        else if (x > 100 && x < 140) {
            retval[0] = 3;
            retval[1] = whichColumn(y);
            return retval;
        }
        else if (x > 140 && x < 180) {
            retval[0] = 4;
            retval[1] = whichColumn(y);
            return retval;
        }
        else if (x > 180 && x < 220) {
            retval[0] = 5;
            retval[1] = whichColumn(y);
            return retval;
        }
        else if (x > 220 && x < 260) {
            retval[0] = 6;
            retval[1] = whichColumn(y);
            return retval;
        }
        else if (x > 260 && x < 300) {
            retval[0] = 7;
            retval[1] = whichColumn(y);
            return retval;
        }
        else if (x > 300 && x < 340) {
            retval[0] = 8;
            retval[1] = whichColumn(y);
            return retval;
        }
        else if (x > 340 && x < 380) {
            retval[0] = 9;
            retval[1] = whichColumn(y);
            return retval;
        }
        else if (x > 380 && x < 420) {
            retval[0] = 10;
            retval[1] = whichColumn(y);
            return retval;
        }
        else {
            // This exception is required to allow for the source code to
            // compile, since all return statements are inside if statements
            throw new Exception("Undefined exception. No appropriate return statement for rowLocation. Thrown in else of rowLocation.");
        }
    }
    
    /**
     * Returns an int representing the column the user clicked on.
     * 
     * @param y The y position of the mouse at the time of the click
     * 
     * @return The column the user clicked on
     */
    private int whichColumn(int y) {
        if (y > 20 && y < 60) {
            return 0;
        }
        else if (y > 60 && y < 100) {
            return 1;
        }
        else if (y > 100 && y < 140) {
            return 2;
        }
        else if (y > 140 && y < 180) {
            return 3;
        }
        else if (y > 180 && y < 220) {
            return 4;
        }
        else if (y > 220 && y < 260) {
            return 5;
        }
        else if (y > 260 && y < 300) {
            return 6;
        }
        else if (y > 300 && y < 340) {
            return 7;
        }
        else if (y > 340 && y < 380) {
            return 8;
        }
        else if (y > 380 && y < 420) {
            return 9;
        }
        else {
            // This exception is required to allow for the source code to
            // compile, since all return statements are inside if statements
            throw new Exception("Undefined exception. No appropriate return statement for rowLocation. Thrown in else of rowLocation.");
        }
    }
    
    /**
     * The main method
     */
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GraphicalInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GraphicalInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GraphicalInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GraphicalInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Battleship2().setVisible(true);
            }
        });
    }                 
}
