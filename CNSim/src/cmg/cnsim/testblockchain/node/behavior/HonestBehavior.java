package cmg.cnsim.testblockchain.node.behavior;

import cmg.cnsim.testblockchain.client.*;
import cmg.cnsim.testblockchain.client.api.AbortAcceptRequest;
import cmg.cnsim.testblockchain.client.api.AbortAcceptResponse;
import cmg.cnsim.testblockchain.client.api.AbortPrepareRequest;
import cmg.cnsim.testblockchain.client.api.AbortPrepareResponse;
import cmg.cnsim.testblockchain.client.api.TxAcceptRequest;
import cmg.cnsim.testblockchain.client.api.TxAcceptResponse;
import cmg.cnsim.testblockchain.client.api.TxPrepareResponse;
import cmg.cnsim.testblockchain.client.tx.RedeemPennyTx;
import cmg.cnsim.testblockchain.client.tx.RedeemRewardTx;
import cmg.cnsim.testblockchain.client.tx.SendPennyTx;
import cmg.cnsim.testblockchain.client.tx.Tx;
import cmg.cnsim.testblockchain.events.validation.Event_NodeReceivesVote;
import cmg.cnsim.testblockchain.node.TestBlockchainNode;
import cmg.cnsim.testblockchain.node.Epoch;
import cmg.cnsim.testblockchain.node.EpochHash;
import cmg.cnsim.testblockchain.node.NodeState;
import cmg.cnsim.testblockchain.node.Submission;
import cmg.cnsim.testblockchain.node.Vote;
import cmg.cnsim.testblockchain.events.transaction.Event_ClientAbortAcceptResponse;
import cmg.cnsim.testblockchain.events.transaction.Event_ClientAbortPrepareResponse;
import cmg.cnsim.testblockchain.events.transaction.Event_ClientCheckRewardJar;
import cmg.cnsim.testblockchain.events.transaction.Event_ClientSyncResponse;
import cmg.cnsim.testblockchain.events.transaction.Event_ClientTxAcceptResponse;
import cmg.cnsim.testblockchain.events.transaction.Event_ClientReceivesAccount;
import cmg.cnsim.testblockchain.events.transaction.Event_ClientReceivesPennyJar;
import cmg.cnsim.testblockchain.events.transaction.Event_ClientReceivesRewardJar;
import cmg.cnsim.testblockchain.events.transaction.Event_ClientTxPrepareResponse;
import cmg.cnsim.testblockchain.events.transaction.Event_NodeCheckRewardJar;
import cmg.cnsim.testblockchain.events.validation.Event_NodeFetchEpochsRequest;
import cmg.cnsim.testblockchain.events.validation.Event_NodeReceivesEpoch;
import cmg.cnsim.testblockchain.events.validation.Event_NodeReceivesHash;
import cmg.cnsim.testblockchain.events.validation.Event_NodeReceivesSubmission;
import cmg.cnsim.testblockchain.reporter.AccountReporter;
import cmg.cnsim.testblockchain.reporter.MessageReporter;
import cmg.cnsim.testblockchain.reporter.TransactionReporter;
import cmg.cnsim.testblockchain.reporter.ValidationReporter;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.node.INode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HonestBehavior implements NodeBehavior {

    private final TestBlockchainNode node; // Reference to the TestBlockchainNode
    protected Simulation sim;

    public HonestBehavior(TestBlockchainNode node, Simulation sim) {
        this.node = node;
        this.sim = sim;
    }

    // Node receives vote from another node
    @Override
    public void event_NodeReceivesVote(Vote vote, long time) {
        MessageReporter.logStateMessage("node-" + vote.senderId, this.node.nodeId, "NodeReceivesVote", vote.toString(), time);
        // For mocking nodefault
        // if (node.getID() == 4 && node.epoch.epochId == 2) return;
        if (node.state != NodeState.WAITING
                && node.state != NodeState.INITIATED
                && node.state != NodeState.WAITING_VALIDATE_SUBMISSIONS) {
            return;
        }
        if (!node.getEpoch().peers.contains(vote.senderId)) {
            //System.out.println("Invalid Vote. Sender node " + vote.senderId + " not in epoch at time " + time);
            return;
        }
        if (node.getEpoch().epochId != vote.epochId) {
            //System.out.println("Invalid Vote. Epoch mismatch. Node Epoch: " + node.epoch.epochId + " Received Epoch: " + vote.epochId + " at time " + time);
            return;
        }
        if (node.votes.containsKey(vote.senderId)) {
            //System.out.println("Invalid Vote at time " + time + ". Sender " + vote.senderId + " has already voted.");
            return;
        }
        NodeState oldState = node.state;
        // Receive vote from other nodes
        //System.out.println("Node " + node.nodeId + " received vote from node: " + vote.senderId + " at time " + time + " in state " + node.state);
        node.votes.put(vote.senderId, vote);

        if (2 * node.votes.size() > node.epoch.peers.size() && node.state == NodeState.WAITING) {
            //System.out.println("Node " + node.nodeId + " received votes from half of the nodes + at time " + time);
            //When a node receives >1/2 of votes, it is a signal that its timeout is taking too long
            // ,and it should proceed with the validation round if it doesn't want to miss out on its votes.
            node.state = NodeState.INITIATED;
            //System.out.println("Node " + node.nodeId + " changed state to INITIATED" + " at time " + time);
            node.timeoutIgnoreCounter++;
            //System.out.println("Node " + node.nodeId + " incremented timeout counter to " + node.timeoutIgnoreCounter + " at time " + time);
            node.scheduleTimeout(time);
            ValidationReporter.logStateChange(node.nodeId, oldState, node.state, time);
            node.propagateVote(time);
            return;
        }

        //it should check if it has received votes from the majority of the nodes
        if (3 * node.votes.size() > 2 * node.epoch.peers.size() && node.state == NodeState.INITIATED) {
            //System.out.println("Node " + node.nodeId + " received votes from majority of the nodes" + " at time " + time);
            node.state = NodeState.WAITING_VALIDATE_SUBMISSIONS;
            //System.out.println("Node " + node.nodeId + " changed state to WAITING_VALIDATE_SUBMISSIONS" + " at time " + time);
            node.timeoutIgnoreCounter++;//this is for ignoring the timeout event in the previous condition.
            //System.out.println("Node " + node.nodeId + " incremented timeout counter to " + node.timeoutIgnoreCounter + " at time " + time);
            node.scheduleTimeout(time);
            ValidationReporter.logStateChange(node.nodeId, oldState, node.state, time);
        }
    }

    // Node receives submission from another node
    @Override
    public void event_NodeReceivesSubmission(Submission submission, long time) {
        MessageReporter.logStateMessage("node-" + submission.senderId, this.node.nodeId, "NodeReceivesSubmission", submission.votes.toString(), time);
        if (node.state != NodeState.INITIATED
                && node.state != NodeState.WAITING_VALIDATE_SUBMISSIONS
                && node.state != NodeState.VALIDATE_SUBMISSIONS
                && node.state != NodeState.WAITING_VALIDATE_HASH) {
            return;
        }
        if (!node.getEpoch().peers.contains(submission.senderId)) {
            //System.out.println("Invalid Submission. Sender node " + submission.senderId + " not in epoch at time " + time);
            return;
        }
        if (node.getEpoch().epochId != submission.epochId) {
            //System.out.println("Invalid Submission. Epoch mismatch. Node Epoch: " + node.epoch.epochId + " Received Epoch: " + submission.epochId + " at time " + time);
            return;
        }
        if (node.submissions.containsKey(submission.senderId)) {
            //System.out.println("Invalid Submission at time " + time + ". Sender " + submission.senderId + " has already sent their submission.");
            return;
        }
        NodeState oldState = node.state;
        // Implement the logic for receiving submission
        //System.out.println("Node " + node.nodeId + " received submission from node: " + submission.senderId + " at time " + time + " in state " + node.state);
        node.submissions.put(submission.senderId, submission);

        if (2 * node.submissions.size() > node.epoch.peers.size() && node.state == NodeState.WAITING_VALIDATE_SUBMISSIONS) {
            //System.out.println("Node " + node.nodeId + " received submissions from half of the nodes." + " at time " + time);
            node.state = NodeState.VALIDATE_SUBMISSIONS;
            //System.out.println("Node " + node.nodeId + " changed state to VALIDATE_SUBMISSIONS" + " at time " + time);
            node.timeoutIgnoreCounter++;
            //System.out.println("Node " + node.nodeId + " incremented timeout counter to " + node.timeoutIgnoreCounter + " at time " + time);
            node.scheduleTimeout(time);
            ValidationReporter.logStateChange(node.nodeId, oldState, node.state, time);
            node.propagateSubmission(time);
            return;
        }

        //it should check if it has received submissions from the majority of the nodes
        if (3 * node.submissions.size() > 2 * node.epoch.peers.size() && node.state == NodeState.VALIDATE_SUBMISSIONS) {
            //System.out.println("Node " + node.nodeId + " received submissions from majority of the nodes." + " at time " + time);
            node.state = NodeState.WAITING_VALIDATE_HASH;
            //System.out.println("Node " + node.nodeId + " changed state to WAITING_VALIDATE_HASH" + " at time " + time);
            node.timeoutIgnoreCounter++; //this is for ignoring the timeout event in the previous condition.
            //System.out.println("Node " + node.nodeId + " incremented timeout counter to " + node.timeoutIgnoreCounter + " at time " + time);
            node.scheduleTimeout(time);
            ValidationReporter.logStateChange(node.nodeId, oldState, node.state, time);
        }

    }

    // Node receives hash from another node
    @Override
    public void event_NodeReceivesHash(EpochHash hash, long time) {
        MessageReporter.logStateMessage("node-" + hash.senderId, this.node.nodeId, "NodeReceivesHash", hash.toString(), time);
        if (node.state != NodeState.VALIDATE_SUBMISSIONS
                && node.state != NodeState.WAITING_VALIDATE_HASH
                && node.state != NodeState.VALIDATE_HASH) {
            return;
        }
        if (!node.getEpoch().peers.contains(hash.senderId)) {
            //System.out.println("Invalid Hash. Sender node " + hash.senderId + " not in epoch at time " + time);
            return;
        }
        if (node.getEpoch().epochId != hash.epochId) {
            //System.out.println("Invalid Hash. Epoch mismatch. Node Epoch: " + node.epoch.epochId + " Received Epoch: " + hash.epochId + " at time " + time);
            return;
        }
        if (node.hashes.containsKey(hash.senderId)) {
            //System.out.println("Invalid Hash at time " + time + ". Sender " + hash.senderId + " has already sent their hash.");
            return;
        }
        NodeState oldState = node.state;
        //System.out.println("Node " + node.nodeId + " received hash from node: " + hash.senderId + " at time " + time);
        node.hashes.put(hash.senderId, hash);

        if (2 * node.hashes.size() > node.epoch.peers.size() && node.state == NodeState.WAITING_VALIDATE_HASH) {
            //System.out.println("Node " + node.nodeId + " received hashes from half of the nodes." + " at time " + time);
            node.timeoutIgnoreCounter++;
            //System.out.println("Node " + node.nodeId + " incremented timeout counter to " + node.timeoutIgnoreCounter + " at time " + time);
            node.state = NodeState.VALIDATE_HASH;
            node.scheduleTimeout(time);
            ValidationReporter.logStateChange(node.nodeId, oldState, node.state, time);
            node.propagateHash(time);
            return;
        }

        //it should check if it has received hashes from the majority of the nodes
        if (3 * node.hashes.size() > 2 * node.epoch.peers.size() && node.state == NodeState.VALIDATE_HASH) {
            //System.out.println("Node " + node.nodeId + " received hashes from majority of the nodes."+ " at time " + time);
            HashMap<Integer, Integer> hashCount = new HashMap<>();
            for (EpochHash receivedHash : node.hashes.values()) {
                hashCount.put(receivedHash.hash, hashCount.getOrDefault(receivedHash.hash, 0) + 1);
            }
            for (Map.Entry<Integer, Integer> entry : hashCount.entrySet()) {
                Integer receivedHash = entry.getKey();
                Integer count = entry.getValue();
                if (3 * count > 2 * node.epoch.peers.size()) {
                    if (node.nextEpochHash == receivedHash) {
                        node.state = NodeState.COMPLETED;
                        //System.out.println("Node " + node.nodeId + " changed state to COMPLETED" + " at time " + time);
                        node.epoch = node.nextEpoch;
                        node.epoch.epochId++;
                        node.timeoutIgnoreCounter++; //this is for ignoring the timeout event in the previous condition.
                        //System.out.println("Node " + node.nodeId + " incremented timeout counter to " + node.timeoutIgnoreCounter + " at time " + time);
                        ValidationReporter.logStateChange(node.nodeId, oldState, node.state, time);
                        node.scheduleTimeout(time);
                        return;
                    }
                }
            }
            node.state = NodeState.NODE_FAULT;
            //System.out.println("Node " + node.nodeId + " changed state to NODE_FAULT" + " at time " + time);
            node.timeoutIgnoreCounter++; //this is for ignoring the timeout event in the previous condition.
            //System.out.println("Node " + node.nodeId + " incremented timeout counter to " + node.timeoutIgnoreCounter + " at time " + time);
            ValidationReporter.logStateChange(node.nodeId, oldState, node.state, time);
            node.fetchEpochs(time);
        }
    }

    // Node receives fetched epoch from another node
    @Override
    public void event_NodeReceivesEpoch(int senderId, Epoch epoch, long time) {
        MessageReporter.logStateMessage("node-" + senderId, this.node.nodeId, "NodeReceivesEpoch", epoch.printEpoch(), time);
        if (node.state != NodeState.NODE_FAULT) {
            return;
        }
        if (!node.getEpoch().peers.contains(senderId)) {
            //System.out.println("Invalid Epoch. Sender node " + senderId + " not in epoch at time " + time);
            return;
        }
        if (node.fetchedEpochs.containsKey(senderId)) {
            //System.out.println("Invalid Epoch at time " + time + ". Sender " + senderId + " has already sent their epoch.");
            return;
        }
        //System.out.println("Node " + node.nodeId + " received epoch with id " + epoch.epochId + " from node: " + senderId + " at time " + time);
        node.fetchedEpochs.put(senderId, epoch);

        if (3 * node.fetchedEpochs.size() > 2 * node.epoch.peers.size()) {
            HashMap<Epoch, Integer> epochCount = new HashMap<>();
            for (Epoch receivedEpoch : node.fetchedEpochs.values()) {
                epochCount.put(receivedEpoch, epochCount.getOrDefault(receivedEpoch, 0) + 1);
            }
            for (Map.Entry<Epoch, Integer> entry : epochCount.entrySet()) {
                Epoch receivedEpoch = entry.getKey();
                Integer count = entry.getValue();
                if (3 * count > 2 * node.epoch.peers.size()) {
                    node.epoch = receivedEpoch;
                    node.distributeValidationRewards(time);
                    node.timeoutIgnoreCounter++; //this is for ignoring the timeout event in the previous condition.
                    //System.out.println("Node " + node.nodeId + " incremented timeout counter to " + node.timeoutIgnoreCounter + " at time " + time);
                    node.register(time);
                    return;
                }
            }
            node.timeoutIgnoreCounter++; //this is for ignoring the timeout event in the previous condition.
            //System.out.println("Node " + node.nodeId + " incremented timeout counter to " + node.timeoutIgnoreCounter + " at time " + time);
            node.fetchEpochs(time);
        }
    }

    // Current stage in the node's validation cycle times out
    @Override
    public void event_NodeTimeout(long time) {
        //System.out.println("Node " + node.nodeId + " timed out." +" at time " + time);
        node.handleTimeout(time);
    }


    // Node queries for its rewardjar and redeems all pennies in it
    @Override
    public void event_NodeCheckRewardJar(long time) {
        ////System.out.println("Node " + node.nodeId + " is fetching rewardjar at time " + time);
        Client client = node.clients.pickSpecificClient(node.getID());  // get corresponding client to the node
        sim.schedule(new Event_ClientCheckRewardJar(client, time));
        sim.schedule(new Event_NodeCheckRewardJar(node, time + node.checkRewardJarInterval));  // also schedule the next check
    }

    // Node receives a request for the current epoch from another node
    @Override
    public void event_NodeFetchEpochsRequest(int senderId, int epochId, long time) {
        MessageReporter.logNodeMessage("node-" + senderId, this.node.nodeId, "NodeFetchEpochsRequest", "EpochId: " + epochId, time);
        ////System.out.println("Node " + node.nodeId + " received request for epoch " + epochId + " at time " + time);
        if (epochId > node.epoch.epochId) {
            //System.out.println("Requested epochId " + epochId + " greater than current epochId " + node.epoch.epochId);
            return;
        }
        TestBlockchainNode sender = (TestBlockchainNode) sim.getNodeSet().pickSpecificNode(senderId);
        long inter = sim.getNetwork().getPropagationTime(this.node.getID(), sender.getID(), 1); // FIXME add proper size
        Event_NodeReceivesEpoch event = new Event_NodeReceivesEpoch(sender, node.getID(), node.epoch, time + inter);
        sender.sim.schedule(event);
    }

    // Node receives a request for the current epoch from a client
    @Override
    public void event_NodeSyncRequest(int senderId, long time) {
        MessageReporter.logNodeMessage(String.valueOf(senderId), this.node.nodeId, "NodeSyncRequest", "", time);
        TransactionReporter.logTrxEvent(this.node.getNodeId(), senderId, "NodeSyncRequest", node.epoch.epochId, "", time);
        Epoch epoch = node.epoch;
        Epoch copy = new Epoch(epoch.epochId, new HashSet<>(epoch.peers), epoch.fee);
        long inter = sim.getNetwork().getPropagationTime(this.node.getID(), sim.getNodeSet().getNodeSetCount() + senderId, 1); // FIXME add proper size
        Event_ClientSyncResponse event = new Event_ClientSyncResponse(node.clients.pickSpecificClient(senderId), node.getID(), copy, time + inter);
        sim.schedule(event);
    }

    // Node receives a request from a client for its account
    // FIXME expand to allow clients to query accounts that are not their own
    @Override
    public void event_NodeGetAccountRequest(int senderId, long time) {
        MessageReporter.logNodeMessage(String.valueOf(senderId), this.node.nodeId, "NodeGetAccountRequest", "", time);
        TransactionReporter.logTrxEvent(this.node.getNodeId(), senderId, "NodeGetAccountRequest", node.epoch.epochId, "", time);
        Account account = node.getAccount(senderId);
        Account copy = new Account(account.getAccountId(), account.state, account.balance, account.nonce);
        long inter = sim.getNetwork().getPropagationTime(this.node.getID(), sim.getNodeSet().getNodeSetCount() + senderId, 1); // FIXME add proper size
        Event_ClientReceivesAccount event = new Event_ClientReceivesAccount(node.clients.pickSpecificClient(senderId), node.getID(), copy, time + inter);
        sim.schedule(event);
    }

    // Node receives a request from a client for its pennyjar
    // FIXME expand to allow clients to query pennyjars that are not their own
    @Override
    public void event_NodeGetPennyJarRequest(int senderId, long time) {
        MessageReporter.logNodeMessage(String.valueOf(senderId), this.node.nodeId, "NodeGetPennyJarRequest", "", time);
        TransactionReporter.logTrxEvent(this.node.getNodeId(), senderId, "NodeGetPennyJarRequest", node.epoch.epochId, "", time);
        PennyJar pennyJar = node.getPennyJar(senderId);
        PennyJar copy = new PennyJar(new HashSet<>(pennyJar.pennies));
        long inter = sim.getNetwork().getPropagationTime(this.node.getID(), sim.getNodeSet().getNodeSetCount() + senderId, 1); // FIXME add proper size
        Event_ClientReceivesPennyJar event = new Event_ClientReceivesPennyJar(node.clients.pickSpecificClient(senderId), node.getID(), copy, time + inter);
        sim.schedule(event);
    }

    // Same thing but for rewardjar
    // TODO refactor since code is identical
    @Override
    public void event_NodeGetRewardJarRequest(int senderId, long time) {
        MessageReporter.logNodeMessage(String.valueOf(senderId), this.node.nodeId, "NodeGetRewardJarRequest", "", time);
        TransactionReporter.logTrxEvent(this.node.getNodeId(), senderId, "NodeGetRewardJarRequest", node.epoch.epochId, "", time);
        PennyJar rewardJar = node.getRewardJar(senderId);
        PennyJar copy = new PennyJar(new HashSet<>(rewardJar.pennies));
        long inter = sim.getNetwork().getPropagationTime(this.node.getID(), sim.getNodeSet().getNodeSetCount() + senderId, 1); // FIXME add proper size
        Event_ClientReceivesRewardJar event = new Event_ClientReceivesRewardJar(node.clients.pickSpecificClient(senderId), node.getID(), copy, time + inter);
        sim.schedule(event);
    }

    // Node receives a transaction request from a client (first stage of transaction)
    @Override
    public void event_NodeTxPrepareRequest(int senderId, List<Tx> txs, long time) {  // TODO refactor into TxPrepareRequest class
        MessageReporter.logNodeMessage(String.valueOf(senderId), this.node.nodeId, "TxPrepareRequest", txs.stream().map(Tx::printTx).collect(Collectors.joining(", ")), time);
        TransactionReporter.logTrxEvent(this.node.getNodeId(), senderId, "NodeTxPrepareRequest", node.epoch.epochId, "Transactions: [" + txs.stream().map(Tx::printTx).collect(Collectors.joining(", ")) + "]", time);
        TxPrepareResponse response = verifyTxPrepare(senderId, txs, time);
        if (response.error == null) {
            Account account = node.getAccount(senderId);
            account.state = AccountState.PRECOMMIT;
        }
        Client sender = node.clients.pickSpecificClient(senderId);
        long inter = sim.getNetwork().getPropagationTime(this.node.getID(), sim.getNodeSet().getNodeSetCount() + senderId, 1); // FIXME add proper size
        Event_ClientTxPrepareResponse event = new Event_ClientTxPrepareResponse(sender, response, time + inter);
        sim.schedule(event);
    }

    // Node receives a transaction, with a list of verifications by the network, from a client (second stage of transaction)
    @Override
    public void event_NodeTxAcceptRequest(TxAcceptRequest request, long time) {
        MessageReporter.logNodeMessage(String.valueOf(request.senderId), this.node.nodeId, "TxAcceptRequest", request.printRequest(), time);
        TransactionReporter.logTrxEvent(this.node.getNodeId(), request.senderId, "NodeTxAcceptRequest", node.epoch.epochId, request.printRequest(), time);
        Client client = node.clients.pickSpecificClient(request.senderId);

        HashSet<Integer> validResponses = new HashSet<>();
        for (TxPrepareResponse response : request.verifications) {
            if (response.txs.equals(request.txs) && response.error == null && response.epochId == node.epoch.epochId) {
                validResponses.add(response.senderId);
            }
        }
        if (3 * validResponses.size() <= 2 * node.epoch.peers.size()) {
            TxAcceptResponse response = new TxAcceptResponse(node.getID(), "Does not meet >2/3 consensus", request.txs);
            sim.schedule(new Event_ClientTxAcceptResponse(client, response, time));
            return;
        }

        Account account = node.getAccount(request.senderId);
        PennyJar pennyJar = node.getPennyJar(request.senderId);
        for (Tx tx : request.txs) {
            if (tx instanceof SendPennyTx) {
                Penny penny = ((SendPennyTx) tx).penny;
                TransactionReporter.logTrx(this.node.getNodeId(), penny.senderId, penny.recipientId, penny.amount, 0, "SendPennyTx", time);

                account.balance -= penny.amount + node.epoch.fee;
                PennyJar recipientJar = node.getPennyJar(penny.recipientId);
                recipientJar.pennies.add(penny);
            } else if (tx instanceof RedeemPennyTx) {
                Penny penny = ((RedeemPennyTx) tx).penny;
                TransactionReporter.logTrx(this.node.getNodeId(), penny.senderId, penny.recipientId, penny.amount, 0, "RedeemPennyTx", time);
                pennyJar.pennies.remove(penny);
                account.balance += penny.amount;
            }
            account.nonce++;
            AccountReporter.logAccount(account.getAccountId(), this.node.nodeId, tx instanceof SendPennyTx? "SendPennyTxAcceptRequest" : "RedeemPennyTxAcceptRequest", account.balance, account.state.toString(), account.nonce, time);
        }
        account.state = AccountState.FINALIZED;
        TxAcceptResponse response = new TxAcceptResponse(node.getID(), null, request.txs);
        sim.schedule(new Event_ClientTxAcceptResponse(client, response, time));
    }

    // Node receives an abort request from a client (first stage of abort)
    @Override
    public void event_NodeAbortPrepareRequest(AbortPrepareRequest request, long time) {
        MessageReporter.logNodeMessage(String.valueOf(request.senderId), this.node.nodeId, "AbortPrepareRequest", request.printRequest(), time);
        TransactionReporter.logTrxEvent(this.node.getNodeId(), request.senderId, "NodeAbortPrepareRequest", node.epoch.epochId, request.printRequest(), time);

        Account account = node.getAccount(request.senderId);
        AbortPrepareResponse response = new AbortPrepareResponse(node.getID(), null, request);
        if (request.transactionId > account.nonce) {
            response.error = "chain is leading";
        } else if (request.transactionId < account.nonce) {
            response.error = "chain is leading";
        } else {
            account.state = AccountState.PREABORT;
        }
        Client sender = node.clients.pickSpecificClient(request.senderId);
        long inter = sim.getNetwork().getPropagationTime(this.node.getID(), sim.getNodeSet().getNodeSetCount() + request.senderId, 1); // FIXME add proper size
        Event_ClientAbortPrepareResponse event = new Event_ClientAbortPrepareResponse(sender, response, time + inter);
        sim.schedule(event);
    }

    // Node receives an abort request with verifications from a client (second stage of abort)
    @Override
    public void event_NodeAbortAcceptRequest(AbortAcceptRequest request, long time) {
        MessageReporter.logNodeMessage(String.valueOf(request.senderId), this.node.nodeId, "AbortAcceptRequest", request.printRequest(), time);
        TransactionReporter.logTrxEvent(this.node.getNodeId(), request.senderId, "NodeAbortAcceptRequest", node.epoch.epochId, request.printRequest(), time);
        Client client = node.clients.pickSpecificClient(request.senderId);

        HashSet<Integer> validResponses = new HashSet<>();
        for (AbortPrepareResponse response : request.verifications) {
            if (response.abortRequest.equals(request.abortRequest) && response.error == null) {
                validResponses.add(response.senderId);
            }
        }
        if (3 * validResponses.size() <= 2 * node.epoch.peers.size()) {
            AbortAcceptResponse response = new AbortAcceptResponse(node.getID(), "Does not meet >2/3 consensus", request.abortRequest);
            sim.schedule(new Event_ClientAbortAcceptResponse(client, response, time));
            return;
        }

        Account account = node.getAccount(request.senderId);
        account.nonce++;
        AccountReporter.logAccount(account.getAccountId(), this.node.nodeId, "AbortAcceptRequest", account.balance, account.state.toString(), account.nonce, time);
        account.state = AccountState.FINALIZED;
        AbortAcceptResponse response = new AbortAcceptResponse(node.getID(), null, request.abortRequest);
        sim.schedule(new Event_ClientAbortAcceptResponse(client, response, time));
    }

    // Node shares its vote with the network
    @Override
    public void propagateVote(long time) {
        //System.out.println("Node " + node.nodeId + " propagated its vote." + " at time " + time);

        // At the moment, honest node does not vote for any additions or ejections
        node.vote = new Vote(node.epoch.epochId, node.getID(), new ArrayList<>(), new ArrayList<>());
        sim.schedule(new Event_NodeReceivesVote(node, node.vote, time));
        for (Integer peerId : node.epoch.peers) {
            INode n = sim.getNodeSet().pickSpecificNode(peerId);
            if (n instanceof TestBlockchainNode && (n.getID() != node.getID())) {
                long inter = sim.getNetwork().getPropagationTime(this.node.getID(), n.getID(), this.node.votes.size());
                Event_NodeReceivesVote event = new Event_NodeReceivesVote(n, node.vote, time + inter);
                ((TestBlockchainNode) n).sim.schedule(event);
            }
        }
    }

    // Node shares its submission (all the votes it has received) with the network
    @Override
    public void propagateSubmission(long time) {
        //System.out.println("Node " + node.nodeId + " propagated its submission." + " at time " + time);

        node.submission = new Submission(node.epoch.epochId, node.getID(), new ArrayList<>(node.votes.values()));
        sim.schedule(new Event_NodeReceivesSubmission(node, node.submission, time));
        for (Integer peerId : node.epoch.peers) {
            INode n = sim.getNodeSet().pickSpecificNode(peerId);
            if (n instanceof TestBlockchainNode && (n.getID() != node.getID())) {
                long inter = sim.getNetwork().getPropagationTime(this.node.getID(), n.getID(), this.node.submissions.size());
                Event_NodeReceivesSubmission event = new Event_NodeReceivesSubmission(n, node.submission, time + inter);
                ((TestBlockchainNode) n).sim.schedule(event);
            }
        }
    }

    // Node shares the hash of its generated epoch with the network
    @Override
    public void propagateHash(long time) {
        //System.out.println("Node " + node.nodeId + " propagated its hash." + " at time " + time);
        generateNextEpoch();
        node.nextEpochHash = node.nextEpoch.hashCode();
        node.hash = new EpochHash(node.nextEpoch.epochId, node.getID(), node.nextEpochHash);
        sim.schedule(new Event_NodeReceivesHash(node, node.hash, time));
        for (Integer peerId : node.epoch.peers) {
            INode n = sim.getNodeSet().pickSpecificNode(peerId);
            if (n instanceof TestBlockchainNode && (n.getID() != node.getID())) {
                long inter = sim.getNetwork().getPropagationTime(this.node.getID(), n.getID(), this.node.hashes.size());
                Event_NodeReceivesHash event = new Event_NodeReceivesHash(n, node.hash, time + inter);
                ((TestBlockchainNode) n).sim.schedule(event);
            }
        }
    }

    // Node queries the network for the current epoch
    @Override
    public void fetchEpochs(long time) {
        //System.out.println("Node " + node.nodeId + " is fetching epochs at time " + time);
        node.scheduleTimeout(time);
        node.fetchedEpochs.clear();
        for (Integer peerId : node.epoch.peers) {
            INode n = sim.getNodeSet().pickSpecificNode(peerId);
            if (n instanceof TestBlockchainNode && (n.getID() != node.getID())) {
                long inter = sim.getNetwork().getPropagationTime(this.node.getID(), n.getID(), 1);  // FIXME add proper size
                Event_NodeFetchEpochsRequest event = new Event_NodeFetchEpochsRequest(n, node.getID(), node.epoch.epochId + 1, time + inter);
                ((TestBlockchainNode) n).sim.schedule(event);
            }
        }
    }

    // Deposits rewards into the nodes' rewardjars for participating in the validation round
    @Override
    public void distributeValidationRewards(long time) {
        // FIXME all balances should be bigints, and this should be 10^25 instead of 10^9
        long rewardAmount = 1000000000 / (365 * node.epoch.peers.size());
        for (Integer peerId : node.epoch.peers) {
            INode n = sim.getNodeSet().pickSpecificNode(peerId);
            if (n instanceof TestBlockchainNode) {
                TestBlockchainNode tbn = (TestBlockchainNode) n;
                Penny reward = new Penny(rewardAmount, 0, tbn.getID(), 0);
                // TODO reporter
                node.getRewardJar(tbn.getID()).pennies.add(reward);
            }
        }
    }

    // Node generates what it believes the next epoch will be from the submissions it receives
    private void generateNextEpoch() {
        HashMap<Integer, HashMap<String, Integer>> perPeerVotes = new HashMap<>();
        ArrayList<String> validVotes = new ArrayList<>();
        HashMap<Integer, Integer> additions = new HashMap<>();
        HashMap<Integer, Integer> ejections = new HashMap<>();

        for (Integer peerId : node.epoch.peers) {
            if (!node.submissions.containsKey(peerId)) {
                continue;
            }
            for (Vote vote : node.submissions.get(peerId).votes) {
                String marshalledVote = vote.marshall();
                if (!perPeerVotes.containsKey(peerId)) {
                    perPeerVotes.put(peerId, new HashMap<>());
                }
                HashMap<String, Integer> peerVotes = perPeerVotes.get(peerId);
                peerVotes.put(marshalledVote, peerVotes.getOrDefault(marshalledVote, 0) + 1);
            }
        }

        for (HashMap<String, Integer> votes : perPeerVotes.values()) {
            for (Map.Entry<String, Integer> entry : votes.entrySet()) {
                String marshalledVote = entry.getKey();
                Integer count = entry.getValue();

                if (3 * count > 2 * node.epoch.peers.size()) {
                    validVotes.add(marshalledVote);
                }
            }
        }

        for (String marshalledVote : validVotes) {
            Vote vote = Vote.unmarshall(marshalledVote);
            for (Integer addPeer : vote.additions) {
                additions.put(addPeer, additions.getOrDefault(addPeer, 0) + 1);
            }
            for (Integer ejectPeer : vote.ejections) {
                ejections.put(ejectPeer, ejections.getOrDefault(ejectPeer, 0) + 1);
            }
        }

        node.nextEpoch = new Epoch(node.epoch.epochId, new HashSet<>(), node.epoch.fee);  // TODO compute fee for for next epoch
        for (Integer peerId : node.epoch.peers) {
            if (3 * ejections.getOrDefault(peerId, 0) <=  2 * node.epoch.peers.size()) {
                node.nextEpoch.peers.add(peerId);
            }
        }
        for (Map.Entry<Integer, Integer> entry : additions.entrySet()) {
            Integer peerId = entry.getKey();
            Integer count = entry.getValue();

            if (3 * count >  2 * node.epoch.peers.size()) {
                node.nextEpoch.peers.add(peerId);
            }
        }
    }

    // Helper for verifying if a transaction request is a valid
    private TxPrepareResponse verifyTxPrepare(int senderId, List<Tx> txs, long time) {
        if (txs.isEmpty()) {
            return new TxPrepareResponse(node.getID(), "transactions can't be empty", node.epoch.epochId, txs);
        }
        Account account = node.getAccount(senderId);
        if (account.state == AccountState.PRECOMMIT) {
            return new TxPrepareResponse(node.getID(), "account is in PRECOMMIT state", node.epoch.epochId, txs);
        }
        long balance = account.balance;
        long nonce = account.nonce;
        for (Tx tx : txs) {
            if (tx.transactionId > nonce) {
                return new TxPrepareResponse(node.getID(), "chain is lagging", node.epoch.epochId, txs);
            }
            if (tx.transactionId < nonce) {
                return new TxPrepareResponse(node.getID(), "chain is leading", node.epoch.epochId, txs);
            }
            nonce++;

            if (tx instanceof SendPennyTx) {
                Penny penny = ((SendPennyTx) tx).penny;
                if (penny.amount + node.epoch.fee > balance) {
                    return new TxPrepareResponse(node.getID(), "insufficient funds", node.epoch.epochId, txs);
                }
                balance -= penny.amount + node.epoch.fee;
            } else if (tx instanceof RedeemPennyTx) {
                Penny penny = ((RedeemPennyTx) tx).penny;
                if (!node.getPennyJar(senderId).pennies.contains(penny)) {
                    return new TxPrepareResponse(node.getID(), "invalid penny", node.epoch.epochId, txs);
                }
                balance += penny.amount;
            } else if (tx instanceof RedeemRewardTx) {
                Penny reward = ((RedeemRewardTx) tx).reward;
                if (!node.getRewardJar(senderId).pennies.contains(reward)) {
                    return new TxPrepareResponse(node.getID(), "invalid reward", node.epoch.epochId, txs);
                }
                balance += reward.amount;
            }
            //handel all three condition
            AccountReporter.logAccount(account.getAccountId(), this.node.nodeId, tx instanceof SendPennyTx? "SendPennyTxPrepareRequest" : tx instanceof RedeemPennyTx? "RedeemPennyTxPrepareRequest" : "RedeemRewardTxPrepareRequest", balance, account.state.toString(), nonce, time);
        }
        return new TxPrepareResponse(node.getID(), null, node.epoch.epochId, txs);
    }
}
