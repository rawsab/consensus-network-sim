package cmg.cnsim.testblockchain;

import cmg.cnsim.testblockchain.node.*;
import cmg.cnsim.testblockchain.node.behavior.FluctuatingBehavior;
import cmg.cnsim.testblockchain.node.behavior.HonestBehavior;
import cmg.cnsim.testblockchain.node.behavior.MaliciousBehavior;
import cmg.cnsim.testblockchain.node.behavior.NodeBehavior;
import cmg.cnsim.engine.Config;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.node.AbstractNodeFactory;
import cmg.cnsim.engine.node.INode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestBlockchainNodeFactory extends AbstractNodeFactory {
    int numberOfMalicious = Config.getPropertyInt("testblockchain.NumberOfMaliciousNodes");
    int numberOfFluctuating = Config.getPropertyInt("testblockchain.NumberOfFluctuatingNodes");
    public TestBlockchainNodeFactory(String behavior, Simulation s) {
        super();
        this.sim = s;
        this.sampler = sim.getSampler();
    }

    @Override
    public INode createNewNode() throws Exception {
        TestBlockchainNode node = new TestBlockchainNode(sim);
        List<String[]> csvData = readCSV("./CNSim/resources/test-blockchain/node_" + node.getID() + "_config.csv");
        node.setNodeConfig(csvData);
        node.setBehaviorStrategy();
        node.setNumberOfEpochs();
        return node;
    }

    public List<String[]> readCSV(String filePath) {
        List<String[]> data = new ArrayList<>();
        String line;
        String delimiter = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while ((line = br.readLine()) != null) {
                String[] row = line.split(delimiter);
                data.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }


}
