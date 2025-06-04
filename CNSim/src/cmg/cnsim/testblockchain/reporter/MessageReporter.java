package cmg.cnsim.testblockchain.reporter;

import cmg.cnsim.engine.Reporter;

import java.io.FileWriter;
import java.util.ArrayList;

public class MessageReporter extends Reporter {
    protected static ArrayList<String> clientMessageLog = new ArrayList<>();
    protected static ArrayList<String> nodeMessageLog = new ArrayList<>();
    protected static ArrayList<String> stateMessageLog = new ArrayList<>();

    static {
        clientMessageLog.add("SimTime\tSender\tReceiver\tType\tDetails");
        nodeMessageLog.add("SimTime\tSender\tReceiver\tType\tDetails");
        stateMessageLog.add("SimTime\tSender\tReceiver\tType\tDetails");
    }

    public static void logClientMessage(String sender, String receiver, String type, String details, long time) {
        clientMessageLog.add(time + "\t" + sender + "\t" + receiver + "\t" + type + "\t" + details);
    }

    public static void logNodeMessage(String sender, String receiver, String type, String details, long time) {
        nodeMessageLog.add(time + "\t" + sender + "\t" + receiver + "\t" + type + "\t" + details);
    }

    public static void logStateMessage(String sender, String receiver, String type, String details, long time) {
        stateMessageLog.add(time + "\t" + sender + "\t" + receiver + "\t" + type + "\t" + details);
    }

    public static void flushClientLog() {
        FileWriter writer;
        try {
            writer = new FileWriter(Reporter.path + "ClientMessageLog - " + Reporter.runId + ".csv");
            for (String str : clientMessageLog) {
                writer.write(str + System.lineSeparator());
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void flushNodeLog() {
        FileWriter writer;
        try {
            writer = new FileWriter(Reporter.path + "NodeMessageLog - " + Reporter.runId + ".csv");
            for (String str : nodeMessageLog) {
                writer.write(str + System.lineSeparator());
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void flushStateLog() {
        FileWriter writer;
        try {
            writer = new FileWriter(Reporter.path + "StateMessageLog - " + Reporter.runId + ".csv");
            for (String str : stateMessageLog) {
                writer.write(str + System.lineSeparator());
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
