package galaxy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class GameSettings {
    private static HashMap<String, String> settings = getSettings();
    
    //private static boolean stringSourceSet=false;
    //private static String stringSource=null;
    
    /*public static void setStringSource(String source) {
    	if (source==null) {
    		stringSource=source;
    		stringSourceSet=true;
    		settings=getSettings();
    	}else {
    		stringSource=null;
    		stringSourceSet=false;
    		settings=getSettings(); 
    	}
    }*/
    
    public static void setProperty(String property, String value) {
    	settings.put(property, value);
    }

    private static HashMap<String, String> getSettings() {
        HashMap<String, String> map = new HashMap<>();
        try {
            Scanner reader;
            //if (stringSourceSet) {
            //	reader=new Scanner(stringSource);
            //}else {
            	reader = new Scanner(new FileReader("settings/SETTINGS"));
            //}
            
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                map.put(line.substring(0, line.indexOf(':')), line.substring(line.indexOf(':') + 2));
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return map;
    }

    public static final Coords DIMENSIONS = getDimensions();

    private static Coords getDimensions() {
        String[] split = settings.get("dimensions").split(" ");
        double[] dims = new double[split.length];
        for (int i = 0; i < dims.length; i++) {
            dims[i] = Double.parseDouble(split[i]);
        }
        return new Coords(dims);
    }

    private static String logFile = settings.get("logfile");

    public static final boolean debugMode = settings.get("debug").equals("true"),
            REVERSE_EACH_MAP = settings.get("reversemaps").equals("true");

    private static BufferedWriter gameLog = makeLogFile(logFile);

    static Player[] getPlayers() {
        String[] split = settings.get("players").split(" ");
        Player[] players = new Player[split.length];
        for (int i = 0; i < players.length; i++) {
            try {
                players[i] = (Player)(Class.forName("ais." + split[i]).newInstance());
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        return players;
    }

    static Visualizer getVisualizer(Director director) {
        try {
            Visualizer v = (Visualizer)(Class.forName("visualizers." + settings.get("visualizer")))
                    .getConstructor(double[].class).newInstance(getDimensions().getCoords());
            v.setDirector(director);
            return v;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            if (GameSettings.debugMode) {System.out.println("Running without visualizer");}
            return null;
        }
    }

    static MapMaker getMapMaker() {
        try {
            return (MapMaker)(Class.forName("mapmakers." + settings.get("mapmaker")).newInstance());
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return null;
    }

    public static final double FLEET_SPEED = Double.parseDouble(settings.get("fleetspeed"));

    public static final int NUM_PLANETS = Integer.parseInt(settings.get("numplanets")),
            MAX_RADIUS = Integer.parseInt(settings.get("maxradius")),
            MIN_RADIUS = Integer.parseInt(settings.get("minradius")),
            MAX_NEUTRAL_UNITS = Integer.parseInt(settings.get("maxneutralunits")),
            MIN_PRODUCE_TIME = Integer.parseInt(settings.get("minproducetime")),
            MAX_PRODUCE_TIME = Integer.parseInt(settings.get("maxproducetime")),
            NUM_ROUNDS = Integer.parseInt(settings.get("numrounds")),
            TIC_LIMIT = Integer.parseInt(settings.get("ticlimit")),
            FRAME_TIME = Integer.parseInt(settings.get("frametime")),
            PLAYERS_PER_GAME = Integer.parseInt(settings.get("playerspergame"));

    static final BufferedWriter makeLogFile(String filename) {
        if (filename.equals("none")) {
            return null;
        }
        filename = filename.equals("default") ? "galconset-" + formatDate(new Date()) : filename;
        try {
            return new BufferedWriter(new FileWriter(new File(filename)));
        } catch (IOException e) {
            System.err.println("Couldn't make log file.");
            System.exit(0);
            return null;
        }
    }

    static final void writeToLog(String str) {
        try {
            gameLog.write(str);
        } catch (IOException e) {
            System.err.println("Couldn't write to log file");
        }
    }

    public static final String formatDate(Date date) {
        return "" + date.getTime();
    }

}
