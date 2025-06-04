package cmg.cnsim.testblockchain.events.transaction;

import cmg.cnsim.testblockchain.client.Client;
import cmg.cnsim.testblockchain.node.Epoch;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.event.Event;


public class Event_ClientSyncResponse extends Event {
    private Client client;
    private int senderId;
    private Epoch epoch;

    public Event_ClientSyncResponse(Client client, int senderId, Epoch epoch, long time) {
        this.client = client;
        this.senderId = senderId;
        this.epoch = epoch;
        super.setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        super.happen(sim);
        client.event_ClientSyncResponse(senderId, epoch, getTime());
    }
}
