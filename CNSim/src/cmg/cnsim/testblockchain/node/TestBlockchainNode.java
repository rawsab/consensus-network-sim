package cmg.cnsim.testblockchain.node;

import cmg.cnsim.testblockchain.client.Account;
import cmg.cnsim.testblockchain.client.AccountState;
import cmg.cnsim.testblockchain.client.Client;
import cmg.cnsim.testblockchain.client.ClientSet;
import cmg.cnsim.testblockchain.client.PennyJar;
import cmg.cnsim.testblockchain.client.api.AbortAcceptRequest;
import cmg.cnsim.testblockchain.client.api.AbortPrepareRequest;
import cmg.cnsim.testblockchain.client.api.TxAcceptRequest;
import cmg.cnsim.testblockchain.client.tx.Tx;
import cmg.cnsim.testblockchain.events.transaction.Event_NodeCheckRewardJar;
import cmg.cnsim.testblockchain.events.validation.Event_NodeTimeout;
import cmg.cnsim.testblockchain.node.behavior.NodeBehavior;
import cmg.cnsim.testblockchain.node.behavior.HonestBehavior;
import cmg.cnsim.testblockchain.node.behavior.MaliciousBehavior;
import cmg.cnsim.testblockchain.node.behavior.FluctuatingBehavior;
import cmg.cnsim.testblockchain.reporter.AccountReporter;
import cmg.cnsim.testblockchain.reporter.ValidationReporter;
import cmg.cnsim.engine.IStructure;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.node.INode;
import cmg.cnsim.engine.node.Node;
import cmg.cnsim.engine.transaction.ITxContainer;
import cmg.cnsim.engine.transaction.Transaction;

import java.util.*;

public class TestBlockchainNode extends Node {
    public String nodeId;  // FIXME refactor to an int
    public Simulation sim;
    public NodeState state;
    private NodeBehavior behavior;
    private final HonestBehavior honestBehavior;
    private final MaliciousBehavior maliciousBehavior;
    private final FluctuatingBehavior fluctuatingBehavior;
    private List<String[]> nodeConfig;
    private int numberOfEpochs;
    public ClientSet clients;

    public boolean registered;
    public static int nodeCounter = 0;
    public int timeoutIgnoreCounter = 0;
    public int timeoutRemaining = 0;
    public int flag = 0;

    // Vote/submission/hash the node submits this round
    public Vote vote;
    public Submission submission;
    public EpochHash hash;

    // Map from node id of sender to vote/submission/hash/epoch/account/pennyjar received this round
    public Map<Integer, Vote> votes;
    public Map<Integer, Submission> submissions;
    public Map<Integer, EpochHash> hashes;
    public Map<Integer, Epoch> fetchedEpochs;

    // How often the node should check and redeem its rewardjar for any pennies
    // FIXME should make configurable for each node
    public long checkRewardJarInterval = 100000;

    // Map from client id to account/pennyjar
    public Map<Integer, Account> accounts;
    public Map<Integer, PennyJar> pennyJars;

    // Map from node id to rewardjar
    public Map<Integer, PennyJar> rewardJars;  

    // Array of the NodeStates of all the nodes in the network, indexed by node id
    ArrayList<Object> networkStates = new ArrayList<>();

    public Epoch epoch;  // current epoch
    public Epoch nextEpoch;  // next epoch generated from received submissions
    public int nextEpochHash;
    

    public TestBlockchainNode(Simulation sim) {
        super(sim);
        this.sim = sim;
        this.state = NodeState.WAITING;
        this.votes = new HashMap<>();
        this.submissions = new HashMap<>();
        this.hashes = new HashMap<>();
        this.accounts = new HashMap<>();
        this.pennyJars = new HashMap<>();
        this.rewardJars = new HashMap<>();
        this.fetchedEpochs = new HashMap<>();
        this.epoch = new Epoch(0, new HashSet<>(), 100);  // TODO initial fee (hardcoded to 100 here for demo purposes)
        this.nodeId = "node-" + (++nodeCounter);
        this.registered = false;
        this.honestBehavior = new HonestBehavior(this, sim);
        this.maliciousBehavior = new MaliciousBehavior(this, sim, 0);
        this.fluctuatingBehavior = new FluctuatingBehavior(this, sim);
    }

    public void setInitialPeers(Set<Integer> peers) {
        this.epoch.peers = peers;
    }

