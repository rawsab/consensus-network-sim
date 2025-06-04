package cmg.cnsim.testblockchain.events.transaction;

import cmg.cnsim.testblockchain.client.api.AbortAcceptRequest;
import cmg.cnsim.testblockchain.node.TestBlockchainNode;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.event.Event;
import cmg.cnsim.engine.node.INode;


public class Event_NodeAbortAcceptRequest  extends Event {
    private TestBlockchainNode node;
    private AbortAcceptRequest request;

    public Event_NodeAbortAcceptRequest(INode node, AbortAcceptRequest request, long time) {
        this.node = (TestBlockchainNode) node;
        this.request = request;
        super.setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        super.happen(sim);
        node.event_NodeAbortAcceptRequest(request, getTime());
    }
}
