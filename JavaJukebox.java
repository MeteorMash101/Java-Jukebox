import java.util.*;
// Might re-name => "Jukebox", "Virtual Jukebox".
public class JavaJukebox {
    // Global variables...
    // EDIT: make most of these private?
    // custom colors for printing to terminal ;)
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String AUDIO_FILE_PATH = "./test-audio/"; // concatenate "songName" at the end here.
    private static final JavaSoundAPI PLAYER = new JavaSoundAPI();
    private static HashMap<String, String> tempSongNames = new HashMap<String, String>(); // [DEV-MODE]: to make it easier for song names
    private static boolean songIsPlaying = false;
    private static boolean songIsPaused = false;
    private static ArrayList<String> commandsHistory = new ArrayList<String>(); // array of commands
    private static ArrayList<String> songHistory = new ArrayList<String>(); // array of SongObjects (mostly keeping track of their filesNames for PLAYER)
    private static HashMap<String, SongObject> songNameToSongObj = new HashMap<String, SongObject>(); // helper for history
    private static ArrayList<SongObject> songQueue = new ArrayList<SongObject>(); // array of SongObjects (mostly keeping track of their filesNames for PLAYER)
    public static void main(String[] args) {
        initDevMode(); 
        welcomeMessage();
        displayOptions();
        Scanner scanner = new Scanner(System.in);
        String input = "";

        while (true) {
            System.out.print(ANSI_GREEN + "Java Jukebox>  " + ANSI_RESET);
            input = scanner.nextLine();
            // Parse the input.
            if (input.length() == 0) {
                printError("Please enter something!");
            } else {
                if (input.equals("exit")) {
                    break;
                }
                executeCommand(input);
            }
        }
        printSuccess("Thanks for playing!");
        scanner.close();
    }
    // [DEV-MODE]: helper
    private static void initDevMode() {
        // actual song name mappings 
        tempSongNames.put("song_1", "Virtual Love");
        tempSongNames.put("song_2", "Virtual Paradise");
        tempSongNames.put("song_3", "Matches");
        tempSongNames.put("song_4", "Famous");
        tempSongNames.put("song_5", "You Broke My Heart Again");
        // load demo disks...
        executeCommand("ld NocturnalZ - Virtual Love");
        executeCommand("ld AK x LYNX ft. Veela - Virtual Paradise");
        executeCommand("ld Ephixa & Stephen Walking & Subtact & Aaron Richards - Matches");
        executeCommand("ld Archie - Famous");
        executeCommand("ld Teqkoi & Aiko - You Broke My Heart Again");
    }
    private static void displayOptions() {
        String[] listOfCmds = new String[] {"play", "stop", "resume", "get song info", "replay", "loop", "set loop", "queue", "queue first", "skip", "previous", "previous command", "load disk", "view song list", "view song history"};
        HashMap<String, String[]> cmdToCMDSpecs = new HashMap<String, String[]>();
        HashMap<String, String[]> cmdToDescMap = new HashMap<String, String[]>();
        // Command -> Specs Mapping
        cmdToCMDSpecs.put("play", new String[] {"p {song_name}"});
        cmdToCMDSpecs.put("kill", new String[] {"k"});
        cmdToCMDSpecs.put("stop", new String[] {"s"});
        cmdToCMDSpecs.put("resume", new String[] {"r"});
        cmdToCMDSpecs.put("get song info", new String[] {"gsi", "gsi {song_name}"});
        cmdToCMDSpecs.put("replay", new String[] {"rp"});
        cmdToCMDSpecs.put("loop", new String[] {"lp {2 - N}"});
        cmdToCMDSpecs.put("set loop", new String[] {"setlp {N} {N}", "setlp {N} {N} {song_name}"});
        cmdToCMDSpecs.put("queue", new String[] {"q {song_name}"});
        cmdToCMDSpecs.put("queue first", new String[] {"qf {song_name}"});
        cmdToCMDSpecs.put("skip", new String[] {"sk"});
        cmdToCMDSpecs.put("previous", new String[] {"prev"});
        cmdToCMDSpecs.put("previous command", new String[] {"<", "< {N}"});
        cmdToCMDSpecs.put("load disk", new String[] {"ld {artist_name} - {song_name}"});
        cmdToCMDSpecs.put("view song list", new String[] {"vsl"});
        cmdToCMDSpecs.put("view song history", new String[] {"vsh"});

        // Command -> Description Mapping
        cmdToDescMap.put("play", new String[] {"to play a song!"});
        cmdToDescMap.put("kill", new String[] {"to kill/end currently playing song. (a song must be playing at time of call)"});
        cmdToDescMap.put("stop", new String[] {"to stop/pause currently playing song. (a song must be playing at time of call)"});
        cmdToDescMap.put("resume", new String[] {"to resume playing song that was stopped/paused."});
        cmdToDescMap.put("get song info", new String[] {"get the song name, artist name, and duration of currently playing song,", "get the song name, artist name, and duration of specific song."});
        cmdToDescMap.put("replay", new String[] {"replay current song."});
        cmdToDescMap.put("loop", new String[] {"loop current song x2 times or more."});
        cmdToDescMap.put("set loop", new String[] {"set loop of current song from N frame to N frame,", "set loop of specific song from N frame to N frame."});
        cmdToDescMap.put("queue", new String[] {"queue a different song. (to be played next)"});
        cmdToDescMap.put("queue first", new String[] {"queue a different song. (to be played IMMEDIATELY next)"});
        cmdToDescMap.put("skip", new String[] {"skip current song."});
        cmdToDescMap.put("previous", new String[] {"go back to previous song."});
        cmdToDescMap.put("previous command", new String[] {"re-use previous command", "re-use previous (Nth) command (must be in-bounds!)"});
        cmdToDescMap.put("load disk", new String[] {"load a song into the Jukebox. Make sure to follow the \"{song_name} - {artist_name}\" format!"}); // "In demo mode we have pre-loaded 5 songs already, you can view them using the view songs command: \"vs\""
        cmdToDescMap.put("view song list", new String[] {"view songs currently loaded in the Jukebox."});
        cmdToDescMap.put("view song history", new String[] {"view listening history."});


        printOptions(listOfCmds, cmdToCMDSpecs, cmdToDescMap);   
        // Optional (for now...) to implement (MUCH harder!)
        // have favorites implement priority queue (with each favorite score having a 'fav-score'?)
        // cmdToCMDSpecs.put("add to favorites", "af");
        // cmdToCMDSpecs.put("play favorites", "pf");
        // cmdToCMDSpecs.put("load playlist", "loadp {playlist_name}");
        // cmdToCMDSpecs.put("shuffle", "shuf, shuf {playlist_name}");
        // cmdToCMDSpecs.put("speed up", "su");
        // cmdToCMDSpecs.put("slow down", "sd");
        // cmdToCMDSpecs.put("reverse", "rev");
        // cmdToCMDSpecs.put("reset", "res");
        // cmdToCMDSpecs.put("startBack", "sb"); // start back at beg.
    }

