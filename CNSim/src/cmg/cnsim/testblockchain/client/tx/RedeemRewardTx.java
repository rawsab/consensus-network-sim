package cmg.cnsim.testblockchain.client.tx;

import cmg.cnsim.testblockchain.client.Penny;

public class RedeemRewardTx extends Tx {
    public Penny reward;

    public RedeemRewardTx(Penny reward, long transactionId, long time) {
        this.reward = reward;
        this.transactionId = transactionId;
        this.time = time;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RedeemRewardTx)) {
            return false;
        }
        RedeemRewardTx tx = (RedeemRewardTx) obj;
        return transactionId == tx.transactionId
            && time == tx.time
            && reward.equals(tx.reward);
    }

    @Override
    public int hashCode() {
        return Long.hashCode((transactionId * 31 + time) * 31 + reward.hashCode());
    }

    @Override
    public String printTx() {
        return "RedeemRewardTx, Penny: " + reward.printPenny() + ", Transaction ID: " + transactionId + ", Time: " + time;
    }
}
