package cmg.cnsim.testblockchain.events.transaction;

import cmg.cnsim.testblockchain.client.Client;
import cmg.cnsim.testblockchain.client.api.AbortAcceptResponse;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.event.Event;


public class Event_ClientAbortAcceptResponse extends Event {
    private Client client;
    private AbortAcceptResponse response;

    public Event_ClientAbortAcceptResponse(Client client, AbortAcceptResponse response, long time) {
        this.client = client;
        this.response = response;
        super.setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        super.happen(sim);
        client.event_ClientAbortAcceptResponse(response, getTime());
    }
}
