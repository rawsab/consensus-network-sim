package cmg.cnsim.testblockchain.client.api;

import java.util.List;
import java.util.stream.Collectors;

import cmg.cnsim.testblockchain.client.tx.Tx;

public class TxAcceptResponse {
    public int senderId;
    public String error;
    public List<Tx> txs;

    public TxAcceptResponse(int senderId, String error, List<Tx> txs) {
        this.senderId = senderId;
        this.error = error;
        this.txs = txs;
    }

    public String printResponse() {
        return("Sender ID: " + this.senderId + ", Error: " + this.error + ", Transactions: [" + this.txs.stream().map(Tx::printTx).collect(Collectors.joining(", ")) + "]");
    }
}
