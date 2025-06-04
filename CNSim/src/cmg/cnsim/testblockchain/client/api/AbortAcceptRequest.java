package cmg.cnsim.testblockchain.client.api;

import java.util.List;
import java.util.stream.Collectors;

public class AbortAcceptRequest {
    public int senderId;
    public AbortPrepareRequest abortRequest;
    public List<AbortPrepareResponse> verifications;

    public AbortAcceptRequest(int senderId, AbortPrepareRequest abortRequest, List<AbortPrepareResponse> verifications) {
        this.senderId = senderId;
        this.abortRequest = abortRequest;
        this.verifications = verifications;
    }

    public String printRequest() {
        return("Sender ID: " + this.senderId + ", Abort Request: [" + this.abortRequest.printRequest() + "], Verifications: ["
            + this.verifications.stream().map(AbortPrepareResponse::printResponse).collect(Collectors.joining(", ")) + "]");
    }
}
