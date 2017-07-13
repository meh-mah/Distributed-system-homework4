
package uniformconsensus.component;

import se.sics.kompics.Init;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class UniformConsensusInit extends Init {
    private Address myAddress;

    public UniformConsensusInit(Address myAddress) {
        this.myAddress = myAddress;
    }

    public Address getMyAddress() {
        return myAddress;
    }
}

