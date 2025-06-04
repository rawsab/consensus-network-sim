package cmg.cnsim.testblockchain.events.transaction;

import cmg.cnsim.testblockchain.node.TestBlockchainNode;
import cmg.cnsim.engine.event.Event;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.node.INode;
import cmg.cnsim.testblockchain.node.TestBlockchainNode;

public class Event_NodeCheckRewardJar extends Event {
    TestBlockchainNode node;

    public Event_NodeCheckRewardJar(INode node, long time) {
        this.node = (TestBlockchainNode) node;
        setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        node.event_NodeCheckRewardJar(getTime());
    }
}
