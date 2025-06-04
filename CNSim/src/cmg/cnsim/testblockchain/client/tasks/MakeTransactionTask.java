package cmg.cnsim.testblockchain.client.tasks;

public class MakeTransactionTask implements ClientTask {
    public int recipientId;
    public long amount;

    public MakeTransactionTask(int recipientId, long amount) {
        this.recipientId = recipientId;
        this.amount = amount;
    }
}
