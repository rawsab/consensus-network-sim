package cmg.cnsim.testblockchain.node.behavior;

import cmg.cnsim.testblockchain.client.PennyJar;
import cmg.cnsim.testblockchain.client.api.AbortAcceptRequest;
import cmg.cnsim.testblockchain.client.api.AbortPrepareRequest;
import cmg.cnsim.testblockchain.client.api.TxAcceptRequest;
import cmg.cnsim.testblockchain.client.tx.Tx;
import cmg.cnsim.testblockchain.node.Epoch;
import cmg.cnsim.testblockchain.node.EpochHash;
import cmg.cnsim.testblockchain.node.Submission;
import cmg.cnsim.testblockchain.node.Vote;

import java.util.List;

public interface NodeBehavior {
    void event_NodeReceivesVote(Vote vote, long time);
    void event_NodeReceivesSubmission(Submission submission, long time);
    void event_NodeReceivesHash(EpochHash hash, long time);
    void event_NodeReceivesEpoch(int senderId, Epoch epoch, long time);
    void event_NodeTimeout(long time);
    void event_NodeCheckRewardJar(long time);
    void event_NodeFetchEpochsRequest(int senderId, int epochId, long time);
    void event_NodeSyncRequest(int senderId, long time);
    void event_NodeGetAccountRequest(int senderId, long time);
    void event_NodeGetPennyJarRequest(int senderId, long time);
    void event_NodeGetRewardJarRequest(int senderId, long time);
    void event_NodeTxPrepareRequest(int senderId, List<Tx> txs, long time);
    void event_NodeTxAcceptRequest(TxAcceptRequest request, long time);
    void event_NodeAbortPrepareRequest(AbortPrepareRequest request, long time);
    void event_NodeAbortAcceptRequest(AbortAcceptRequest request, long time);
    void propagateVote(long time);
    void propagateSubmission(long time);
    void propagateHash(long time);
    void fetchEpochs(long time);
    void distributeValidationRewards(long time);
}
