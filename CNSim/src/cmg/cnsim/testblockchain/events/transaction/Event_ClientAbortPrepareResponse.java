package cmg.cnsim.testblockchain.events.transaction;

import cmg.cnsim.testblockchain.client.Client;
import cmg.cnsim.testblockchain.client.api.AbortPrepareResponse;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.event.Event;


public class Event_ClientAbortPrepareResponse extends Event {
    private Client client;
    private AbortPrepareResponse response;

    public Event_ClientAbortPrepareResponse(Client client, AbortPrepareResponse response, long time) {
        this.client = client;
        this.response = response;
        super.setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        super.happen(sim);
        client.event_ClientAbortPrepareResponse(response, getTime());
    }
}
