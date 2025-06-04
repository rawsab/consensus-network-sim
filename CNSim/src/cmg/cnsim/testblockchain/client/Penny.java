package cmg.cnsim.testblockchain.client;

public class Penny {
    public long amount;
    public int senderId;
    public int recipientId;
    public long time;

    public Penny(long amount, int senderId, int recipientId, long time) {
        this.amount = amount;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.time = time;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Penny)) {
            return false;
        }
        Penny p = (Penny) obj;
        return amount == p.amount
            && senderId == p.senderId
            && recipientId == p.recipientId
            && time == p.time;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(((amount * 31 + senderId) * 31 + recipientId) * 31 + time);
    }

    public String printPenny() {
        return "Amount: " + amount + ", Sender ID: " + senderId + ", Recipient ID: " + recipientId + ", Time: " + time;
    }
}
