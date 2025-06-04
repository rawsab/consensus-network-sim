package cmg.cnsim.testblockchain.node.behavior;

import cmg.cnsim.testblockchain.client.api.AbortAcceptRequest;
import cmg.cnsim.testblockchain.client.api.AbortPrepareRequest;
import cmg.cnsim.testblockchain.client.api.TxAcceptRequest;
import cmg.cnsim.testblockchain.client.tx.Tx;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.testblockchain.node.TestBlockchainNode;
import cmg.cnsim.testblockchain.node.Epoch;
import cmg.cnsim.testblockchain.node.EpochHash;
import cmg.cnsim.testblockchain.node.Submission;
import cmg.cnsim.testblockchain.node.Vote;

import java.util.List;
import java.util.Random;

public class FluctuatingBehavior implements NodeBehavior {
    private final TestBlockchainNode node;
    private final Simulation sim;
    private final HonestBehavior honestBehavior;
    private final Random random;
    private double probability;

    public FluctuatingBehavior(TestBlockchainNode node, Simulation sim) {
        this.node = node;
        this.sim = sim;
        this.honestBehavior = new HonestBehavior(node, sim);
        this.random = new Random();
        //set different seed for each node
        random.setSeed(System.currentTimeMillis() + node.getID());
    }

    private boolean shouldActHonestly() {
        // 90% chance of acting honestly
        return random.nextDouble() < probability;
    }

    @Override
    public void event_NodeReceivesVote(Vote vote, long time) {
        if (shouldActHonestly()) {
            honestBehavior.event_NodeReceivesVote(vote, time);
        } else {
            System.out.println("Node " + node.nodeId + " chose to ignore vote at time " + time);
        }
    }

    @Override
    public void event_NodeReceivesSubmission(Submission submission, long time) {
        if (shouldActHonestly()) {
            honestBehavior.event_NodeReceivesSubmission(submission, time);
        } else {
            System.out.println("Node " + node.nodeId + " chose to ignore submission at time " + time);
        }
    }

    @Override
    public void event_NodeReceivesHash(EpochHash hash, long time) {
        if (shouldActHonestly()) {
            honestBehavior.event_NodeReceivesHash(hash, time);
        } else {
            System.out.println("Node " + node.nodeId + " chose to ignore hash at time " + time);
        }
    }

    @Override
    public void event_NodeReceivesEpoch(int senderId, Epoch epoch, long time) {
        if (shouldActHonestly()) {
            honestBehavior.event_NodeReceivesEpoch(senderId, epoch, time);
        } else {
            System.out.println("Node " + node.nodeId + " chose to ignore hash at time " + time);
        }
    }

    @Override
    public void event_NodeFetchEpochsRequest(int senderId, int epochId, long time) {
        if (shouldActHonestly()) {
            honestBehavior.event_NodeFetchEpochsRequest(senderId, epochId, time);
        } else {
            System.out.println("Node " + node.nodeId + " chose to ignore fetchepochs request at time " + time);
        }
    }

    @Override
    public void event_NodeSyncRequest(int senderId, long time) {
        if (shouldActHonestly()) {
            honestBehavior.event_NodeSyncRequest(senderId, time);
        } else {
            System.out.println("Node " + node.nodeId + " chose to ignore sync request at time " + time);
        }
    }

    @Override
    public void event_NodeGetAccountRequest(int senderId, long time) {
        if (shouldActHonestly()) {
            honestBehavior.event_NodeGetAccountRequest(senderId, time);
        } else {
            System.out.println("Node " + node.nodeId + " chose to ignore getaccount request at time " + time);
        }
    }

    @Override
    public void event_NodeGetPennyJarRequest(int senderId, long time) {
        if (shouldActHonestly()) {
            honestBehavior.event_NodeGetPennyJarRequest(senderId, time);
        } else {
            System.out.println("Node " + node.nodeId + " chose to ignore getpennyjar request at time " + time);
        }
    }

    @Override
    public void event_NodeGetRewardJarRequest(int senderId, long time) {
        if (shouldActHonestly()) {
            honestBehavior.event_NodeGetRewardJarRequest(senderId, time);
        } else {
            System.out.println("Node " + node.nodeId + " chose to ignore getrewardjar request at time " + time);
        }
    }

    @Override
    public void event_NodeTxPrepareRequest(int senderId, List<Tx> txs, long time) {
        if (shouldActHonestly()) {
            honestBehavior.event_NodeTxPrepareRequest(senderId, txs, time);
        } else {
            System.out.println("Node " + node.nodeId + " chose to ignore txprepare request at time " + time);
        }
    }

    @Override
    public void event_NodeTxAcceptRequest(TxAcceptRequest request, long time) {
        if (shouldActHonestly()) {
            honestBehavior.event_NodeTxAcceptRequest(request, time);
        } else {
            System.out.println("Node " + node.nodeId + " chose to ignore txaccept request at time " + time);
        }
    }

    @Override
    public void event_NodeAbortPrepareRequest(AbortPrepareRequest request, long time) {
        if (shouldActHonestly()) {
            honestBehavior.event_NodeAbortPrepareRequest(request, time);
        } else {
            System.out.println("Node " + node.nodeId + " chose to ignore abortprepare request at time " + time);
        }
    }

    @Override
    public void event_NodeAbortAcceptRequest(AbortAcceptRequest request, long time) {
        if (shouldActHonestly()) {
            honestBehavior.event_NodeAbortAcceptRequest(request, time);
        } else {
            System.out.println("Node " + node.nodeId + " chose to ignore abortaccept request at time " + time);
        }
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
        if (shouldActHonestly()) {
            honestBehavior.propagateVote(time);
        } else {
            System.out.println("Node " + node.nodeId + " chose not to propagate vote at time " + time);
        }
    }

    @Override
    public void propagateSubmission(long time) {
        if (shouldActHonestly()) {
            honestBehavior.propagateSubmission(time);
        } else {
            System.out.println("Node " + node.nodeId + " chose not to propagate submission at time " + time);
        }
    }

    @Override
    public void propagateHash(long time) {
        if (shouldActHonestly()) {
            honestBehavior.propagateHash(time);
        } else {
            System.out.println("Node " + node.nodeId + " chose not to propagate hash at time " + time);
        }
    }

    @Override
    public void fetchEpochs(long time) {
        honestBehavior.fetchEpochs(time);
    }

    @Override
    public void distributeValidationRewards(long time) {
        honestBehavior.distributeValidationRewards(time);
    }

    public void setProbability(double v) {
        this.probability = v;
    }
}
