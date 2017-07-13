
package abortableconsensus.port;

import se.sics.kompics.PortType;

/**
 *
 * @author M&M
 */
public class ACPort extends PortType {
    {
        request(AcPropose.class);
        indication(AcReturn.class);
        
    }
}
