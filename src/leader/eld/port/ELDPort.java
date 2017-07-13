
package leader.eld.port;

import se.sics.kompics.PortType;

/**
 *
 * @author M&M
 */
public class ELDPort extends PortType {
    {
        indication(Trust.class);
    }
}
