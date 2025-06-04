package cmg.cnsim.testblockchain.client.behavior;

import cmg.cnsim.testblockchain.events.transaction.Event_NodeGetAccountRequest;
import cmg.cnsim.testblockchain.events.transaction.Event_NodeGetPennyJarRequest;
import cmg.cnsim.testblockchain.events.transaction.Event_NodeGetRewardJarRequest;
import cmg.cnsim.testblockchain.events.transaction.Event_NodeSyncRequest;
import cmg.cnsim.testblockchain.client.Account;
import cmg.cnsim.testblockchain.client.Client;
import cmg.cnsim.testblockchain.client.Penny;
import cmg.cnsim.testblockchain.client.PennyJar;
import cmg.cnsim.testblockchain.client.api.AbortAcceptRequest;
import cmg.cnsim.testblockchain.client.api.AbortAcceptResponse;
import cmg.cnsim.testblockchain.client.api.AbortPrepareRequest;
import cmg.cnsim.testblockchain.client.api.AbortPrepareResponse;
import cmg.cnsim.testblockchain.client.api.TxAcceptRequest;
import cmg.cnsim.testblockchain.client.api.TxAcceptResponse;
import cmg.cnsim.testblockchain.client.api.TxPrepareResponse;
import cmg.cnsim.testblockchain.client.tasks.CheckPennyJarTask;
import cmg.cnsim.testblockchain.client.tasks.CheckRewardJarTask;
import cmg.cnsim.testblockchain.client.tasks.GetAccountTask;
import cmg.cnsim.testblockchain.client.tasks.MakeTransactionTask;
import cmg.cnsim.testblockchain.client.tasks.SyncEpochTask;
import cmg.cnsim.testblockchain.client.tx.RedeemPennyTx;
import cmg.cnsim.testblockchain.client.tx.SendPennyTx;
import cmg.cnsim.testblockchain.client.tx.Tx;
import cmg.cnsim.testblockchain.events.transaction.Event_ClientTxAcceptTimeout;
import cmg.cnsim.testblockchain.events.transaction.Event_ClientTxPrepareTimeout;
import cmg.cnsim.testblockchain.events.transaction.Event_NodeAbortAcceptRequest;
import cmg.cnsim.testblockchain.events.transaction.Event_NodeAbortPrepareRequest;
import cmg.cnsim.testblockchain.events.transaction.Event_NodeTxAcceptRequest;
import cmg.cnsim.testblockchain.events.transaction.Event_NodeTxPrepareRequest;
import cmg.cnsim.testblockchain.node.Epoch;
import cmg.cnsim.testblockchain.reporter.ClientReporter;
import cmg.cnsim.testblockchain.reporter.MessageReporter;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.node.INode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HonestBehavior implements ClientBehavior {
    private final Client client;
    protected Simulation sim;

    public HonestBehavior(Client client, Simulation sim) {
        this.client = client;
        this.sim = sim;
    }

    // Fetches the latest epoch from the network
    // Should be done before every query to the network
    @Override
    public void doSyncEpochTask(SyncEpochTask task, long time) {
        client.epochResponses.clear();
        client.epochCount.clear();
        for (int nodeId : client.epoch.peers) {
            INode node = sim.getNodeSet().pickSpecificNode(nodeId);
            long inter = sim.getNetwork().getPropagationTime(this.client.getClientId() + sim.getNodeSet().getNodes().size(), node.getID(), 1); // TODO fix the size
            sim.schedule(new Event_NodeSyncRequest(node, client.getClientId(), time + inter));
        }
    }

    @Override
    public void doMakeTransactionTask(MakeTransactionTask task, long time) {
        Penny penny = new Penny(task.amount, client.getClientId(), task.recipientId, time);
        Tx tx = new SendPennyTx(penny, client.fetchedAccount.nonce, time);
        List<Tx> txs = new ArrayList<>();
        txs.add(tx);
        client.transact(txs, time);
    }

    @Override
    public void doGetAccountTask(GetAccountTask task, long time) {
        client.accountResponses.clear();
        client.accountCount.clear();
        for (int nodeId : client.epoch.peers) {
            INode node = sim.getNodeSet().pickSpecificNode(nodeId);
            long inter = sim.getNetwork().getPropagationTime(this.client.getClientId() + sim.getNodeSet().getNodes().size(), node.getID(), 1); // TODO fix the size
            Event_NodeGetAccountRequest event = new Event_NodeGetAccountRequest(node, client.getClientId(), time + inter);
            sim.schedule(event);
        }
    }

    @Override
    public void doCheckPennyJarTask(CheckPennyJarTask task, long time) {
        client.pennyJarResponses.clear();
        client.pennyJarCount.clear();
        for (int nodeId : client.epoch.peers) {
            INode node = sim.getNodeSet().pickSpecificNode(nodeId);
            long inter = sim.getNetwork().getPropagationTime(this.client.getClientId() + sim.getNodeSet().getNodes().size(), node.getID(), 1); // TODO fix the size
            Event_NodeGetPennyJarRequest event = new Event_NodeGetPennyJarRequest(node, client.getClientId(), time + inter);
            sim.schedule(event);
        }
    }

    // TODO refactor since code is identical
    @Override
    public void doCheckRewardJarTask(CheckRewardJarTask task, long time) {
        client.rewardJarResponses.clear();
        client.rewardJarCount.clear();
        for (int nodeId : client.epoch.peers) {
            INode node = sim.getNodeSet().pickSpecificNode(nodeId);
            long inter = sim.getNetwork().getPropagationTime(this.client.getClientId() + sim.getNodeSet().getNodes().size(), node.getID(), 1); // TODO fix the size
            Event_NodeGetRewardJarRequest event = new Event_NodeGetRewardJarRequest(node, client.getClientId(), time + inter);
            sim.schedule(event);
        }
    }

    // Client queries for its account
    @Override
    public void event_ClientGetAccount(long time) {
        client.pushTask(new SyncEpochTask(), time);
        client.pushTask(new GetAccountTask(), time);
    }

    // Client queries for its pennyjar and redeems all pennies in it
    @Override
    public void event_ClientCheckPennyJar(long time) {
        client.pushTask(new SyncEpochTask(), time);
        client.pushTask(new GetAccountTask(), time);
        client.pushTask(new CheckPennyJarTask(), time);
    }

    // Client queries for its rewardjar and redeems all pennies in it
    @Override
    public void event_ClientCheckRewardJar(long time) {
        client.pushTask(new SyncEpochTask(), time);
        client.pushTask(new GetAccountTask(), time);
        client.pushTask(new CheckRewardJarTask(), time);
    }

    // Sends tokens to another client
    @Override
    public void event_ClientMakeTransaction(int recipientId, long amount, long time) {
        client.pushTask(new SyncEpochTask(), time);
        client.pushTask(new GetAccountTask(), time);
        client.pushTask(new MakeTransactionTask(recipientId, amount), time);
    }

    // Client receives the epoch from a node
    @Override
    public void event_ClientSyncResponse(int senderId, Epoch epoch, long time) {
        MessageReporter.logClientMessage("node-" + senderId, Integer.toString(client.getClientId()), "SyncResponse", epoch.printEpoch(), time);
        if (client.epochResponses.containsKey(senderId)) {
            Epoch oldResponse = client.epochResponses.get(senderId);
            client.epochCount.put(oldResponse, client.epochCount.get(oldResponse) - 1);
        }
        client.epochResponses.put(senderId, epoch);
        client.epochCount.put(epoch, client.epochCount.getOrDefault(epoch, 0) + 1);
        if (3 * client.epochCount.get(epoch) > 2 * client.epoch.peers.size()) {
            // Only be <1/3 responses can come after this, and will be ignored
            client.epochResponses.clear();
            client.epochCount.clear();

            client.receivesEpoch(epoch, time);
        }
    }

    // Client receives its account from a node
    @Override
    public void event_ClientReceivesAccount(int senderId, Account account, long time) {
        MessageReporter.logClientMessage("node-" + senderId, Integer.toString(client.getClientId()), "ReceivesAccount", account.printAccount(), time);
        if (client.accountResponses.containsKey(senderId)) {
            Account oldResponse = client.accountResponses.get(senderId);
            client.accountCount.put(oldResponse, client.accountCount.get(oldResponse) - 1);
        }
        client.accountResponses.put(senderId, account);
        client.accountCount.put(account, client.accountCount.getOrDefault(account, 0) + 1);
        if (3 * client.accountCount.get(account) > 2 * client.epoch.peers.size()) {
            // Only be <1/3 responses can come after this, and will be ignored
            client.accountResponses.clear();
            client.accountCount.clear();

            client.receivesAccount(account, time);
        }
    }

    // Client receives its pennyjar from a node
    @Override
    public void event_ClientReceivesPennyJar(int senderId, PennyJar pennyJar, long time) {
        MessageReporter.logClientMessage("node-" + senderId, Integer.toString(client.getClientId()), "ReceivesPennyJar", pennyJar.printPennyJar(), time);
        if (client.pennyJarResponses.containsKey(senderId)) {
            PennyJar oldResponse = client.pennyJarResponses.get(senderId);
            client.pennyJarCount.put(oldResponse, client.pennyJarCount.get(oldResponse) - 1);
        }
        client.pennyJarResponses.put(senderId, pennyJar);
        client.pennyJarCount.put(pennyJar, client.pennyJarCount.getOrDefault(pennyJar, 0) + 1);
        if (3 * client.pennyJarCount.get(pennyJar) > 2 * client.epoch.peers.size()) {
            // Only be <1/3 responses can come after this, and will be ignored
            client.pennyJarResponses.clear();
            client.pennyJarCount.clear();

            client.receivesPennyJar(pennyJar, time);
        }
    }

    // Same thing but for rewardjar
    // TODO refactor since code is identical
    @Override
    public void event_ClientReceivesRewardJar(int senderId, PennyJar rewardJar, long time) {
        MessageReporter.logClientMessage("node-" + senderId, Integer.toString(client.getClientId()), "ReceivesRewardJar", rewardJar.printPennyJar(), time);
        if (client.rewardJarResponses.containsKey(senderId)) {
            PennyJar oldResponse = client.rewardJarResponses.get(senderId);
            client.rewardJarCount.put(oldResponse, client.rewardJarCount.get(oldResponse) - 1);
        }
        client.rewardJarResponses.put(senderId, rewardJar);
        client.rewardJarCount.put(rewardJar, client.rewardJarCount.getOrDefault(rewardJar, 0) + 1);
        if (3 * client.rewardJarCount.get(rewardJar) > 2 * client.epoch.peers.size()) {
            // Only be <1/3 responses can come after this, and will be ignored
            client.rewardJarResponses.clear();
            client.rewardJarCount.clear();

            client.receivesRewardJar(rewardJar, time);
        }
    }

    // Client receives verification for its current transaction from a node
    @Override
    public void event_ClientTxPrepareResponse(TxPrepareResponse response, long time) {
        MessageReporter.logClientMessage("node-" + response.senderId, Integer.toString(client.getClientId()), "TxPrepareResponse", response.printResponse(), time);
        if (response.error != null || client.currentTxs == null || !client.currentTxs.equals(response.txs)) {
            return;
        }
        if (client.txResponsesBySender.containsKey(response.senderId)) {
            int oldEpochId = client.txResponsesBySender.get(response.senderId).epochId;
            client.txResponsesEpochIdCount.put(oldEpochId, client.txResponsesEpochIdCount.get(oldEpochId) - 1);
        }
        client.txResponsesBySender.put(response.senderId, response);
        client.txResponsesEpochIdCount.put(response.epochId, client.txResponsesEpochIdCount.getOrDefault(response.epochId, 0) + 1);

        if (3 * client.txResponsesEpochIdCount.get(response.epochId) > 2 * client.epoch.peers.size()) {
            List<TxPrepareResponse> validResponses = new ArrayList<>();
            for (TxPrepareResponse resp : client.txResponsesBySender.values()) {
                if (resp.epochId == response.epochId) {
                    validResponses.add(resp);
                }
            }
            TxAcceptRequest request = new TxAcceptRequest(client.getClientId(), response.txs, validResponses);

            for (int nodeId : client.epoch.peers) {
                INode node = sim.getNodeSet().pickSpecificNode(nodeId);
                long inter = sim.getNetwork().getPropagationTime(this.client.getClientId() + sim.getNodeSet().getNodes().size(), node.getID(), 1);  // TODO fix the size
                Event_NodeTxAcceptRequest event = new Event_NodeTxAcceptRequest(node, request, time + inter);
                sim.schedule(event);
            }
            client.txResponsesBySender.clear();
            client.txResponsesEpochIdCount.clear();
            client.txSuccessNodes.clear();
            sim.schedule(new Event_ClientTxAcceptTimeout(client, client.currentTxs, time + client.timeout));
        }
    }

    // Client receives receipt for its current transaction from a node
    @Override
    public void event_ClientTxAcceptResponse(TxAcceptResponse response, long time) {
        MessageReporter.logClientMessage("node-" + response.senderId, Integer.toString(client.getClientId()), "TxAcceptResponse", response.printResponse(), time);
        if (response.error != null || client.currentTxs == null || !client.currentTxs.equals(response.txs)) {
            return;
        }
        client.txSuccessNodes.add(response.senderId);
        if (3 * client.txSuccessNodes.size() > 2 * client.epoch.peers.size()) {
            client.currentTxs = null;
            client.txSuccessNodes.clear();
            client.popTask(time);
        }
    }

    @Override
    public void event_ClientTxPrepareTimeout(List<Tx> txs, long time) {
        if (client.currentTxs == null || !client.currentTxs.equals(txs)) {
            return;
        }
        client.currentTxs = null;

        // Abort transaction and continue with the rest of the tasks
        client.abort(new AbortPrepareRequest(client.getClientId(), client.fetchedAccount.nonce), time);
    }

    @Override
    public void event_ClientTxAcceptTimeout(List<Tx> txs, long time) {
        if (client.currentTxs == null || !client.currentTxs.equals(txs)) {
            return;
        }
        client.currentTxs = null;

        // client.popTask(time); // Continue with the rest of the tasks (may not be desired behaviour)
    }

    // Client receives verification for its abort request from a node
    @Override
    public void event_ClientAbortPrepareResponse(AbortPrepareResponse response, long time) {
        MessageReporter.logClientMessage("node-" + response.senderId, Integer.toString(client.getClientId()), "AbortPrepareResponse", response.printResponse(), time);
        if (response.error != null || client.currentAbortRequest == null || !client.currentAbortRequest.equals(response.abortRequest)) {
            return;
        }
        client.abortResponsesBySender.put(response.senderId, response);

        if (3 * client.abortResponsesBySender.size() > 2 * client.epoch.peers.size()) {
            List<AbortPrepareResponse> allResponses = new ArrayList<>(client.abortResponsesBySender.values());
            AbortAcceptRequest request = new AbortAcceptRequest(client.getClientId(), client.currentAbortRequest, allResponses);

            for (int nodeId : client.epoch.peers) {
                INode node = sim.getNodeSet().pickSpecificNode(nodeId);
                long inter = sim.getNetwork().getPropagationTime(this.client.getClientId() + sim.getNodeSet().getNodes().size(), node.getID(), 1);  // TODO fix the size
                Event_NodeAbortAcceptRequest event = new Event_NodeAbortAcceptRequest(node, request, time + inter);
                sim.schedule(event);
            }
            client.abortResponsesBySender.clear();
            client.abortSuccessNodes.clear();
        }
    }

    // Client receives receipt for its abort request from a node
    @Override
    public void event_ClientAbortAcceptResponse(AbortAcceptResponse response, long time) {
        MessageReporter.logClientMessage("node-" + response.senderId, Integer.toString(client.getClientId()), "AbortAcceptResponse", response.printResponse(), time);
        if (response.error != null || client.currentAbortRequest == null || !client.currentAbortRequest.equals(response.abortRequest)) {
            return;
        }
        client.abortSuccessNodes.add(response.senderId);
        if (3 * client.abortSuccessNodes.size() > 2 * client.epoch.peers.size()) {
            client.currentAbortRequest = null;
            client.abortSuccessNodes.clear();
            client.popTask(time);  // end whichever task started the transaction
        }
    }

    // Client receives a conensus on the epoch from >2/3 of the network
    @Override
    public void receivesEpoch(Epoch epoch, long time) {
        ClientReporter.logClient(this.client.getClientId(), "ReceivesEpoch", epoch.printEpoch(), time);
        client.epoch = epoch;
        client.lastSyncedTime = time;
        client.popTask(time);
    }

    // Client receives a conensus on its account from >2/3 of the network
    @Override
    public void receivesAccount(Account account, long time) {
        ClientReporter.logClient(this.client.getClientId(), "ReceivesAccount", account.printAccount(), time);
        client.fetchedAccount = account;
        client.popTask(time);
    }

    // Client receives a conensus on its pennyjar from >2/3 of the network
    // Will then try to make a transaction to redeem all of the pennies in it
    @Override
    public void receivesPennyJar(PennyJar pennyJar, long time) {
        ClientReporter.logClient(this.client.getClientId(), "ReceivesPennyJar", pennyJar.printPennyJar(), time);
        long nonce = client.fetchedAccount.nonce;
        List<Tx> txs = new ArrayList<>();
        for (Penny penny : pennyJar.pennies) {
            Tx tx = new RedeemPennyTx(penny, nonce, time);
            txs.add(tx);
            nonce++;
        }
        if (!txs.isEmpty()) {
            client.transact(txs, time);
        } else {
            client.popTask(time);
        }
    }

    // Same thing but for rewardjar
    // TODO refactor since code is identical
    @Override
    public void receivesRewardJar(PennyJar rewardJar, long time) {
        ClientReporter.logClient(this.client.getClientId(), "ReceivesRewardJar", rewardJar.pennies.toString(), time);
        long nonce = client.fetchedAccount.nonce;
        List<Tx> txs = new ArrayList<>();
        for (Penny penny : rewardJar.pennies) {
            Tx tx = new RedeemPennyTx(penny, nonce, time);
            txs.add(tx);
            nonce++;
        }
        if (!txs.isEmpty()) {
            client.transact(txs, time);
        } else {
            client.popTask(time);
        }
    }

    // Helper for making a stacked transaction
    @Override
    public void transact(List<Tx> txs, long time) {
        ClientReporter.logClient(this.client.getClientId(), "Transact", "Transacting: [" + txs.stream().map(Tx::printTx).collect(Collectors.joining(", ")) + "]", time);
        client.currentTxs = txs;
        client.txResponsesBySender.clear();
        client.txResponsesEpochIdCount.clear();
        for (int nodeId : client.epoch.peers) {
            INode node = sim.getNodeSet().pickSpecificNode(nodeId);
            long inter = sim.getNetwork().getPropagationTime(this.client.getClientId() + sim.getNodeSet().getNodes().size(), node.getID(), 1); // TODO fix the size
            Event_NodeTxPrepareRequest event = new Event_NodeTxPrepareRequest(node, client.getClientId(), txs, time + inter);
            sim.schedule(event);
        }
        sim.schedule(new Event_ClientTxPrepareTimeout(client, txs, time + client.timeout));
    }

    // Tries to abort the current transaction
    @Override
    public void abort(AbortPrepareRequest request, long time) {
        ClientReporter.logClient(this.client.getClientId(), "Abort", "Aborting: [" + request.printRequest() + "]", time);
        client.currentAbortRequest = request;
        client.abortResponsesBySender.clear();
        for (int nodeId : client.epoch.peers) {
            INode node = sim.getNodeSet().pickSpecificNode(nodeId);
            long inter = sim.getNetwork().getPropagationTime(this.client.getClientId() + sim.getNodeSet().getNodes().size(), node.getID(), 1); // TODO fix the size
            Event_NodeAbortPrepareRequest event = new Event_NodeAbortPrepareRequest(node, request, time + inter);
            sim.schedule(event);
        }
    }
}
