// TODO probably move to different package
package cmg.cnsim.testblockchain.client;

public class Account {
    private final int accountId;
    public AccountState state;
    public long balance;
    public long nonce;

    public Account(int accountId, AccountState initialState, long initialBalance, long initialNonce) {
        this.accountId = accountId;
        this.state = initialState;
        this.balance = initialBalance;
        this.nonce = initialNonce;
    }

    public int getAccountId() {
        return accountId;
    }

    public String printAccount() {
        return("Account ID: " + this.accountId + ", State: " + state + ", Balance: " + balance + ", Nonce: " + nonce);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Account)) {
            return false;
        }
        Account a = (Account) obj;
        return accountId == a.accountId
            && state == a.state
            && balance == a.balance
            && nonce == a.nonce;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(((accountId * 31 + balance) * 31 + nonce) * 31 + state.hashCode());
    }
}
