package cmg.cnsim.testblockchain.events.transaction;

import cmg.cnsim.testblockchain.client.Client;
import cmg.cnsim.engine.event.Event;
import cmg.cnsim.engine.Simulation;

public class Event_ClientMakeTransaction extends Event {
    Client senderClient;
    int recipientId;
    long amount;

    public Event_ClientMakeTransaction(Client senderClient, int recipientId, long amount, long time) {
        this.senderClient = senderClient;
        this.recipientId = recipientId;
        this.amount = amount;
        setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        senderClient.event_ClientMakeTransaction(recipientId, amount, getTime());
    }
}