    public void setClients(ClientSet clients) {
        this.clients = clients;

        // FIXME just for demo; should be able to config starting balances of each account later
        for (Client client : clients.getClients()) {
            accounts.put(client.getClientId(), new Account(client.getClientId(), AccountState.FINALIZED, 10000, 0));
            AccountReporter.logAccount(client.getClientId(), this.nodeId, "Account Created", 10000, "FINALIZED", 0, 0);
        }
    }

    public void startCheckRewardJar(long time) {
        sim.schedule(new Event_NodeCheckRewardJar(this, time + checkRewardJarInterval));
    }

    // Called at the start of every validation round
    public void initialize(long time) {
        ValidationReporter.logInitialization(this.nodeId, time);
        // Initialization logic
        System.out.println("Initialized node: " + this.nodeId + "in time " + time);
        this.votes.clear();
        this.submissions.clear();
        this.hashes.clear();
        NodeState oldState = this.state;
        this.state = NodeState.WAITING;
        ValidationReporter.logStateChange(this.nodeId, oldState, this.state, time);
        this.registered = false;
        this.networkStates.clear();
        this.timeoutIgnoreCounter = 0;
        this.timeoutRemaining = 0;
        scheduleTimeout(time);
    }

    // Called at the start of every validation round
    public void register(long time) {
        this.registered = true;
        if (flag < numberOfEpochs - 1) {  // validation will run for numberOfEpochs rounds
            initialize(time);
            System.out.println("-----------");
            flag++;
            this.setBehaviorStrategy();
        }
        ValidationReporter.logRegistration(this.nodeId, time, epoch.epochId);
        System.out.println("Node registered: " + this.nodeId + " in epoch: " + epoch.epochId + " at time " + time);
    }


