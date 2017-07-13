
package abortableconsensus.component;

import id2203.link.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class WriteAck extends Pp2pDeliver {
    private int id;
    private int wts;

    public WriteAck(Address src, int id, int wts) {
        super(src);
        this.id = id;
        this.wts = wts;
    }

    public int getId() {
        return id;
    }

    public int getWTS() {
        return wts;
    }
}

