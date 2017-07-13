
package leader.eld.component;

import detectors.messages.HBMsg;
import detectors.messages.HBTimeout;
import id2203.link.pp2p.PerfectPointToPointLink;
import id2203.link.pp2p.Pp2pSend;
import java.util.HashSet;
import java.util.Set;
import leader.eld.port.ELDPort;
import leader.eld.port.Trust;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

/**
 *
 * @author M&M
 */
public class ELDComponent extends ComponentDefinition {

    Positive<PerfectPointToPointLink> pp2pl = requires(PerfectPointToPointLink.class);
    Positive<Timer> timer = requires(Timer.class);
    Negative<ELDPort> eld = provides(ELDPort.class);

    private Address myAddress;
    private Set<Address> II;
    private long period;
    private Address leader;
    private Set<Address> leaderCandids;
    private long delta;

    public ELDComponent() {
        subscribe(hInit, control);
        subscribe(hHBMsg, pp2pl);
        subscribe(hHBTimeout, timer);
    }

    Handler<ELDInit> hInit = new Handler<ELDInit>() {
        @Override
        public void handle(ELDInit e) {
            myAddress = e.getMyAddress();
            II = e.getAllNodes();
            period = e.getDelay();
            delta = e.getDelta();

            leader = selectLeader(II);
            trigger(new Trust(leader), eld);
            HBMsg hb = new HBMsg(myAddress);
            for (Address add : II) {
                trigger(new Pp2pSend(add, hb), pp2pl);
            }
            leaderCandids = new HashSet<>();

            ScheduleTimeout st = new ScheduleTimeout(period);
            st.setTimeoutEvent(new HBTimeout(st));
            trigger(st, timer);
        }
    };
    Handler<HBTimeout> hHBTimeout = new Handler<HBTimeout>() {
        @Override
        public void handle(HBTimeout e) {
            Address leader_new = selectLeader(leaderCandids);
            if (!leader.equals(leader_new) && leader_new != null) {
                period += delta;
                leader = leader_new;
                trigger(new Trust(leader), eld);
            }

            HBMsg hb = new HBMsg(myAddress);
            for (Address add : II) {
                trigger(new Pp2pSend(add, hb), pp2pl);
            }
            leaderCandids.clear();

            ScheduleTimeout st = new ScheduleTimeout(period);
            st.setTimeoutEvent(new HBTimeout(st));
            trigger(st, timer);
        }
    };
    Handler<HBMsg> hHBMsg = new Handler<HBMsg>() {
        @Override
        public void handle(HBMsg e) {
            leaderCandids.add(e.getSource());
        }
    };


    private Address selectLeader(Set<Address> candids) {
        Address selectedLeader = null;
        int r = Integer.MAX_VALUE;
        for (Address c : candids) {
            if (rank(c) < r) {
                selectedLeader = c;
                r = rank(c);
            }
        }
        return selectedLeader;
    }

    private int rank(Address process) {
        return process.getId();
    }
}
