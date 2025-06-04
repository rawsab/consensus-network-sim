package cmg.cnsim.testblockchain.client;

import java.util.Set;

// FIXME maybe remove class?
public class PennyJar {
    public Set<Penny> pennies;

    public PennyJar(Set<Penny> pennies) {
        this.pennies = pennies;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PennyJar)) {
            return false;
        }
        PennyJar pj = (PennyJar) obj;
        return pennies.equals(pj.pennies);
    }

    @Override
    public int hashCode() {
        return pennies.hashCode();
    }

    public String printPennyJar() {
        String result = "[";
        for (Penny penny : pennies) {
            result += penny.printPenny() + ", ";
        }
        result += "]";
        return result;
    }
}
