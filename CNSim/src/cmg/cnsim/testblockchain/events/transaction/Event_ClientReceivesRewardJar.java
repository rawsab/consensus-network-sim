package cmg.cnsim.testblockchain.events.transaction;

import cmg.cnsim.testblockchain.client.Client;
import cmg.cnsim.testblockchain.client.PennyJar;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.event.Event;


public class Event_ClientReceivesRewardJar extends Event {
    private Client client;
    private int senderId;
    private PennyJar rewardJar;

    public Event_ClientReceivesRewardJar(Client client, int senderId, PennyJar rewardJar, long time) {
        this.client = client;
        this.senderId = senderId;
        this.rewardJar = rewardJar;
        super.setTime(time);
    }

    @Override
    public void happen(Simulation sim) {
        super.happen(sim);
        client.event_ClientReceivesRewardJar(senderId, rewardJar, getTime());
    }
}
