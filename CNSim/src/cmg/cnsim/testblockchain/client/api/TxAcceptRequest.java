package cmg.cnsim.testblockchain.client.api;

import java.util.List;
import java.util.stream.Collectors;

import cmg.cnsim.testblockchain.client.tx.Tx;

public class TxAcceptRequest {
    public int senderId;
    public List<Tx> txs;
    public List<TxPrepareResponse> verifications;

    public TxAcceptRequest(int senderId, List<Tx> txs, List<TxPrepareResponse> verifications) {
        this.senderId = senderId;
        this.txs = txs;
        this.verifications = verifications;
    }

    public String printRequest() {
        return("Sender ID: " + this.senderId + ", Transactions: [" + this.txs.stream().map(Tx::printTx).collect(Collectors.joining(", ")) + "], Verifications: " + this.verifications);
    }
}
