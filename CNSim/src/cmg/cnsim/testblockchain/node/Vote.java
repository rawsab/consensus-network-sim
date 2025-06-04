package cmg.cnsim.testblockchain.node;

import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.List;

public class Vote {
    public int epochId;
    public int senderId;
    public List<Integer> additions;
    public List<Integer> ejections;

    public Vote(int epochId, int senderId, List<Integer> additions, List<Integer> ejections) {
        this.epochId = epochId;
        this.additions = additions;
        this.ejections = ejections;
        this.senderId = senderId;
    }

    public String marshall() {
        StringBuilder sb = new StringBuilder();
        sb.append(epochId + "/");
        for (Integer x : additions) {
            sb.append(x + ",");
        }
        sb.append("/");
        for (Integer x : ejections) {
            sb.append(x + ",");
        }
        return sb.toString();
    }

    public static Vote unmarshall(String marshalledVote) {
        Vote vote = new Vote(0, -1, new ArrayList<>(), new ArrayList<>());
        String[] parts = marshalledVote.split("/");
        String[] additions = parts.length > 1 ? parts[1].split(",") : new String[0];
        String[] ejections = parts.length > 2 ? parts[2].split(",") : new String[0];
        vote.epochId = Integer.parseInt(parts[0]);
        for (String s : additions) {
            if (s != "") {
                vote.additions.add(Integer.parseInt(s));
            }
        }
        for (String s : ejections) {
            if (s != "") {
                vote.ejections.add(Integer.parseInt(s));
            }
        }
        return vote;
    }
}
