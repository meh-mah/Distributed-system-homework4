
package uniformconsensus.component;

import bebbroadcast.port.BebDeliver;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class Decided extends BebDeliver {
    private int id;
    private Object decided;

    public Decided(Address source, int id, Object decidedV) {
        super(source);
        this.id = id;
        this.decided = decidedV;
    }

    public int getId() {
        return id;
    }

    public Object getDecidedValue() {
        return decided;
    }
}
