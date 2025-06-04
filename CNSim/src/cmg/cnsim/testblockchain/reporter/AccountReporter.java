package cmg.cnsim.testblockchain.reporter;

import cmg.cnsim.engine.Reporter;

import java.util.ArrayList;
import java.io.FileWriter;

public class AccountReporter extends Reporter {
    protected static ArrayList<String> accountLog = new ArrayList<>();

    static {
        accountLog.add("SimTime\t AccountID\t NodeID\t Event\t Balance\t State\t Nonce");
    }

    public static void logAccount(int accountID, String nodeID, String event, long balance, String state, long nonce, long time) {
        accountLog.add(time + "\t" + accountID + "\t" + nodeID + "\t" + event + "\t" + balance + "\t" + state + "\t" + nonce);
    }

    public static void flushAccountLog() {
        FileWriter writer;
        try {
            writer = new FileWriter(Reporter.path + "AccountLog - " + Reporter.runId + ".csv");
            for (String str : accountLog) {
                writer.write(str + System.lineSeparator());
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
