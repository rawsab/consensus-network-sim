package cmg.cnsim.testblockchain.events.transaction;

import cmg.cnsim.testblockchain.client.Account;
import cmg.cnsim.testblockchain.client.Client;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.event.Event;


public class Event_ClientReceivesAccount extends Event {
    private Client client;
    private int senderId;
    private Account account;

    public Event_ClientReceivesAccount(Client client, int senderId, Account account, long time) {
        this.client = client;
        this.senderId = senderId;
        this.account = account;
        super.setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        super.happen(sim);
        client.event_ClientReceivesAccount(senderId, account, getTime());
    }
}
