
package abortableconsensus.component;

import bebbroadcast.port.BebDeliver;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class Read extends BebDeliver {
    private int id;
    private int ts;

    public Read(Address src, int id, int ts) {
        super(src);
        this.id = id;
        this.ts = ts;
    }

    public int getId() {
        return id;
    }

    public int getTimestamp() {
        return ts;
    }
}