    // Called whenever the timeout of the node's current stage in the validation cycle is reached
    // Otherwise, timeoutIgnoreCounter is incremented whenever we want to manually transition to a new stage before the current timeout ends
    public void handleTimeout(long time) {
        NodeState oldState = this.state;
        timeoutRemaining--;
        if (timeoutIgnoreCounter > 0) {
            System.out.println("timeout ignored");
            timeoutIgnoreCounter--;
            System.out.println("Node " + this.nodeId + " now has " + timeoutIgnoreCounter + " timeouts left to ignore.");
            System.out.println("Node " + this.nodeId + " now has " + timeoutRemaining + " timeouts remaining.");
            return;
        }
        if (this.state == NodeState.WAITING) {
            this.state = NodeState.INITIATED;
            System.out.println("Node " + this.nodeId + " changed state to INITIATED" + " at time " + time);
            scheduleTimeout(time);
            ValidationReporter.logStateChange(this.nodeId, oldState, this.state, time);
            propagateVote(time);
            return;
        }

        int temp1 = 0;
        int temp2 = 0;
        int temp3 = 0;
        if (this.state == NodeState.INITIATED) {
            System.out.println("Node " + this.nodeId + " going to fault suspicion initiated");
            this.state = NodeState.FAULT_SUSPICION_INITIATED;
            ValidationReporter.logStateChange(this.nodeId, oldState, this.state, time);
            oldState = this.state;
            updateNetworkStates();
            for (Object neighborState : networkStates) {
                System.out.println("Node " + this.nodeId + " neighbor state: " + neighborState);
                if (neighborState == NodeState.WAITING
                        || neighborState == NodeState.INITIATED
                        || neighborState == NodeState.WAITING_VALIDATE_SUBMISSIONS
                        || neighborState == NodeState.FAULT_SUSPICION_INITIATED) {
                    temp1++;
                }

                if (neighborState == NodeState.WAITING_VALIDATE_SUBMISSIONS
                        || neighborState == NodeState.VALIDATE_SUBMISSIONS
                        || neighborState == NodeState.WAITING_VALIDATE_HASH
                        || neighborState == NodeState.FAULT_SUSPICION_VALIDATE_SUBMISSIONS) {
                    temp2++;
                }

                if (neighborState == NodeState.WAITING_VALIDATE_HASH
                        || neighborState == NodeState.VALIDATE_HASH
                        || neighborState == NodeState.FAULT_SUSPICION_VALIDATE_HASH
                        || neighborState == NodeState.COMPLETED) {
                    temp3++;
                }
            }
            if (3 * temp1 > 2 * epoch.peers.size()) {
                System.out.println("Node " + this.nodeId + " going back to INITIATED from FAULT_SUSPICION_INITIATED");
                this.state = NodeState.INITIATED;
                ValidationReporter.logStateChange(this.nodeId, oldState, this.state, time);
                scheduleTimeout(time);
                return;
            }

            if (3 * temp2 > 2 * epoch.peers.size()
                    || 3 * temp3 > 2 * epoch.peers.size()) {
                System.out.println("Node " + this.nodeId + " going to Node Fault from FAULT_SUSPICION_INITIATED");
                this.state = NodeState.NODE_FAULT;
                ValidationReporter.logStateChange(this.nodeId, oldState, this.state, time);
                fetchEpochs(time);
                return;
            }

            else {
                this.state = NodeState.NETWORK_FAULT;
                System.out.println("Node " + this.nodeId + " going to Network Fault from FAULT_SUSPICION_INITIATED");
                ValidationReporter.logStateChange(this.nodeId, oldState, this.state, time);
                return;
            }
        }

        if (this.state == NodeState.WAITING_VALIDATE_SUBMISSIONS) {
            this.state = NodeState.VALIDATE_SUBMISSIONS;
            System.out.println("Node " + this.nodeId + " changed state to VALIDATE_SUBMISSIONS" + " at time " + time);
            scheduleTimeout(time);
            ValidationReporter.logStateChange(this.nodeId, oldState, this.state, time);
            propagateSubmission(time);
            return;
        }

        if (this.state == NodeState.VALIDATE_SUBMISSIONS) {
            this.state = NodeState.FAULT_SUSPICION_VALIDATE_SUBMISSIONS;
            ValidationReporter.logStateChange(this.nodeId, oldState, this.state, time);
            oldState = this.state;
            System.out.println("Node " + this.nodeId + " going to fault suspicion validate submissions");
            updateNetworkStates();
            for (Object neighborState : networkStates) {
                if (neighborState == NodeState.WAITING_VALIDATE_SUBMISSIONS
                        || neighborState == NodeState.VALIDATE_SUBMISSIONS
                        || neighborState == NodeState.WAITING_VALIDATE_HASH
                        || neighborState == NodeState.FAULT_SUSPICION_VALIDATE_SUBMISSIONS) {
                    temp1++;

                }  if (neighborState == NodeState.WAITING
                        || neighborState == NodeState.INITIATED
                        || neighborState == NodeState.WAITING_VALIDATE_SUBMISSIONS
                        || neighborState == NodeState.FAULT_SUSPICION_INITIATED) {
                    temp2++;
                }  if (neighborState == NodeState.WAITING_VALIDATE_HASH
                        || neighborState == NodeState.VALIDATE_HASH
                        || neighborState == NodeState.FAULT_SUSPICION_VALIDATE_HASH
                        || neighborState == NodeState.COMPLETED) {
                    temp3++;
                }
            }

            if (3 * temp1 > 2 * epoch.peers.size()) {
                this.state = NodeState.VALIDATE_SUBMISSIONS;
                System.out.println("Node " + this.nodeId + " going back to VALIDATE_SUBMISSION from FAULT_SUSPICION_VALIDATE_SUBMISSIONS");
                scheduleTimeout(time);
                ValidationReporter.logStateChange(this.nodeId, oldState, this.state, time);
                return;
            }
            if (3 * temp2 > 2 * epoch.peers.size()
                    || 3 * temp3 > 2 * epoch.peers.size()) {
                System.out.println("Node " + this.nodeId + " going to Node Fault from FAULT_SUSPICION_VALIDATE_SUBMISSIONS");
                this.state = NodeState.NODE_FAULT;
                ValidationReporter.logStateChange(this.nodeId, oldState, this.state, time);
                fetchEpochs(time);
                return;
            }
            else {
                this.state = NodeState.NETWORK_FAULT;
                System.out.println("Node " + this.nodeId + " going to Network Fault from FAULT_SUSPICION_VALIDATE_SUBMISSIONS");
                ValidationReporter.logStateChange(this.nodeId, oldState, this.state, time);
                return;
            }
        }


        if (this.state == NodeState.WAITING_VALIDATE_HASH) {
            this.state = NodeState.VALIDATE_HASH;
            ValidationReporter.logStateChange(this.nodeId, oldState, this.state, time);
            propagateHash(time);
            scheduleTimeout(time);
            return;
        }

        if (this.state == NodeState.VALIDATE_HASH) {
            this.state = NodeState.FAULT_SUSPICION_VALIDATE_HASH;
            ValidationReporter.logStateChange(this.nodeId, oldState, this.state, time);
            oldState = this.state;
            System.out.println("Node " + this.nodeId + " going to fault suspicion validate hash");
            updateNetworkStates();
            for (Object neighborState : networkStates) {
                if (neighborState == NodeState.WAITING_VALIDATE_HASH
                        || neighborState == NodeState.VALIDATE_HASH
                        || neighborState == NodeState.FAULT_SUSPICION_VALIDATE_HASH
                        || neighborState == NodeState.COMPLETED) {
                    temp1++;
                }  if (neighborState == NodeState.WAITING
                        || neighborState == NodeState.INITIATED
                        || neighborState == NodeState.WAITING_VALIDATE_SUBMISSIONS
                        || neighborState == NodeState.FAULT_SUSPICION_INITIATED) {
                    temp2++;
                }  if (neighborState == NodeState.WAITING_VALIDATE_SUBMISSIONS
                        || neighborState == NodeState.VALIDATE_SUBMISSIONS
                        || neighborState == NodeState.WAITING_VALIDATE_HASH
                        || neighborState == NodeState.FAULT_SUSPICION_VALIDATE_SUBMISSIONS) {
                    temp3++;
                }
            }

            if (3 * temp1 > 2 * epoch.peers.size()) {
                System.out.println("Node " + this.nodeId + " going back to VALIDATE_HASH from FAULT_SUSPICION_VALIDATE_HASH");
                this.state = NodeState.VALIDATE_HASH;
                scheduleTimeout(time);
                ValidationReporter.logStateChange(this.nodeId, oldState, this.state, time);
                return;
            }
            if (3 * temp2 > 2 * epoch.peers.size() || 3 * temp3 > 2 * epoch.peers.size()) {
                System.out.println("Node " + this.nodeId + " going to Node Fault from FAULT_SUSPICION_VALIDATE_HASH");
                this.state = NodeState.NODE_FAULT;
                ValidationReporter.logStateChange(this.nodeId, oldState, this.state, time);
                fetchEpochs(time);
                return;
            }
            else {
                this.state = NodeState.NETWORK_FAULT;
                System.out.println("Node " + this.nodeId + " going to Network Fault from FAULT_SUSPICION_VALIDATE_HASH");
                ValidationReporter.logStateChange(this.nodeId, oldState, this.state, time);
                return;
            }
        }

        if (this.state == NodeState.COMPLETED) {
            register(time);
        }

        if (this.state == NodeState.NODE_FAULT) {
            fetchEpochs(time);
        }
    }


