package cmg.cnsim.testblockchain.client.api;

import java.util.List;
import java.util.stream.Collectors;

import cmg.cnsim.testblockchain.client.tx.Tx;

// Used for verifying transactions
// Simulator assumes all messages are signed and nodes aren't allowed to lie about senderId
// or otherwise tamper with the messages.
public class TxPrepareResponse {
    public int senderId;
    public String error;
    public int epochId;
    public List<Tx> txs;

    public TxPrepareResponse(int senderId, String error, int epochId, List<Tx> txs) {
        this.senderId = senderId;
        this.error = error;
        this.epochId = epochId;
        this.txs = txs;
    }

    public String printResponse() {
        return("Sender ID: " + this.senderId + ", Error: " + this.error + ", Epoch ID: " + epochId + ", Transactions: [" + this.txs.stream().map(Tx::printTx).collect(Collectors.joining(", ")) + "]");
    }
}
