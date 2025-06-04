package cmg.cnsim.testblockchain.events.transaction;

import cmg.cnsim.testblockchain.client.Client;
import cmg.cnsim.testblockchain.client.PennyJar;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.event.Event;


public class Event_ClientReceivesPennyJar extends Event {
    private Client client;
    private int senderId;
    private PennyJar pennyJar;

    public Event_ClientReceivesPennyJar(Client client, int senderId, PennyJar pennyJar, long time) {
        this.client = client;
        this.senderId = senderId;
        this.pennyJar = pennyJar;
        super.setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        super.happen(sim);
        client.event_ClientReceivesPennyJar(senderId, pennyJar, getTime());
    }
}
