package cmg.cnsim.testblockchain.events.validation;

import cmg.cnsim.testblockchain.reporter.ValidationReporter;
import cmg.cnsim.testblockchain.node.Vote;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.event.Event;
import cmg.cnsim.engine.node.INode;
import cmg.cnsim.testblockchain.node.TestBlockchainNode;

public class Event_NodeReceivesVote extends Event {
    private TestBlockchainNode node;
    private Vote vote;
    private long time;

    public Event_NodeReceivesVote(INode node, Vote vote, long time) {
        this.node = (TestBlockchainNode) node;
        this.vote = vote;
        this.time = time;
        super.setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        super.happen(sim);
        ValidationReporter.logVoteReceived(node.getNodeId(), vote.senderId, time);
        node.event_NodeReceivesVote(vote, getTime());
    }
}
