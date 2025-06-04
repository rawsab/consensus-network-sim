package cmg.cnsim.testblockchain.client.tx;

public abstract class Tx {
    public long transactionId;
    public long time;

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

    public abstract String printTx();
}
