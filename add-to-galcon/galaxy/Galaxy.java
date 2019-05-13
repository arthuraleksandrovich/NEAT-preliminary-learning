package galaxy;

import java.util.ArrayList;
import java.util.Iterator;

final class Galaxy {
    private ArrayList<Planet> planets;
    private ArrayList<Fleet> fleets = new ArrayList<Fleet>();

    /**
     * Updates all Planets and Fleets
     */
    void update() {
        for (Planet p : planets) {
            p.update();
        }

        Iterator<Fleet> fleeterator = fleets.iterator();
        while (fleeterator.hasNext()) {
            Fleet f = fleeterator.next();
            f.update();
            if (f.hasHit()) {
                fleeterator.remove();
            }
        }
    }

    /**
     * Register a new Fleet that was just launched.
     * Does not register fleets with less than 1 unit.
     * @param f The Fleet being registered
     */
    void addFleet(Fleet f) {
        if (f.getNumUnits() > 0) {
            fleets.add(f);
        }

    }

    /**
     * @return A list of all Fleets
     */
    ArrayList<Fleet> getFleets() {
        return new ArrayList<Fleet>(fleets);
    }

    /**
     * @return A list of all Planets
     */
    ArrayList<Planet> getPlanets() {
        return new ArrayList<Planet>(planets);
    }

    /**
     * Determines a winner. A Player wins when they are the only Player with units remaining.
     * @return The winning Player. Null for game still ongoing.
     */
    Player checkWinner() {
        Player winner = checkPlanetWinner();
        for (Fleet f : fleets) {
            if (!f.ownedBy(winner)) {
                return null;
            }
        }
        return winner;
    }

    /**
     * Checks if a Player has a potential win condition given the state of the Planets.
     * That is, a Player who is the only Player to control a Planet.
     * @return A potentially winning Player. Null for game still ongoing.
     */
    Player checkPlanetWinner() {
        int i = -1;
        Player p = null;
        // Finds first planet owned by a player
        while (++i < planets.size() && (p = planets.get(i).getOwner()) == null) {
            ;
        }
        // Sees if any planets are owned by another player
        while (++i < planets.size() && !planets.get(i).ownedByOpponentOf(p)) {
            ;
        }
        return i == planets.size() ? p : null;
    }

    /**
     * Starts a new game on the given Map. Registers the Planets.
     * @param newMap The Planets to register.
     */
    void nextGame(Planet[] newMap) {
        if (planets != null) {
            for (Planet p : planets) {
                p.terminate();
            }
            fleets.clear();
        }
        planets = new ArrayList<Planet>();
        
        for (Planet p: newMap) {
        	planets.add(p);
        }
    }

    /**
     * Will eventually have all the info needed to write games to file.
     */
    @Override
    public String toString() {
        return "";
    }

    /**
     * Searches through all Fleets and Planets to sum the units owned by the given Player.
     * @param p The Player to count the units of.
     * @return The number of units owned by that Player.
     */
    int numUnitsOwnedBy(Player p) {
        return numUnitsInPlanets(p) + numUnitsInFleets(p);
    }

    /**
     * @param p The Player being counted
     * @return How many units that Player has in their Planets
     */
    int numUnitsInPlanets(Player p) {
        int count = 0;
        for (Planet f : planets) {
            if (f.ownedBy(p)) {
                count += f.numUnits;
            }
        }
        return count;
    }

    /**
     * @param p The Player being counted
     * @return How many units that Player has in their Fleets
     */
    int numUnitsInFleets(Player p) {
        int count = 0;
        for (Fleet f : fleets) {
            if (f.ownedBy(p)) {
                count += f.getNumUnits();
            }
        }
        return count;
    }
}
