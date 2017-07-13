
package abortableconsensus.component;

import id2203.link.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class NACK extends Pp2pDeliver {
    private int id;

    public NACK(Address src, int id) {
        super(src);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
