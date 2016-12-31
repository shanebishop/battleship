/**
 * Programmer:   Shane Bishop
 * Date:         December 24, 2016
 * Program Name: Battleship2
 * Class:        AIShip
 */
package battleship2;

public class AIShip {
    // Class constants used for differentiating between ships
    public static final int PATROL_BOAT      = 1;
    public static final int DESTROYER        = 2;
    public static final int SUBMARINE        = 3;
    public static final int BATTLESHIP       = 4;
    public static final int AIRCRAFT_CARRIER = 5;
    
    // Indicates whether or not the ship is destroyed
    private boolean destroyed;
    
    // Holds the approprate int for the ship, indiciating which ship this is
    private int ship;
    
    /**
     * The constructor.
     * 
     * @param ship An integer representation of which ship this is
     */
    public AIShip(int ship) {
        this.ship = ship;  // Initializes the field ship variable to the argument
        destroyed = false; // Initializes destroyed to false
    }
    
    /**
     * Returns destroyed.
     * 
     * @return destroyed - true if ship is destroyed, false if otherwise
     */
    public boolean getDestroyed() {
        return destroyed;
    }
    
    /**
     * Sets destroyed.
     * 
     * @param status The boolean value to set status to
     */
    public void setDestroyed(boolean status) {
        destroyed = status;
    }
    
    /**
     * Returns the ship integer.
     * 
     * @return The ship integer - indicates which ship this is
     */
    public int getShipNumber() {
        return ship;
    }
}
