package cmg.cnsim.testblockchain.events.validation;

import cmg.cnsim.testblockchain.node.TestBlockchainNode;
import cmg.cnsim.testblockchain.reporter.ValidationReporter;
import cmg.cnsim.testblockchain.node.Submission;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.event.Event;
import cmg.cnsim.engine.node.INode;


public class Event_NodeReceivesSubmission extends Event {
    private TestBlockchainNode node;
    private Submission submission;
    private long time;

    public Event_NodeReceivesSubmission(INode node, Submission submission, long time) {
        this.node = (TestBlockchainNode) node;
        this.submission = submission;
        this.time = time;
        super.setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        super.happen(sim);
        ValidationReporter.logSubmissionReceived(node.getNodeId(), submission.senderId, time);
        node.event_NodeReceivesSubmission(submission, getTime());
    }
}
