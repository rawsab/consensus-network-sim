package cmg.cnsim.testblockchain.events.validation;

import cmg.cnsim.testblockchain.reporter.ValidationReporter;
import cmg.cnsim.testblockchain.node.EpochHash;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.event.Event;
import cmg.cnsim.engine.node.INode;
import cmg.cnsim.testblockchain.node.TestBlockchainNode;

public class Event_NodeReceivesHash extends Event {
    private TestBlockchainNode node;
    private EpochHash hash;
    private long time;

    public Event_NodeReceivesHash(INode node, EpochHash hash, long time) {
        this.node = (TestBlockchainNode) node;
        this.hash = hash;
        this.time = time;
        super.setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        super.happen(sim);
        ValidationReporter.logHashReceived(node.getNodeId(), hash.senderId, time);
        node.event_NodeReceivesHash(hash, getTime());
    }
}
