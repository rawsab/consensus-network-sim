package cmg.cnsim.testblockchain.events.validation;

import cmg.cnsim.testblockchain.reporter.ValidationReporter;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.event.Event;
import cmg.cnsim.engine.node.INode;
import cmg.cnsim.testblockchain.node.TestBlockchainNode;

public class Event_NodeTimeout extends Event {
    private TestBlockchainNode node;

    public Event_NodeTimeout(INode node, long time) {
        super();
        this.node = (TestBlockchainNode) node;
        super.setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        super.happen(sim);
        ValidationReporter.logTimeout(node.getNodeId(), getTime());
        node.event_NodeTimeout(getTime());
    }
}
