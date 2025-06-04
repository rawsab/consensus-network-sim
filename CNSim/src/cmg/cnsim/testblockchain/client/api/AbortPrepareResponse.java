package cmg.cnsim.testblockchain.client.api;

public class AbortPrepareResponse {
    public int senderId;
    public String error;
    public AbortPrepareRequest abortRequest;

    public AbortPrepareResponse(int senderId, String error, AbortPrepareRequest abortRequest) {
        this.senderId = senderId;
        this.error = error;
        this.abortRequest = abortRequest;
    }

    public String printResponse() {
        return("Sender ID: " + this.senderId + ", Error: " + this.error + ", Abort Request: [" + this.abortRequest.printRequest() + "]");
    }
}
