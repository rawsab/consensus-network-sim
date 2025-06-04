package cmg.cnsim.testblockchain.node;

public class EpochHash {
    public int epochId;
    public int senderId;
    public int hash;

    public EpochHash(int epochId, int senderId, int hash) {
        this.epochId = epochId;
        this.senderId = senderId;
        this.hash = hash;
    }
}
