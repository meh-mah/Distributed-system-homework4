
package abortableconsensus.component;

import bebbroadcast.port.BebDeliver;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class Write extends BebDeliver {
    private int id;
    private int ts;
    private Object v;

    public Write(Address src, int id, int ts, Object v) {
        super(src);
        this.id = id;
        this.ts = ts;
        this.v = v;
    }

    public int getId() {
        return id;
    }

    public int getTS() {
        return ts;
    }

    public Object getValue() {
        return v;
    }
}
