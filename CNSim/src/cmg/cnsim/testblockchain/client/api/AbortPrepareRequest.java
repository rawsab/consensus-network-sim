package cmg.cnsim.testblockchain.client.api;

public class AbortPrepareRequest {
    public int senderId;
    public long transactionId;

    public AbortPrepareRequest(int senderId, long transactionId) {
        this.senderId = senderId;
        this.transactionId = transactionId;
    }

    public String printRequest() {
        return("Sender ID: " + this.senderId + ", Transaction ID: " + this.transactionId);
    }
}
