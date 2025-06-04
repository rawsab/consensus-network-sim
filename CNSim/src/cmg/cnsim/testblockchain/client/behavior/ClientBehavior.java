package cmg.cnsim.testblockchain.client.behavior;

import cmg.cnsim.testblockchain.client.Account;
import cmg.cnsim.testblockchain.client.PennyJar;
import cmg.cnsim.testblockchain.client.api.AbortAcceptResponse;
import cmg.cnsim.testblockchain.client.api.AbortPrepareRequest;
import cmg.cnsim.testblockchain.client.api.AbortPrepareResponse;
import cmg.cnsim.testblockchain.client.api.TxAcceptResponse;
import cmg.cnsim.testblockchain.client.api.TxPrepareResponse;
import cmg.cnsim.testblockchain.client.tasks.CheckPennyJarTask;
import cmg.cnsim.testblockchain.client.tasks.CheckRewardJarTask;
import cmg.cnsim.testblockchain.client.tasks.GetAccountTask;
import cmg.cnsim.testblockchain.client.tasks.MakeTransactionTask;
import cmg.cnsim.testblockchain.client.tasks.SyncEpochTask;
import cmg.cnsim.testblockchain.client.tx.Tx;
import cmg.cnsim.testblockchain.node.Epoch;
import java.util.List;

public interface ClientBehavior {
    void doSyncEpochTask(SyncEpochTask task, long time);
    void doGetAccountTask(GetAccountTask task, long time);
    void doCheckPennyJarTask(CheckPennyJarTask task, long time);
    void doCheckRewardJarTask(CheckRewardJarTask task, long time);
    void doMakeTransactionTask(MakeTransactionTask task, long time);
    void event_ClientGetAccount(long time);
    void event_ClientCheckPennyJar(long time);
    void event_ClientCheckRewardJar(long time);
    void event_ClientMakeTransaction(int recipientId, long amount, long time);
    void event_ClientSyncResponse(int senderId, Epoch epoch, long time);
    void event_ClientReceivesAccount(int senderId, Account account, long time);
    void event_ClientReceivesPennyJar(int senderId, PennyJar pennyJar, long time);
    void event_ClientReceivesRewardJar(int senderId, PennyJar rewardJar, long time);
    void event_ClientTxPrepareResponse(TxPrepareResponse response, long time);
    void event_ClientTxAcceptResponse(TxAcceptResponse response, long time);
    void event_ClientTxPrepareTimeout(List<Tx> txs, long time);
    void event_ClientTxAcceptTimeout(List<Tx> txs, long time);
    void event_ClientAbortPrepareResponse(AbortPrepareResponse response, long time);
    void event_ClientAbortAcceptResponse(AbortAcceptResponse response, long time);
    void receivesEpoch(Epoch epoch, long time);
    void receivesPennyJar(PennyJar pennyJar, long time);
    void receivesRewardJar(PennyJar rewardJar, long time);
    void receivesAccount(Account account, long time);
    void transact(List<Tx> txs, long time);
    void abort(AbortPrepareRequest request, long time);
}
