
package abortableconsensus.component;

import id2203.link.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class ReadAck extends Pp2pDeliver {
    private int id;
    private int ts;
    private Object val;
    private int sts;

    public ReadAck(Address src, int id, int ts, Object val, int sts) {
        super(src);
        this.id = id;
        this.ts = ts;
        this.val = val;
        this.sts = sts;
    }

    public int getId() {
        return id;
    }

    public int getSentTS() {
        return sts;
    }

    public int getTS() {
        return ts;
    }

    public Object getVal() {
        return val;
    }
}

