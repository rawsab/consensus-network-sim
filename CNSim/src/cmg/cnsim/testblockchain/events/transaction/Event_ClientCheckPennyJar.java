package cmg.cnsim.testblockchain.events.transaction;

import cmg.cnsim.testblockchain.client.Client;
import cmg.cnsim.engine.event.Event;
import cmg.cnsim.engine.Simulation;

public class Event_ClientCheckPennyJar extends Event {
    Client client;

    public Event_ClientCheckPennyJar(Client client, long time) {
        this.client = client;
        setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        client.event_ClientCheckPennyJar(getTime());
    }
}
