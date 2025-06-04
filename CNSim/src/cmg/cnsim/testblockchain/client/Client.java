package cmg.cnsim.testblockchain.client;

import cmg.cnsim.testblockchain.client.api.AbortAcceptResponse;
import cmg.cnsim.testblockchain.client.api.AbortPrepareRequest;
import cmg.cnsim.testblockchain.client.api.AbortPrepareResponse;
import cmg.cnsim.testblockchain.client.api.TxAcceptResponse;
import cmg.cnsim.testblockchain.client.api.TxPrepareResponse;
import cmg.cnsim.testblockchain.client.behavior.ClientBehavior;
import cmg.cnsim.testblockchain.client.tasks.CheckPennyJarTask;
import cmg.cnsim.testblockchain.client.tasks.ClientTask;
import cmg.cnsim.testblockchain.client.tasks.GetAccountTask;
import cmg.cnsim.testblockchain.client.tasks.MakeTransactionTask;
import cmg.cnsim.testblockchain.client.tasks.SyncEpochTask;
import cmg.cnsim.testblockchain.client.tx.Tx;
import cmg.cnsim.testblockchain.node.Epoch;
import cmg.cnsim.engine.Simulation;

import java.util.*;

public class Client {
    private int clientId;  // represents the address of the client
                           // corresponds to the node with the same id, if it exists

    private Simulation sim;
    private ClientBehavior behavior;
    private Queue<ClientTask> tasks; // queue of tasks (ex. transactions, checking pennyjar, syncing epoch) that the client will process sequentially

    public long timeout = 10000; // transaction timeout
                                 // FIXME should be configurable per client

    public List<Tx> currentTxs; // stack of transactions the client is currently trying to make
    public Map<Integer, TxPrepareResponse> txResponsesBySender; // map from nodeId to the latest TxPrepareResponse they returned
    public Map<Integer, Integer> txResponsesEpochIdCount; // frequency table of the epochIDs of all the TxPrepareResponses received
    public Set<Integer> txSuccessNodes; // set of all nodes that returned a successful TxAcceptResponse

    public AbortPrepareRequest currentAbortRequest; // abort request client is currently trying to make
    public Map<Integer, AbortPrepareResponse> abortResponsesBySender; // map from nodeId to the latest AbortPrepareResponse they returned
    public Set<Integer> abortSuccessNodes; // set of all nodes that returned a successful AbortAcceptResponse

    public Account fetchedAccount; // last fetched account
    public Map<Integer, Account> accountResponses;
    public Map<Account, Integer> accountCount;

    public Map<Integer, PennyJar> pennyJarResponses;
    public Map<PennyJar, Integer> pennyJarCount;

    public Map<Integer, PennyJar> rewardJarResponses;
    public Map<PennyJar, Integer> rewardJarCount;

    public Epoch epoch; // last synced epoch
    public Map<Integer, Epoch> epochResponses;
    public Map<Epoch, Integer> epochCount;
    public long lastSyncedTime; // last time the client synced epoch with the network

    public Client(int clientId, Simulation sim) {
        this.clientId = clientId;
        this.sim = sim;
        tasks = new LinkedList<>();
        txResponsesBySender = new HashMap<>();
        txResponsesEpochIdCount = new HashMap<>();
        txSuccessNodes = new HashSet<>();
        abortResponsesBySender = new HashMap<>();
        abortSuccessNodes = new HashSet<>();
        accountResponses = new HashMap<>();
        accountCount = new HashMap<>();
        pennyJarResponses = new HashMap<>();
        pennyJarCount = new HashMap<>();
        epochResponses = new HashMap<>();
        epochCount = new HashMap<>();
    }

    public void setInitialEpoch(Epoch epoch) {
        this.epoch = epoch;
        lastSyncedTime = 0;
    }

    // Adds a tasks to the queue, and does it if there are no existing tasks before it
    public void pushTask(ClientTask task, long time) {
        tasks.add(task);
        if (tasks.size() == 1) {
            doTask(task, time);
        }
    }