    private static void welcomeMessage() {
        System.out.println("Welcome to Java Jukebox!");
        System.out.println("First, move all of your .WAV sound files to ./test-audio directory.");
        System.out.println("Now, start by entering one of the following commands!");
        
    }
    private static void printOptions(String[] listOfCmds, HashMap<String, String[]> cmdToCMDSpecs, HashMap<String, String[]> cmdToDescMap) {
        for (String command : listOfCmds) {
            System.out.println("[" + command + "]");
            for (int i = 0; i < cmdToCMDSpecs.get(command).length; i++) {
                System.out.println("\t" + "command: " + "\"" + cmdToCMDSpecs.get(command)[i] + "\"");
                System.out.println("\t" + "desc: " + cmdToDescMap.get(command)[i]);
            }
        }
    }
    private static ArrayList<String> parse(String input) {
        int ptr = 0; // ptr to iterate through the input
        ArrayList<String> commandLine = new ArrayList<String>(); // the entire command as a line
        StringBuilder currWord = new StringBuilder(); // adding chars to this
        while (ptr < input.length()) {
            while (ptr < input.length() && input.charAt(ptr) == ' ' && currWord.length() == 0) {
                ptr++;
            }
            // if we broke b/c of bounds, we want to break out of outer loop as well.
            if (ptr == input.length()) { 
                break;
            }
            char currChar = input.charAt(ptr);
            if (currChar == ' ') {
                commandLine.add(currWord.toString());
                currWord.setLength(0); // reset word
            } else {
                currWord.append(currChar);
            }
            ptr++;
        }
        if (currWord.length() > 0) { // leftover word
            commandLine.add(currWord.toString());
        }
        return commandLine;
    }
    private static void executeCommand(String input) {
        ArrayList<String> commandLine = parse(input);
        String commandKey = commandLine.get(0);
        // We don't want to add these cmds to our cmds history...
        if (!commandKey.equals("<")) {
            commandsHistory.add(input);
        }
        if (commandKey.equals("ld")) {
            adjustParseForLoad(commandLine, input);
        }
        switch (commandKey) {
            // EDIT: account for error handling in each of these methods!
            case "p":
                play(commandLine);
                break;
            case "k":
                kill(commandLine);
                break;
            case "s":
                stop(commandLine);
                break;
            case "r":
                resume(commandLine);
                break;
            case "gsi":
                getSongInfo(commandLine);
                break;
            case "ld":
                loadDisk(commandLine);
                break;
            case "vsl":
                viewSongList(commandLine);
                break;
            case "vsh":
                viewSongHistory(commandLine);
                break;
            case "rp":
                break;
            case "<":
                previousCommand(commandLine, commandsHistory);
                break;
            default:
                printError(String.format("ERROR: Command %s not found...", commandKey));
                break;
        }
        return;
    }
    private static void adjustParseForLoad(ArrayList<String> commandLine, String input) {
        if (!input.contains("-")) { // if input doesn't contain name/title splitter "-", we can't parse it.
            printError("ERROR: ls arguements are not valid...");
            return;
        }
        while (commandLine.size() > 1) { // we only want the "ld" in the command line (get rid of the wrongly parsed stuff)
            commandLine.remove(commandLine.size() - 1);
        }
        String[] parts;
        parts = input.split("-");
        // using trim to get rid of trailing whitespaces...
        parts[0] = parts[0].trim(); // "ld" still included here
        parts[0] = parts[0].substring(2).trim(); // we want to start after "ld" (trim again as well.)
        parts[1] = parts[1].trim();
        commandLine.add(parts[0]); // song name
        commandLine.add(parts[1]); // artist name
        commandLine.add(parts[0] + " " + "-" + " " + parts[1]); // file name
    }
    private static void play(ArrayList<String> commandLine) {
        switch (commandLine.size()) {
            case 2:
                // EDIT: in cmd line we'll be entering "Song_#" for convenience. Change later.
                String songName = tempSongNames.get(commandLine.get(1));
                if (songIsPlaying) { // to prevent two songs from playing concurrently.
                    PLAYER.kill();
                    songIsPlaying = false;
                }
                PLAYER.play(AUDIO_FILE_PATH + songNameToSongObj.get(songName).getFileName() + ".wav");
                songIsPlaying = true;
                songHistory.add(songName);
                printSuccess("SUCCESS: Playing song!");
                break;
            default:
                printError("ERROR: Invalid # of args. supplied for command...");    
                break;
        }
    }
    private static void previousCommand(ArrayList<String> commandLine, ArrayList<String> commandsHistory) {
        if (commandsHistory.size() <= 0) {
            printError("ERROR: commands history is empty...");
            return;
        }
        String prevCmd;
        switch (commandLine.size()) {
            case 1:
                prevCmd = commandsHistory.get(commandsHistory.size() - 1);
                printSuccess("Executing previous command...");
                printSuccess(prevCmd.toString());
                executeCommand(prevCmd);
                break;
            case 2:
                int nth = Integer.parseInt(commandLine.get(1));
                if (nth <= 1) { // must be >= 2...
                    printError("ERROR: nth arguement must be >= 2...");
                } else if (commandsHistory.size() - nth < 0) {
                    printError("ERROR: nth value leads to out of bounds...");
                } else {
                    // We have valid nth! Execute the previous command.
                    prevCmd = commandsHistory.get(commandsHistory.size() - nth);
                    printSuccess("Executing previous command...");
                    printSuccess(prevCmd);
                    executeCommand(prevCmd);
                }
                break;
            default:
                printError("ERROR: Invalid # of args. supplied for command...");
                break;
        }
    }
    private static void kill(ArrayList<String> commandLine) {
        if (songIsPlaying) { // a song must be playing...
            PLAYER.kill();
            songIsPlaying = false;
            printSuccess("SUCCESS: Killed song. xD");
        } else {
            printError("ERROR: No song is currently playing...");
        }
    }
    private static void stop(ArrayList<String> commandLine) {
        if (songIsPlaying) { // a song must be playing...
            PLAYER.stop();
            songIsPlaying = false;
            songIsPaused = true;
            printSuccess("SUCCESS: Song stopped.");
        } else {
            printError("ERROR: No song is currently playing...");
        }
    }
    private static void resume(ArrayList<String> commandLine) {
        if (songIsPaused) { // a song must been paused.
            PLAYER.resume();
            songIsPlaying = true; // assertion.
            songIsPaused = false;
            printSuccess("SUCCESS: Song is paused.");
        } else {
            printError("ERROR: A song hasn't been paused or a song is currently playing...");
        }
    }
    private static void getSongInfo(ArrayList<String> commandLine) {
        switch (commandLine.size()) {
            case 1:
                // for current song...
                printSuccess("Song info: ");
                System.out.println(songNameToSongObj.get(songHistory.get(songHistory.size() - 1)));
                break;
            case 2:
                // for specific song...
                String songName = tempSongNames.get(commandLine.get(1));
                printSuccess("Song info for " + songName + ": ");
                System.out.println(songNameToSongObj.get(songName));
                break;
            default:
                printError("ERROR: Invalid # of args. supplied for command...");
                break;
        }
    }
    private static void loadDisk(ArrayList<String> commandLine) {
        if (commandLine.size() == 4) {
            String artistName = commandLine.get(1);
            String songName = commandLine.get(2);
            String fileName = commandLine.get(3);
            SongObject songObj = new SongObject(artistName, songName, 1000, fileName); // EDIT: temp dur.
            songQueue.add(songObj);
            songNameToSongObj.put(songName, songObj);
            printSuccess("SUCCESS: Loaded disk!");
        } else {
            printError("ERROR: ld arguements length is invalid...(after parsed)");
        }
    }
    private static void viewSongList(ArrayList<String> commandLine) {
        printSuccess("Song List:");
        for (int i = 0; i < songQueue.size(); i++) {
            System.out.println(songQueue.get(i).getSongName());
        }
    }
    private static void viewSongHistory(ArrayList<String> commandLine) {
        printSuccess("Listening history:");
        for (int i = 0; i < songHistory.size(); i++) {
            System.out.println(songHistory.get(i));
        }
    }
    private static void printSuccess(String msg) {
        System.out.println(ANSI_CYAN + msg + ANSI_RESET);
    }
    private static void printError(String msg) {
        System.out.println(ANSI_RED + msg + ANSI_RESET);
    }
}