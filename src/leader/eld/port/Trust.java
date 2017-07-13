
package leader.eld.port;

import se.sics.kompics.Event;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class Trust extends Event {
    private Address l;

    public Trust(Address trustedLeader) {
        this.l = trustedLeader;
    }

    public Address getTrustedLeader() {
        return l;
    }
}

