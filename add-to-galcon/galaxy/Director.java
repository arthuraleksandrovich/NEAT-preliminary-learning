package galaxy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Director {
    private int rounds = 0;
    private Matcher mm = null;
    protected Visualizer visualizer = GameSettings.getVisualizer(this);
    private MapMaker maps = GameSettings.getMapMaker();
    protected Galaxy galaxy = new Galaxy();
    private LinkedList<Player> active;
    private HashMap<Player, Integer> numUnitsInFleets = new HashMap<Player, Integer>(),
            numUnitsInPlanets = new HashMap<Player, Integer>();
    private int tic = 0;
    protected Player[] players = GameSettings.getPlayers();

    private static Director director;
    private static Timer game;
    private static boolean pause = false;

    /**
     * The game loop.
     * @param args
     */
    public static void main(String[] args) {
    	director = new Director();
    	game = new Timer();
    	
        if (director.usingVisualizer()) {
            game.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!pause && !director.done()) {
                        director.next();
                    }
                }
            }, 0, GameSettings.FRAME_TIME);
        } else {
            while (!director.done()) {
                if (!pause) {
                    director.next();
                }
            }
            game.cancel();
        }
    }

    /**
     * Creates a new Director.
     */
    protected Director() {
        for (int i = 0; i < GameSettings.PLAYERS_PER_GAME; i++) {
            mm = new Matcher(mm);
        }

        // TODO can this be done with a finishGame call?
        newGame(maps.getNewMap(mm.nextMatchup()));
    }

    /**
     * Determines if all rounds have been played.
     * @return Whether all rounds have been played.
     */
    boolean done() {
        return rounds > GameSettings.NUM_ROUNDS;
    }

    /**
     * Does all work for a game tic.
     */
    void next() {
        numUnitsInFleets.clear();
        numUnitsInPlanets.clear();

        for (Player p : active) {
            numUnitsInFleets.put(p, galaxy.numUnitsInFleets(p));
            numUnitsInPlanets.put(p, galaxy.numUnitsInPlanets(p));
        }

        LinkedList<Action> actions = new LinkedList<Action>();

        for (Player p : active) {
            actions.addAll(p.turn(galaxy.getFleets()));
        }	

        for (Action a : actions) {
            galaxy.addFleet(a.doAction());
        }

        galaxy.update();
        if (visualizer != null) {
            visualizer.update(galaxy.getFleets());
        }

        tic++;

        Player winner = galaxy.checkWinner();
        if (winner != null) {
            finishGame(winner);
        } else if (tic > GameSettings.TIC_LIMIT) {
            finishGame(null);
        }
    }

    /**
     * Finishes a game.
     */
    void finishGame(Player winner, Planet[] newMap) {
    	if (GameSettings.debugMode) {
    		System.out.println(winner == null ? "NULL" : winner.NAME + " wins!");
    	}
    	
        for (Player p : active) {
            p.endGame(winner);
        }
        newGame(newMap);
    }

    /**
     * Finishes a game.
     */
    void finishGame(Player winner) {
        finishGame(winner, maps.hasRevsered() || !GameSettings.REVERSE_EACH_MAP ? maps.getNewMap(mm.nextMatchup())
                : maps.getReversedMap());
    }

    /**
     * @return Whether or not a visualizer is in use.
     */
    boolean usingVisualizer() {
        return visualizer != null;
    }

    /**
     * Starts a new game
     * @param map The map for the new game.
     */
    private void newGame(Planet[] map) {
        tic = 0;

        galaxy.nextGame(map);

        for (Player p : active) {
            p.newGame(galaxy.getPlanets());
        }

        if (usingVisualizer()) {
            visualizer.nextGame(active, galaxy.getPlanets());
        }
    }

    /**
     * When manually skipped game
     */
    void skipGame() {
        finishGame(null);
    }

    void restartGame() {
        finishGame(null, maps.getExistingMap());
    }

    void reverseMap() {
        finishGame(null, maps.getReversedMap());
    }

    void togglePause() {
        pause = !pause;
    }

    final int numUnitsOwnedBy(Player p) {
        return numUnitsInPlanets(p) + numUnitsInFleets(p);
    }

    final int numUnitsInPlanets(Player p) {
        Integer numUnits = numUnitsInPlanets.get(p);
        return numUnits == null ? 0 : numUnits;
    }

    final int numUnitsInFleets(Player p) {
        Integer numUnits = numUnitsInFleets.get(p);
        return numUnits == null ? 0 : numUnits;
    }

    /******************* MATCHER *****************/

    /**
     * Creates combinations of Players to determine who plays next.
     */
    class Matcher {
        private Matcher next, prev;
        private int player;

        /**
         * Creates a new Matcher. There will be an equal number of Matchers and active Players.
         * @param previous The Matcher created before this one.
         */
        Matcher(Matcher previous) {
            prev = previous;
            if (prev != null) {
                player = prev.player + 1;
                prev.next = this;
            } else {
                player = 0;
            }
        }

        /**
         * Sets the list of active Players.
         * @return The set of Players who will play next
         */
        LinkedList<Player> nextMatchup() {
            active = getPlayers();
            update();
            return active;
        }

        /**
         * Increments this Matcher to point to the next Player for the next game.
         */
        void update() {
            player++;
            if (player == players.length) {
                player = ++prev.player + 1;
                if (player == players.length) {
                    player = prev.overflow();
                }
            }
        }

        /**
         * When a Matcher reaches the end of the Player list it must increment the Matcher behind it.
         * @return The value for the Matcher after this to be set to.
         */
        int overflow() {
            if (prev == null) { // Full set of games completed - reset to start
                rounds++;
                player = 0;
                Matcher search = next;
                while (search != null) {
                    search.player = search.prev.player + 1;
                    search = search.next;
                }
                return 1; // This will have been called by its own 'next'
            } else {
                if (prev.player == player - 2) {
                    prev.player++;
                    player = prev.overflow();
                } else {
                    player = ++prev.player + 1;
                }
                return player + 1;
            }
        }

        /**
         * @return The set of Players who will play next
         */
        LinkedList<Player> getPlayers() {
            LinkedList<Player> set = prev != null ? prev.getPlayers() : new LinkedList<Player>();
            set.add(players[player]);
            return set;
        }
    }
}
