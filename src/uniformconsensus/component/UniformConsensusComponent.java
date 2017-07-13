
package uniformconsensus.component;

import abortableconsensus.port.ACPort;
import abortableconsensus.port.AcReturn;
import abortableconsensus.port.AcPropose;
import bebbroadcast.port.BebBroadcast;
import bebbroadcast.port.BebPort;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import leader.eld.port.ELDPort;
import leader.eld.port.Trust;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;
import uniformconsensus.port.UcDecide;
import uniformconsensus.port.UcPropose;
import uniformconsensus.port.UniformConsensusPort;

/**
 *
 * @author M&M
 */
public class UniformConsensusComponent extends ComponentDefinition {

    private static final Logger logger = LoggerFactory.getLogger(UniformConsensusComponent.class);
    
    Negative<UniformConsensusPort> uc = provides(UniformConsensusPort.class);
    Positive<BebPort> beb = requires(BebPort.class);
    Positive<ACPort> ac = requires(ACPort.class);
    Positive<ELDPort> eld = requires(ELDPort.class);

    private Map<Integer, Object> proposal;
    private Address myAddress;
    private Map<Integer, Boolean> proposed;
    private Set<Integer> seenId;
    private boolean leader;
    private Map<Integer, Boolean> decided;

    public UniformConsensusComponent() {
        subscribe(hInit, control);
        subscribe(hTrust, eld);
        subscribe(hUcPropose, uc);
        subscribe(hAcReturn, ac);
        subscribe(decidedMessageHandler, beb);
    }

    Handler<UniformConsensusInit> hInit = new Handler<UniformConsensusInit>() {
        @Override
        public void handle(UniformConsensusInit e) {
            leader = false;
            
            myAddress = e.getMyAddress();

            seenId = new TreeSet<>();
            
            proposal = new HashMap<>();
            decided = new HashMap<>();
            proposed = new HashMap<>();
            
        }
    };
    
        private void initInstance(int id) {
        if (!seenId.contains(id)) {
            proposal.put(id, null);
            proposed.put(id, false);
            decided.put(id, false);
            seenId.add(id);
        }
    }
        
    Handler<Trust> hTrust = new Handler<Trust>() {
        @Override
        public void handle(Trust e) {
            Address newLeader = e.getTrustedLeader();
            logger.debug("leader={}", newLeader);

            if (newLeader.equals(myAddress)) {
                leader = true;
                for (int id : seenId) {
                    tryPropose(id);
                    logger.debug("try propose on trust event id={}", id);
                }
            } else {
                leader = false;
            }
        }
    };
    Handler<UcPropose> hUcPropose = new Handler<UcPropose>() {
        @Override
        public void handle(UcPropose e) {
            int id = e.getId();
            Object value = e.getProposedValue();

            initInstance(id);
            proposal.put(id, value);
            tryPropose(id);
//            logger.debug("try propose on ucpropose event id={}", id);
        }
    };
    
     private void tryPropose(int id) {
        if (leader && !proposed.get(id) && proposal.get(id) != null) {
            
            proposed.put(id, true);
            trigger(new AcPropose(id, proposal.get(id)), ac);
            logger.debug("try propose v={}", proposal.get(id));
        }
    }
     
    Handler<AcReturn> hAcReturn = new Handler<AcReturn>() {
        @Override
        public void handle(AcReturn e) {
            int id = e.getId();
            Object r = e.getResult();

            if (r != null) {
                trigger(new BebBroadcast(new Decided(myAddress, id, r)), beb);
                logger.debug("PUC beb decide id={}  result={}", id, r);
            } else {
                proposed.put(id, false);
                tryPropose(id);
            }
        }
    };
    Handler<Decided> decidedMessageHandler = new Handler<Decided>() {
        @Override
        public void handle(Decided e) {
            int id = e.getId();
            Object value = e.getDecidedValue();

            initInstance(id);
            if (!decided.get(id)) {
                decided.put(id, true);
                trigger(new UcDecide(id, value), uc);
//                seenIds.remove(id);
//                trigger (new Acremove(id, v, 1), ac);
//                logger.debug("remove id={}", id);
            }
        }
    };



   
}
