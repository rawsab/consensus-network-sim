package cmg.cnsim.testblockchain.events.transaction;

import java.util.List;
import cmg.cnsim.testblockchain.client.Client;
import cmg.cnsim.testblockchain.client.tx.Tx;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.event.Event;


public class Event_ClientTxAcceptTimeout extends Event {
    private Client client;
    List<Tx> txs;

    public Event_ClientTxAcceptTimeout(Client client, List<Tx> txs, long time) {
        this.client = client;
        this.txs = txs;
        super.setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        super.happen(sim);
        client.event_ClientTxAcceptTimeout(txs, getTime());
    }
}
