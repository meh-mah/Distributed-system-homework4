
package uniformconsensus.port;

import se.sics.kompics.Event;

/**
 *
 * @author M&M
 */
public class UcDecide extends Event {
    private int id;
    private Object decided;

    public UcDecide(int id, Object decided) {
        this.id = id;
        this.decided = decided;
    }

    public int getId() {
        return id;
    }

    public Object getDecidedValue() {
        return decided;
    }
}
