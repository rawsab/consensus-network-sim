package cmg.cnsim.testblockchain.node;

import java.util.Set;

public class Epoch {
    public int epochId;
    public Set<Integer> peers;
    public long fee;

    public Epoch(int epochId, Set<Integer> peers, long fee) {
        this.epochId = epochId;
        this.peers = peers;
        this.fee = fee;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Epoch)) {
            return false;
        }
        Epoch e = (Epoch) obj;
        return epochId == e.epochId && peers.equals(e.peers) && fee == e.fee;
    }

    @Override
    public int hashCode() {
        return Long.hashCode((epochId * 31 + fee) * 31 + peers.hashCode());
    }

    public String printEpoch() {
        return("Epoch ID: " + this.epochId + ", Peers: " + this.peers + ", Fee: " + this.fee);
    }
}
