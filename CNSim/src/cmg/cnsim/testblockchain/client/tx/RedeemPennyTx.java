package cmg.cnsim.testblockchain.client.tx;

import cmg.cnsim.testblockchain.client.Penny;

public class RedeemPennyTx extends Tx {
    public Penny penny;

    public RedeemPennyTx(Penny penny, long transactionId, long time) {
        this.penny = penny;
        this.transactionId = transactionId;
        this.time = time;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RedeemPennyTx)) {
            return false;
        }
        RedeemPennyTx tx = (RedeemPennyTx) obj;
        return transactionId == tx.transactionId
            && time == tx.time
            && penny.equals(tx.penny);
    }

    @Override
    public int hashCode() {
        return Long.hashCode((transactionId * 31 + time) * 31 + penny.hashCode());
    }

    @Override
    public String printTx() {
        return "RedeemPennyTx, Penny: " + penny.printPenny() + ", Transaction ID: " + transactionId + ", Time: " + time;
    }
}
