package cmg.cnsim.testblockchain.node;

import java.util.List;

public class Submission {
    public int epochId;
    public int senderId;
    public List<Vote> votes;

    public Submission(int epochId, int senderId, List<Vote> votes) {
        this.epochId = epochId;
        this.senderId = senderId;
        this.votes = votes;
    }
}
