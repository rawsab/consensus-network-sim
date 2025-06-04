package cmg.cnsim.testblockchain.events.validation;

import cmg.cnsim.testblockchain.node.TestBlockchainNode;
import cmg.cnsim.testblockchain.reporter.ValidationReporter;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.event.Event;
import cmg.cnsim.engine.node.INode;


public class Event_NodeFetchEpochsRequest extends Event {
    private TestBlockchainNode node;
    private long time;
    private int senderId;
    private int epochId;

    public Event_NodeFetchEpochsRequest(INode node, int senderId, int epochId, long time) {
        this.node = (TestBlockchainNode) node;
        this.time = time;
        this.senderId = senderId;
        this.epochId = epochId;
        super.setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        super.happen(sim);
        ValidationReporter.logFetchEpochsRequest(node.getNodeId(), senderId, epochId, time);
        node.event_NodeFetchEpochsRequest(senderId, epochId, getTime());
    }
}
