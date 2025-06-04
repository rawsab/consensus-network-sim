package cmg.cnsim.testblockchain.reporter;

import cmg.cnsim.testblockchain.node.NodeState;
import cmg.cnsim.engine.Reporter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TransactionReporter extends Reporter {

    protected static ArrayList<String> trxLog = new ArrayList<>();
    protected static ArrayList<String> eventLog = new ArrayList<>();
    protected static String path = "./"; // Path to save the log files
    protected static String runId = "run_" + System.currentTimeMillis(); // Unique run identifier

    static {
        trxLog.add("SimTime\tClientID\tSender\tReceiver\tAmount\tFee\tDetails");
        eventLog.add("SimTime\tNodeId\tClientId\tEventType\tEpoch\tDetails");
    }


    public static void logTrx(String nodeId, int sender, int receiver, double amount, double fee, String details , long time) {
        trxLog.add(time + "\t" + nodeId + "\t" + sender + "\t" + receiver + "\t" + amount + "\t" + fee + "\t" + details);
    }

    public static void logTrxEvent(String nodeId, int clientId, String EventType, int epoch, String details, long time) {
        eventLog.add(time + "\t" + nodeId + "\t" + clientId + "\t" + EventType + "\t" + epoch + "\t" + details);
    }


    public static void flushTrxEventLog() {
        FileWriter writer;
        try {
            writer = new FileWriter(Reporter.path + "NodeTrxEventLog - " + Reporter.runId + ".csv");
            for (String str : eventLog) {
                writer.write(str + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void flushTrxLog() {
        FileWriter writer;
        try {
            writer = new FileWriter(Reporter.path + "NodeTrxLog - " + Reporter.runId + ".csv");
            for (String str : trxLog) {
                writer.write(str + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
