package cmg.cnsim.testblockchain.events.transaction;

import cmg.cnsim.testblockchain.client.Client;
import cmg.cnsim.testblockchain.client.api.TxAcceptResponse;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.event.Event;


public class Event_ClientTxAcceptResponse extends Event {
    private Client client;
    private TxAcceptResponse response;

    public Event_ClientTxAcceptResponse(Client client, TxAcceptResponse response, long time) {
        this.client = client;
        this.response = response;
        super.setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        super.happen(sim);
        client.event_ClientTxAcceptResponse(response, getTime());
    }
}
