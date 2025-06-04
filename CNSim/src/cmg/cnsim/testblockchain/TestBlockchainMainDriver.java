package cmg.cnsim.testblockchain;

import cmg.cnsim.testblockchain.client.Client;
import cmg.cnsim.testblockchain.client.ClientSet;
import cmg.cnsim.testblockchain.events.transaction.Event_ClientGetAccount;
import cmg.cnsim.testblockchain.node.TestBlockchainNode;
import cmg.cnsim.testblockchain.node.Epoch;
import cmg.cnsim.testblockchain.reporter.*;
import cmg.cnsim.engine.AbstractSampler;
import cmg.cnsim.engine.Config;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.StandardSampler;
import cmg.cnsim.engine.network.AbstractNetwork;
import cmg.cnsim.engine.network.RandomEndToEndNetwork;
import cmg.cnsim.engine.node.AbstractNodeFactory;
import cmg.cnsim.engine.node.INode;
import cmg.cnsim.engine.node.NodeSet;

import java.util.HashSet;

public class TestBlockchainMainDriver {
    public static void main(String[] args) throws Exception {
        TestBlockchainMainDriver driver = new TestBlockchainMainDriver();
        driver.run();
    }

    private void run() throws Exception {
        Config.init("./CNSim/resources/config.txt");
        AbstractSampler sampler = new StandardSampler();
        sampler.LoadConfig();

        Simulation s = new Simulation(sampler);

        AbstractNodeFactory nf = new TestBlockchainNodeFactory("Honest", s);
        TestBlockchainClientFactory cf = new TestBlockchainClientFactory(s);

        ClientSet cs = new ClientSet(cf);
        NodeSet ns = new NodeSet(nf);


        ns.addNodes(Config.getPropertyInt("net.numOfNodes"));
        cs.addClients(Config.getPropertyInt("net.numOfClients"));

        AbstractNetwork n = new RandomEndToEndNetwork(ns, cs, sampler);
        s.setNetwork(n);

        // We should define a validation process here
        //for each node in ns:
        HashSet<Integer> initialPeers = new HashSet<>();
        for (INode node : ns.getNodes()) {
            initialPeers.add(node.getID());
        }
        for (INode node : ns.getNodes()) {
            TestBlockchainNode testBlockchainNode = (TestBlockchainNode) node;
            testBlockchainNode.setInitialPeers(new HashSet<>(initialPeers));
            testBlockchainNode.setClients(cs);
            testBlockchainNode.initialize(1);
            testBlockchainNode.startCheckRewardJar(1);
        }

        for (Client client : cs.getClients()) {
            client.setInitialEpoch(new Epoch(0, new HashSet<>(initialPeers), 0)); // FIXME initial fee
        }

        TestBlockchainWorkload tbw = new TestBlockchainWorkload(s, cs);
        tbw.appendTransactionsFromFile();
        tbw.appendPennyCheckFromFile();



        s.schedule(tbw);
        // s.getQueue().iterator().forEachRemaining(System.out::println);

        // We should define a transaction workload here
        s.run();

        ValidationReporter.flushEventLog();
        ValidationReporter.flushStateLog();
        TransactionReporter.flushTrxEventLog();
        TransactionReporter.flushTrxLog();
        ClientReporter.flushClientLog();
        AccountReporter.flushAccountLog();
        MessageReporter.flushClientLog();
        MessageReporter.flushNodeLog();
        MessageReporter.flushStateLog();
    }
}
