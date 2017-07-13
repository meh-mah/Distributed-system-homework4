
package leader.eld.component;

import java.util.Set;
import se.sics.kompics.Init;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class ELDInit extends Init {
    private Set<Address> II;
    private Address myAddress;
    private long Delay;
    private long delta;

    public ELDInit(Set<Address> II, Address myAddress, long delay, long delta) {
        this.II = II;
        this.myAddress = myAddress;
        this.Delay = delay;
        this.delta = delta;
    }

    public Set<Address> getAllNodes() {
        return II;
    }

    public long getDelay() {
        return Delay;
    }

    public Address getMyAddress() {
        return myAddress;
    }

    public long getDelta() {
        return delta;
    }
}

