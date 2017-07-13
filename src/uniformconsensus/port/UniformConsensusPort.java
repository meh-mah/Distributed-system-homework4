
package uniformconsensus.port;

import se.sics.kompics.PortType;

/**
 *
 * @author M&M
 */
public class UniformConsensusPort extends PortType {
    {
        request(UcPropose.class);
        indication(UcDecide.class);
        
    }
}
