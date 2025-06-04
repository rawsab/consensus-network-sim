package cmg.cnsim.testblockchain.events.transaction;

import cmg.cnsim.testblockchain.client.Client;
import cmg.cnsim.testblockchain.client.api.TxPrepareResponse;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.event.Event;


public class Event_ClientTxPrepareResponse extends Event {
    private Client client;
    private TxPrepareResponse response;

    public Event_ClientTxPrepareResponse(Client client, TxPrepareResponse response, long time) {
        this.client = client;
        this.response = response;
        super.setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        super.happen(sim);
        client.event_ClientTxPrepareResponse(response, getTime());
    }
}
