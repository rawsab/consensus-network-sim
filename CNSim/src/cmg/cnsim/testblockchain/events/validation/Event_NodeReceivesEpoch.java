package cmg.cnsim.testblockchain.events.validation;

import cmg.cnsim.testblockchain.node.TestBlockchainNode;
import cmg.cnsim.testblockchain.reporter.ValidationReporter;
import cmg.cnsim.testblockchain.node.Epoch;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.event.Event;
import cmg.cnsim.engine.node.INode;

public class Event_NodeReceivesEpoch extends Event {
    private TestBlockchainNode node;
    private Epoch epoch;
    private long time;
    private int senderId;

    // TODO maybe make another class instead of passing senderId around
    public Event_NodeReceivesEpoch(INode node, int senderId, Epoch epoch, long time) {
        this.node = (TestBlockchainNode) node;
        this.senderId = senderId;
        this.epoch = epoch;
        this.time = time;
        super.setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        super.happen(sim);
        ValidationReporter.logEpochReceived(node.getNodeId(), senderId, time);
        node.event_NodeReceivesEpoch(senderId, epoch, getTime());
    }
}
