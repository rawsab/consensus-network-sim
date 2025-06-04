package cmg.cnsim.testblockchain;

import cmg.cnsim.testblockchain.client.Client;
import cmg.cnsim.testblockchain.client.behavior.ClientBehavior;
import cmg.cnsim.testblockchain.client.behavior.HonestBehavior;
import cmg.cnsim.testblockchain.node.behavior.MaliciousBehavior;
import cmg.cnsim.engine.AbstractSampler;
import cmg.cnsim.engine.Config;
import cmg.cnsim.engine.Simulation;

public class TestBlockchainClientFactory {
    int numberOfMalicious = Config.getPropertyInt("testblockchain.NumberOfMaliciousClients");
    int numberOfFluctuating = Config.getPropertyInt("testblockchain.NumberOfFluctuatingClients");
    Simulation sim;
    AbstractSampler sampler;

    public TestBlockchainClientFactory(Simulation s){
        this.sim = s;
        this.sampler = sim.getSampler();
    }

    public Client createNewClient(int id) throws Exception {
        Client client = new Client(id, sim);
        client.setSimulation(sim);

        ClientBehavior behavior;
        behavior = new HonestBehavior(client, sim);
        client.setBehaviorStrategy(behavior);

        return client;
    }
}
