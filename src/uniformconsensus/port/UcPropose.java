
package uniformconsensus.port;

import se.sics.kompics.Event;

/**
 *
 * @author M&M
 */
public class UcPropose extends Event {
    private int id;
    private Object proposal;

    public UcPropose(int id, Object propose) {
        this.id = id;
        this.proposal = propose;
    }

    public int getId() {
        return id;
    }

    public Object getProposedValue() {
        return proposal;
    }
}

