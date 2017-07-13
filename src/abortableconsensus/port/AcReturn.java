
package abortableconsensus.port;

import se.sics.kompics.Event;

/**
 *
 * @author M&M
 */

public class AcReturn extends Event {
    private Object result;
    private int id;
    

    public AcReturn(int id, Object result) {
        this.result = result;
        this.id = id;
        
    }

    public Object getResult() {
        return result;
    }
    
    public int getId() {
        return id;
    }

    
}

