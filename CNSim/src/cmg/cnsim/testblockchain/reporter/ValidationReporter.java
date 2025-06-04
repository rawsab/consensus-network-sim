package cmg.cnsim.testblockchain.reporter;

import cmg.cnsim.testblockchain.node.NodeState;
import cmg.cnsim.engine.Reporter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ValidationReporter extends Reporter {

    protected static ArrayList<String> eventLog = new ArrayList<>();
    protected static ArrayList<String> stateLog = new ArrayList<>();
    protected static String path = "./"; // Path to save the log files
    protected static String runId = "run_" + System.currentTimeMillis(); // Unique run identifier

    static {
        eventLog.add("SimTime,EventType,NodeID,Details");
        stateLog.add("SimTime,NodeID,OldState,NewState");
    }


    public static void logInitialization(String nodeId, long time) {
        eventLog.add(time + ",Initialization," + nodeId + ",Node initialized");
    }

    public static void logRegistration(String nodeId, long time, int epoch) {
        eventLog.add(time + ",Registration," + nodeId + ",Registered in epoch " + epoch);
    }

    public static void logStateChange(String nodeId, NodeState oldState, NodeState newState, long time) {
        stateLog.add(time + "," + nodeId + "," + oldState + "," + newState);
    }

    public static void logVoteReceived(String nodeId, int receivedNodeID, long time) {
        eventLog.add(time + ",VoteReceived," + nodeId + ",Received vote from node " + receivedNodeID);
    }

    public static void logSubmissionReceived(String nodeId, int receivedNodeID, long time) {
        eventLog.add(time + ",SubmissionReceived," + nodeId + ",Received submission from node " + receivedNodeID);
    }

    public static void logHashReceived(String nodeId, int receivedNodeID, long time) {
        eventLog.add(time + ",HashReceived," + nodeId + ",Received hash from node " + receivedNodeID);
    }

    public static void logTimeout(String nodeId, long time) {
        eventLog.add(time + ",Timeout," + nodeId + ",Node timed out");
    }

    public static void logEventPropagation(String nodeId, String eventType, long time) {
        eventLog.add(time + ",EventPropagation," + nodeId + ",Propagated " + eventType);
    }

    public static void logFetchEpochsRequest(String nodeId, int receivedNodeID, int epochId, long time) {
        eventLog.add(time + ",FetchEpochsRequest," + nodeId + ",Received request for epoch " + epochId + " from node " + receivedNodeID);
    }

    public static void logEpochReceived(String nodeId, int receivedNodeID, long time) {
        eventLog.add(time + ",EpochReceived," + nodeId + ",Received epoch from node " + receivedNodeID);
    }

    public static void flushEventLog() {
        FileWriter writer;
        try {
            writer = new FileWriter(Reporter.path + "EventLog - " + Reporter.runId + ".csv");
            for (String str : eventLog) {
                writer.write(str + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void flushStateLog() {
        FileWriter writer;
        try {
            writer = new FileWriter(Reporter.path + "StateLog - " + Reporter.runId + ".csv");
            for (String str : stateLog) {
                writer.write(str + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
