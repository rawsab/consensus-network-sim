package cmg.cnsim.testblockchain.events.transaction;

import cmg.cnsim.testblockchain.client.tx.Tx;
import cmg.cnsim.testblockchain.node.TestBlockchainNode;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.event.Event;
import cmg.cnsim.engine.node.INode;
import java.util.List;


public class Event_NodeTxPrepareRequest  extends Event {
    private TestBlockchainNode node;
    private int senderId;
    private List<Tx> txs;

    public Event_NodeTxPrepareRequest(INode node, int senderId, List<Tx> txs, long time) {
        this.node = (TestBlockchainNode) node;
        this.senderId = senderId;
        this.txs = txs;
        super.setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        super.happen(sim);
        node.event_NodeTxPrepareRequest(senderId, txs, getTime());
    }
}
