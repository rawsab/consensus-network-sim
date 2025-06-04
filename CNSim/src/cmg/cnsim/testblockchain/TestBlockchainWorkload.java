package cmg.cnsim.testblockchain;

import cmg.cnsim.testblockchain.client.Client;
import cmg.cnsim.testblockchain.client.ClientSet;
import cmg.cnsim.testblockchain.events.transaction.Event_ClientCheckPennyJar;
import cmg.cnsim.testblockchain.events.transaction.Event_ClientGetAccount;
import cmg.cnsim.testblockchain.events.transaction.Event_ClientMakeTransaction;
import cmg.cnsim.engine.AbstractSampler;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.event.Event;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestBlockchainWorkload {
    private AbstractSampler sampler;
    private Simulation sim;
    private ClientSet clients;
    private long timeEnd = 0;
    public List<Event> events = new ArrayList<>();

    // Constructor with sampler
    public TestBlockchainWorkload(AbstractSampler sampler, Simulation sim, ClientSet clients) {
        this.sampler = sampler;
        this.sim = sim;
    }

    // File-based constructor
    public TestBlockchainWorkload(Simulation sim, ClientSet clients) throws Exception {
        this.events = new ArrayList<>();
        this.sim = sim;
        this.clients = clients;
    }

    public void appendTransactions(int numOfTransactions) {
        long currTime = timeEnd;
        for (int i = 0; i < numOfTransactions; i++) {
            currTime += (long) sampler.getNextTransactionArrivalInterval();
            Event event = new Event_ClientMakeTransaction(clients.pickRandomClient(),
                clients.pickRandomClient().getClientId(),
                sampler.getRandomNum(1, 2),  // FIXME
                currTime);
            events.add(event);
        }
        timeEnd = currTime;
    }

    public void appendPennyCheck(int numOfPennyChecks) {
        long currTime = timeEnd;
        for (int i = 0; i < numOfPennyChecks; i++) {
            currTime += (long) sampler.getNextTransactionArrivalInterval();
            Event event = new Event_ClientCheckPennyJar(clients.pickRandomClient(), currTime);
            events.add(event);
        }
        timeEnd = currTime;
    }

    // Append transactions from file
    public void appendTransactionsFromFile() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("./CNSim/resources/test-blockchain/transactions.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    int senderId = Integer.parseInt(parts[0].trim());
                    int recipientId = Integer.parseInt(parts[1].trim());
                    long amount = (long) Double.parseDouble(parts[2].trim());  // FIXME
                    long time = Long.parseLong(parts[3].trim());
                    Client client = clients.pickSpecificClient(senderId);
                    Event event = new Event_ClientMakeTransaction(client, recipientId, amount, time);
                    events.add(event);
                }
            }
        }
    }


    public void appendPennyCheckFromFile() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("./CNSim/resources/pennycheck.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    int clientId = Integer.parseInt(parts[0].trim());
                    long simTime = Long.parseLong(parts[1].trim());
                    Client client = clients.pickSpecificClient(clientId);
                    //events.add(new Event_ClientGetAccount(client, simTime));
                    events.add(new Event_ClientCheckPennyJar(client, simTime));
                    //System.out.println("Penny check for client " + client.getClientId() + " at time " + simTime);
                }
            }
        }
    }
}