    public void printState() {
        System.out.println("Current state of " + this.nodeId + ": " + this.state);
    }

    public String getNodeId() {
        return nodeId;
    }


    @Override
    public IStructure getStructure() {
        return null;
    }

    @Override
    public void timeAdvancementReport() {

    }

    @Override
    public void periodicReport() {

    }

    @Override
    public void close(INode n) {

    }

    // The Validation Events:



    public void event_NodeReceivesVote(Vote vote, long time) {
        behavior.event_NodeReceivesVote(vote, time);
    }

    public void event_NodeReceivesSubmission(Submission submission, long time) {
        behavior.event_NodeReceivesSubmission(submission, time);
    }

    public void event_NodeReceivesHash(EpochHash hash, long time) {
        behavior.event_NodeReceivesHash(hash, time);
    }

    public void event_NodeReceivesEpoch(int senderId, Epoch epoch, long time) {
        behavior.event_NodeReceivesEpoch(senderId, epoch, time);
    }

    public void event_NodeTimeout(long time){
        behavior.event_NodeTimeout(time);
    }

    public void event_NodeCheckRewardJar(long time) {
        behavior.event_NodeCheckRewardJar(time);
    }

    public void event_NodeFetchEpochsRequest(int senderId, int epochId, long time) {
        behavior.event_NodeFetchEpochsRequest(senderId, epochId, time);
    }

    public void event_NodeSyncRequest(int senderId, long time) {
        behavior.event_NodeSyncRequest(senderId, time);
    }

    public void event_NodeGetAccountRequest(int senderId, long time) {
        behavior.event_NodeGetAccountRequest(senderId, time);
    }

    public void event_NodeGetPennyJarRequest(int senderId, long time) {
        behavior.event_NodeGetPennyJarRequest(senderId, time);
    }

    public void event_NodeGetRewardJarRequest(int senderId, long time) {
        behavior.event_NodeGetRewardJarRequest(senderId, time);
    }

    public void event_NodeTxPrepareRequest(int senderId, List<Tx> txs, long time) {
        behavior.event_NodeTxPrepareRequest(senderId, txs, time);
    }

