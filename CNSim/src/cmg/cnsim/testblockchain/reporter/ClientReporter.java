package cmg.cnsim.testblockchain.reporter;

import cmg.cnsim.engine.Reporter;

import java.io.FileWriter;
import java.util.ArrayList;

public class ClientReporter extends Reporter {
    protected static ArrayList<String> clientLog = new ArrayList<>();

    static {
        clientLog.add("SimTime\tClientID\tType\tContent");
    }

    public static void logClient(int clientId, String type, String content, long time) {
        clientLog.add(time + "\t" + clientId + "\t" + type + "\t" + content);
    }

    public static void flushClientLog() {
        FileWriter writer;
        try {
            writer = new FileWriter(Reporter.path + "ClientLog - " + Reporter.runId + ".csv");
            for (String str : clientLog) {
                writer.write(str + System.lineSeparator());
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