    // Called when the current task has been completed
    // Removes the task from the queue, and does the next task if it exists
    public void popTask(long time) {
        tasks.remove();
        if (!tasks.isEmpty()) {
            doTask(tasks.peek(), time);
        }
    }

    private void doTask(ClientTask task, long time) {
        if (task instanceof SyncEpochTask) {
            behavior.doSyncEpochTask((SyncEpochTask) task, time);
        } else if (task instanceof GetAccountTask) {
            behavior.doGetAccountTask((GetAccountTask) task, time);
        } else if (task instanceof CheckPennyJarTask) {
            behavior.doCheckPennyJarTask((CheckPennyJarTask) task, time);
        } else if (task instanceof MakeTransactionTask) {
            behavior.doMakeTransactionTask((MakeTransactionTask) task, time);
        }
    }

    public void event_ClientGetAccount(long time) {
        behavior.event_ClientGetAccount(time);
    }

    public void event_ClientCheckPennyJar(long time) {
        behavior.event_ClientCheckPennyJar(time);
    }

    public void event_ClientCheckRewardJar(long time) {
        behavior.event_ClientCheckRewardJar(time);
    }

    public void event_ClientMakeTransaction(int recipientId, long amount, long time) {
        behavior.event_ClientMakeTransaction(recipientId, amount, time);
    }

    public void event_ClientSyncResponse(int senderId, Epoch epoch, long time) {
        behavior.event_ClientSyncResponse(senderId, epoch, time);
    }
    public void event_ClientReceivesAccount(int senderId, Account account, long time) {
        behavior.event_ClientReceivesAccount(senderId, account, time);
    }

    public void event_ClientReceivesPennyJar(int senderId, PennyJar pennyJar, long time) {
        behavior.event_ClientReceivesPennyJar(senderId, pennyJar, time);
    }

    public void event_ClientReceivesRewardJar(int senderId, PennyJar rewardJar, long time) {
        behavior.event_ClientReceivesRewardJar(senderId, rewardJar, time);
    }

    public void event_ClientTxPrepareResponse(TxPrepareResponse response, long time) {
        behavior.event_ClientTxPrepareResponse(response, time);
    }

    public void event_ClientTxAcceptResponse(TxAcceptResponse response, long time) {
        behavior.event_ClientTxAcceptResponse(response, time);
    }

    public void event_ClientTxPrepareTimeout(List<Tx> txs, long time) {
        behavior.event_ClientTxPrepareTimeout(txs, time);
    }

    public void event_ClientTxAcceptTimeout(List<Tx> txs, long time) {
        behavior.event_ClientTxAcceptTimeout(txs, time);
    }

    public void event_ClientAbortPrepareResponse(AbortPrepareResponse response, long time) {
        behavior.event_ClientAbortPrepareResponse(response, time);
    }

    public void event_ClientAbortAcceptResponse(AbortAcceptResponse response, long time) {
        behavior.event_ClientAbortAcceptResponse(response, time);
    }

    public void transact(List<Tx> txs, long time) {
        behavior.transact(txs, time);
    }

    public void abort(AbortPrepareRequest request, long time) {
        behavior.abort(request, time);
    }

    public void receivesEpoch(Epoch epoch, long time) {
        behavior.receivesEpoch(epoch, time);
    }

    public void receivesAccount(Account account, long time) {
        behavior.receivesAccount(account, time);
    }

    public void receivesPennyJar(PennyJar pennyJar, long time) {
        behavior.receivesPennyJar(pennyJar, time);
    }

    public void receivesRewardJar(PennyJar rewardJar, long time) {
        behavior.receivesRewardJar(rewardJar, time);
    }

    public int getClientId() {
        return clientId;
    }

    public void setSimulation(Simulation sim) {
        this.sim = sim;
    }

    public void setBehaviorStrategy(ClientBehavior behavior) {
        this.behavior = behavior;
    }
}
