package cmg.cnsim.testblockchain;

import cmg.cnsim.testblockchain.client.Account;
import cmg.cnsim.testblockchain.client.tx.Tx;
import cmg.cnsim.engine.IStructure;
import cmg.cnsim.engine.Simulation;
import cmg.cnsim.engine.transaction.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The TestBlockchain's blocklattice structure
 *
 */
public class BlockLattice implements IStructure {


    Map<ArrayList<Tx>, Account> accounts = new HashMap<>();

    public void addToStructure(Transaction tx) {


    }

    /**
     * Print structure for direct presentation. Returns a list of account details and their transactions.
     */
    @Override
    public String[] printStructure() {
        List<String> result = new ArrayList<>();
        result.add("AccountID,OutgoingTransactionIDs,IncomingTransactionIDs");

        for (Account account : accounts.values()) {
            result.add(account.toString());
        }

        return result.toArray(new String[0]);
    }

    // Additional methods can be added here to support other functionalities.
}
