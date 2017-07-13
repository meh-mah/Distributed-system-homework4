
package abortableconsensus.component;

import se.sics.kompics.Init;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class AbortableConsensusInit extends Init {
    private Address myAddress;
    private int nodes;

    public AbortableConsensusInit(Address myAddress, int nodes) {
        this.myAddress = myAddress;
        this.nodes = nodes;
    }

    public int getNumberOfNodes() {
        return nodes;
    }

    public Address getMyAddress() {
        return myAddress;
    }
}

