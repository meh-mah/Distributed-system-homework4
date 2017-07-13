
package abortableconsensus.port;

import se.sics.kompics.Event;

/**
 *
 * @author M&M
 */
public class AcPropose extends Event {
    private Object propose;
    private int id;

    public AcPropose(int id, Object propose) {
        this.propose = propose;
        this.id = id;
        
    }
    public Object getProposal() {
        return propose;
    }
    
    public int getId() {
        return id;
    }

    
}