    public void event_NodeTxAcceptRequest(TxAcceptRequest request, long time) {
        behavior.event_NodeTxAcceptRequest(request, time);
    }

    public void event_NodeAbortPrepareRequest(AbortPrepareRequest request, long time) {
        behavior.event_NodeAbortPrepareRequest(request, time);
    }

    public void event_NodeAbortAcceptRequest(AbortAcceptRequest request, long time) {
        behavior.event_NodeAbortAcceptRequest(request, time);
    }

    public void propagateVote(long time) {
        behavior.propagateVote(time);
    }

    public void propagateSubmission(long time) {
        behavior.propagateSubmission(time);
    }

    public void propagateHash(long time) {
        behavior.propagateHash(time);
    }

    public void fetchEpochs(long time) {
        behavior.fetchEpochs(time);
    }

    public void distributeValidationRewards(long time) {
        behavior.distributeValidationRewards(time);
    }

    // The duration of every timeout is currently hardcoded to 50000 + 100 * node id
    // TODO the timeout should be configurable for each node
    public void scheduleTimeout(long time) {
        System.out.println("Node " + this.nodeId + " scheduled timeout." + " at time " + time);
        timeoutRemaining++;
        System.out.println("Node " + this.nodeId + " has " + timeoutIgnoreCounter + " timeouts left to ignore.");
        System.out.println("Node " + this.nodeId + " has " + timeoutRemaining + " timeouts remaining.");
        Event_NodeTimeout event = new Event_NodeTimeout(this, time + 50000 + this.getID()* 100L);
        sim.schedule(event);
    }

    // FIXME currently happens "instantly" (does not use the event queue)
    public void updateNetworkStates() {
        networkStates.clear();
        for (INode node : sim.getNodeSet().getNodes()) {
            if (node instanceof TestBlockchainNode) {
                networkStates.add(((TestBlockchainNode) node).state);
            }
        }
        System.out.println(networkStates);
    }

    @Override
    public void event_NodeReceivesClientTransaction(Transaction t, long time) {
    }

    @Override
    public void event_NodeReceivesPropagatedContainer(ITxContainer t) {

    }

    public NodeState getState() {
        return state;
    }

    public CharSequence getVotes() {
        return (CharSequence) votes.toString();
    }


    public CharSequence getSubmissions() {
        return (CharSequence) submissions.toString();
    }

    public CharSequence getHashes() {
        return (CharSequence) hashes.toString();
    }

    public Epoch getEpoch() {
        return epoch;
    }

    public void setNodeConfig(List<String[]> nodeConfig) {
        this.nodeConfig = nodeConfig;
    }

    public void setBehaviorStrategy() {
        switch (nodeConfig.get(epoch.epochId+1)[1]) {
            case "Honest":
                this.behavior = this.honestBehavior;
                break;
            case "Malicious":
                this.behavior = this.maliciousBehavior;
                maliciousBehavior.setTargetNodeId(Integer.parseInt(nodeConfig.get(epoch.epochId+1)[2]));
                break;
            case "Fluctuating":
                this.behavior = this.fluctuatingBehavior;
                fluctuatingBehavior.setProbability(Double.parseDouble(nodeConfig.get(epoch.epochId+1)[3]));
                break;
            default:
                System.out.println("Invalid behavior strategy");
                this.behavior = this.honestBehavior;
        }
    }

    public void setBehaviorStrategy(NodeBehavior behavior) {
        this.behavior = behavior;
    }

    public void setNumberOfEpochs() {
        this.numberOfEpochs = nodeConfig.size()-1;
    }

    public Simulation getSim() {
        return sim;
    }

    // Used by the NodeBehavior to query the node's account/pennyjar database

    public Account getAccount(int senderId) {
        if (accounts.containsKey(senderId)) {
            return accounts.get(senderId);
        }
        Account account = new Account(senderId, AccountState.FINALIZED, 0, 0);
        accounts.put(senderId, account);
        return account;
    }
    
    public PennyJar getPennyJar(int senderId) {
        if (pennyJars.containsKey(senderId)) {
            return pennyJars.get(senderId);
        }
        PennyJar pennyJar = new PennyJar(new HashSet<>());
        pennyJars.put(senderId, pennyJar);
        return pennyJar;
    }

    public PennyJar getRewardJar(int senderId) {
        if (rewardJars.containsKey(senderId)) {
            return rewardJars.get(senderId);
        }
        PennyJar rewardJar = new PennyJar(new HashSet<>());
        pennyJars.put(senderId, rewardJar);
        return rewardJar;
    }
}
