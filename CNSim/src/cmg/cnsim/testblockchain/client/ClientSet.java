package cmg.cnsim.testblockchain.client;

import cmg.cnsim.testblockchain.TestBlockchainClientFactory;

import java.util.ArrayList;

public class ClientSet {
    protected ArrayList<Client> clients;
    protected TestBlockchainClientFactory clientFactory;


    public ClientSet(TestBlockchainClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        clients = new ArrayList<>();
    }


    public void addClients(int num) {
        if (num < 0)
            throw new ArithmeticException("num < 0");
        for (int i = 1; i <= num; i++) {
            try {
                addClient(i);
            } catch (Exception e) {e.printStackTrace();}
        }
    }

    private void addClient(int i) throws Exception {
        Client c = clientFactory.createNewClient(i);
        clients.add(c);
    }

    public ArrayList<Client> getClients() {
        return clients;
    }

    public int getClientSetCount() {
        return clients.size();
    }

    public Client pickRandomClient() {
        return clients.get((int) (Math.random() * clients.size()));
    }

    public Client pickSpecificClient(int index) {
        return clients.get(index - 1);
    }


    public void closeClients() {
    }


}
