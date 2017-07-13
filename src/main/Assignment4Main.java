/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import abortableconsensus.component.AbortableConsensusComponent;
import abortableconsensus.component.AbortableConsensusInit;
import abortableconsensus.port.ACPort;
import application.Application;
import application.ApplicationInit;
import bebbroadcast.component.BebInit;
import bebbroadcast.component.BestEffortBroadcastComponent;
import bebbroadcast.port.BebPort;
import id2203.link.pp2p.PerfectPointToPointLink;
import id2203.link.pp2p.delay.DelayLink;
import id2203.link.pp2p.delay.DelayLinkInit;
import java.util.Set;
import leader.eld.component.ELDComponent;
import leader.eld.component.ELDInit;
import leader.eld.port.ELDPort;
import org.apache.log4j.PropertyConfigurator;
import se.sics.kompics.*;
import se.sics.kompics.address.Address;
import se.sics.kompics.launch.Topology;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.mina.MinaNetwork;
import se.sics.kompics.network.mina.MinaNetworkInit;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;
import uniformconsensus.component.UniformConsensusComponent;
import uniformconsensus.component.UniformConsensusInit;
import uniformconsensus.port.UniformConsensusPort;

/**
 *
 * @author M&M
 */
public class Assignment4Main extends ComponentDefinition {
    static {
        PropertyConfigurator.configureAndWatch("log4j.properties");
    }
    private static final long TIME_DELAY = 400;
    private static final long DELTA = 10;
    private static int selfId;
    private static String commandScript;
    Topology topology = Topology.load(System.getProperty("topology"), selfId);

    public static void main(String[] args) {
        selfId = Integer.parseInt(args[0]);
        commandScript = args[1];

        Kompics.createAndStart(Assignment4Main.class);
    }

    public Assignment4Main() {
        // create components
        Component time = create(JavaTimer.class);
        Component network = create(MinaNetwork.class);
        Component pp2p = create(DelayLink.class);
        Component beb = create(BestEffortBroadcastComponent.class);
        Component eld = create(ELDComponent.class);
        Component ac = create(AbortableConsensusComponent.class);
        Component uc = create(UniformConsensusComponent.class);
        Component app = create(Application.class);

        // handle possible faults in the components
        subscribe(faultHandler, time.control());
        subscribe(faultHandler, network.control());
        subscribe(faultHandler, pp2p.control());
        subscribe(faultHandler, beb.control());
        subscribe(faultHandler, eld.control());
        subscribe(faultHandler, ac.control());
        subscribe(faultHandler, uc.control());
        subscribe(faultHandler, app.control());

        // initialize the components
        Address self = topology.getSelfAddress();
        Set<Address> neighborSet = topology.getNeighbors(self);
        Set<Address> all = topology.getAllAddresses();

        trigger(new MinaNetworkInit(self, 5), network.control());
        trigger(new DelayLinkInit(topology), pp2p.control());
        trigger(new BebInit(all, self), beb.control());
        trigger(new ELDInit(all, self, TIME_DELAY, DELTA), eld.control());
        trigger(new AbortableConsensusInit(self, all.size()), ac.control());
        trigger(new UniformConsensusInit(self), uc.control());
        trigger(new ApplicationInit(commandScript), app.control());

        // connect the components
        connect(app.required(UniformConsensusPort.class), uc.provided(UniformConsensusPort.class));
        connect(app.required(Timer.class), time.provided(Timer.class));

        connect(uc.required(BebPort.class), beb.provided(BebPort.class));
        connect(uc.required(ACPort.class), ac.provided(ACPort.class));
        connect(uc.required(ELDPort.class), eld.provided(ELDPort.class));

        connect(ac.required(BebPort.class), beb.provided(BebPort.class));
        connect(ac.required(PerfectPointToPointLink.class), pp2p.provided(PerfectPointToPointLink.class));

        connect(beb.required(PerfectPointToPointLink.class), pp2p.provided(PerfectPointToPointLink.class));

        connect(eld.required(PerfectPointToPointLink.class), pp2p.provided(PerfectPointToPointLink.class));
        connect(eld.required(Timer.class), time.provided(Timer.class));

        connect(pp2p.required(Network.class), network.provided(Network.class));
        connect(pp2p.required(Timer.class), time.provided(Timer.class));
    }
    //handlers
    Handler<Fault> faultHandler = new Handler<Fault>() {
        @Override
        public void handle(Fault fault) {
            fault.getFault().printStackTrace(System.err);
        }
    };
}
