package cmg.cnsim.testblockchain.node.behavior;

import cmg.cnsim.testblockchain.client.api.AbortAcceptRequest;
import cmg.cnsim.testblockchain.client.api.AbortPrepareRequest;
import cmg.cnsim.testblockchain.client.api.TxAcceptRequest;
import cmg.cnsim.testblockchain.client.tx.Tx;
import cmg.cnsim.testblockchain.events.validation.Event_NodeReceivesVote;
import cmg.cnsim.testblockchain.node.TestBlockchainNode;
import cmg.cnsim.testblockchain.node.Epoch;
import cmg.cnsim.testblockchain.node.EpochHash;
import cmg.cnsim.testblockchain.node.Submission;
import cmg.cnsim.testblockchain.node.Vote;
import cmg.cnsim.testblockchain.events.validation.Event_NodeReceivesSubmission;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.node.INode;

import java.util.*;

public class MaliciousBehavior implements NodeBehavior {

    private final TestBlockchainNode node;
    private final HonestBehavior honestBehavior; // Reference to the HonestBehavior
    protected Simulation sim;
    private int targetNodeId; // The node ID to exclude

    public MaliciousBehavior(TestBlockchainNode node, Simulation sim, int targetNodeId) {
        this.node = node;
        this.honestBehavior = new HonestBehavior(node, sim);
        this.sim = sim;
        this.targetNodeId = targetNodeId;
    }

    @Override
    public void event_NodeReceivesVote(Vote vote, long time) {
        honestBehavior.event_NodeReceivesVote(vote, time);
        System.out.println("Meow1");
    }

    @Override
    public void event_NodeReceivesSubmission(Submission submission, long time) {
        honestBehavior.event_NodeReceivesSubmission(submission, time);
    }

    @Override
    public void event_NodeReceivesHash(EpochHash hash, long time) {
        honestBehavior.event_NodeReceivesHash(hash, time);
    }

    @Override
    public void event_NodeFetchEpochsRequest(int senderId, int epochId, long time) {
    }

    @Override
    public void event_NodeSyncRequest(int senderId, long time) {
    }

    @Override
    public void event_NodeGetAccountRequest(int senderId, long time) {
    }

    @Override
    public void event_NodeGetPennyJarRequest(int senderId, long time) {
    }

    @Override
    public void event_NodeGetRewardJarRequest(int senderId, long time) {
    }

    @Override
    public void event_NodeTxPrepareRequest(int senderId, List<Tx> txs, long time) {
    }

    @Override
    public void event_NodeTxAcceptRequest(TxAcceptRequest request, long time) {
    }

    public void event_NodeAbortPrepareRequest(AbortPrepareRequest request, long time) {
    }

    @Override
    public void event_NodeAbortAcceptRequest(AbortAcceptRequest request, long time) {
    }

    @Override
    public void event_NodeReceivesEpoch(int senderId, Epoch epoch, long time) {
        honestBehavior.event_NodeReceivesEpoch(senderId, epoch, time);
    }

    @Override
    public void event_NodeTimeout(long time) {
        honestBehavior.event_NodeTimeout(time);
    }

    @Override
    public void event_NodeCheckRewardJar(long time) {
        honestBehavior.event_NodeCheckRewardJar(time);
    }


    @Override
    public void propagateVote(long time) {
        System.out.println("Node " + node.nodeId + " propagated its vote." + " at time " + time);

        // Modify the vote by removing targetNodeId from additions and adding it to ejections
        List<Integer> additions = new ArrayList<>();
        List<Integer> ejections = new ArrayList<>();

        if (node.epoch.peers.contains(targetNodeId)) {
            ejections.add(targetNodeId);
        }

        node.vote = new Vote(node.epoch.epochId, node.getID(), additions, ejections);
        sim.schedule(new Event_NodeReceivesVote(node, node.vote, time));
        ArrayList<INode> nodes = sim.getNodeSet().getNodes();
        for (INode n : nodes) {
            if (n instanceof TestBlockchainNode && (n.getID() != node.getID())) {
                long inter = sim.getNetwork().getPropagationTime(this.node.getID(), n.getID(), this.node.votes.size());
                Event_NodeReceivesVote event = new Event_NodeReceivesVote(n, node.vote, time + inter);
                ((TestBlockchainNode) n).sim.schedule(event);
            }
        }
    }

    @Override
    public void propagateSubmission(long time) {
        System.out.println("Node " + node.nodeId + " propagated its submission." + " at time " + time);

        List<Vote> manipulatedVotes = new ArrayList<>(node.votes.values());
        // Filter out votes containing targetNodeId
        manipulatedVotes.removeIf(v -> v.additions.contains(targetNodeId) || !v.ejections.contains(targetNodeId));

        node.submission = new Submission(node.epoch.epochId, node.getID(), manipulatedVotes);
        sim.schedule(new Event_NodeReceivesSubmission(node, node.submission, time));
        ArrayList<INode> nodes = node.sim.getNodeSet().getNodes();
        for (INode n : nodes) {
            if (n instanceof TestBlockchainNode && (n.getID() != node.getID())) {
                long inter = sim.getNetwork().getPropagationTime(this.node.getID(), n.getID(), this.node.submissions.size());
                Event_NodeReceivesSubmission event = new Event_NodeReceivesSubmission(n, node.submission, time + inter);
                ((TestBlockchainNode) n).sim.schedule(event);
            }
        }
    }

    @Override
    public void propagateHash(long time) {
        honestBehavior.propagateHash(time);
    }

    @Override
    public void fetchEpochs(long time) {
        honestBehavior.fetchEpochs(time);
    }

    @Override
    public void distributeValidationRewards(long time) {
        honestBehavior.distributeValidationRewards(time);
    }

    public void setTargetNodeId(int targetNodeId) {
        this.targetNodeId = targetNodeId;
    }

}
