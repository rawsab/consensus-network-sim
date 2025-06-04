package cmg.cnsim.testblockchain.events.transaction;

import cmg.cnsim.testblockchain.client.api.TxAcceptRequest;
import cmg.cnsim.testblockchain.node.TestBlockchainNode;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.event.Event;
import cmg.cnsim.engine.node.INode;


public class Event_NodeTxAcceptRequest  extends Event {
    private TestBlockchainNode node;
    private TxAcceptRequest request;

    public Event_NodeTxAcceptRequest(INode node, TxAcceptRequest request, long time) {
        this.node = (TestBlockchainNode) node;
        this.request = request;
        super.setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        super.happen(sim);
        node.event_NodeTxAcceptRequest(request, getTime());
    }
}
