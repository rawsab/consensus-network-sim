package cmg.cnsim.testblockchain.events.transaction;

import cmg.cnsim.testblockchain.node.TestBlockchainNode;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.event.Event;
import cmg.cnsim.engine.node.INode;
import cmg.cnsim.testblockchain.node.TestBlockchainNode;


public class Event_NodeSyncRequest extends Event {
    private TestBlockchainNode node;
    private int senderId;

    public Event_NodeSyncRequest(INode node, int senderId, long time) {
        this.node = (TestBlockchainNode) node;
        this.senderId = senderId;
        super.setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        super.happen(sim);
        node.event_NodeSyncRequest(senderId, getTime());
    }
}
