
package application;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.*;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;
import uniformconsensus.port.UcDecide;
import uniformconsensus.port.UcPropose;
import uniformconsensus.port.UniformConsensusPort;

/**
 *
 * @author M&M
 */
public final class Application extends ComponentDefinition {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    
    Positive<UniformConsensusPort> ucPort = requires(UniformConsensusPort.class);
    Positive<Timer> timerPort = requires(Timer.class);

    private String[] cmd;
    private long time;
    private int lastCmd;
    private Map<Integer, Object> decidedValues;
    private boolean linger;
    private Set<Integer> underDecisions;
    
    public Application() {
        subscribe(hInit, control);
        subscribe(hStart, control);
        subscribe(hContinue, timerPort);
        subscribe(hUcDecide, ucPort);
    }

    Handler<ApplicationInit> hInit = new Handler<ApplicationInit>() {
        @Override
        public void handle(ApplicationInit e) {
            cmd = e.getCommandScript().split(":");
            lastCmd = -1;
            decidedValues = new TreeMap<>();
            underDecisions = new HashSet<>();
        }
    };
    Handler<Start> hStart = new Handler<Start>() {
        @Override
        public void handle(Start e) {
            doNextCommand();
        }
    };
    Handler<ApplicationContinue> hContinue = new Handler<ApplicationContinue>() {
        @Override
        public void handle(ApplicationContinue e) {
            doNextCommand();
        }
    };
    Handler<UcDecide> hUcDecide = new Handler<UcDecide>() {
        @Override
        public void handle(UcDecide event) {
            logger.info("::Decision received for id={}, DECIDED VALUE={}::", event.getId(), event.getDecidedValue());

            decidedValues.put(event.getId(), event.getDecidedValue());
            underDecisions.remove(event.getId());
            if (linger && underDecisions.isEmpty()) {
                linger = false;
                doSleep(time);
            }
        }
    };


    private void doNextCommand() {
        lastCmd++;

        if (lastCmd > cmd.length) {
            return;
        }
        if (lastCmd == cmd.length) {
            logger.info("DONE ALL OPERATIONS");
            Thread appT = new Thread("AppThread") {
                @Override
                public void run() {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(System.in));
                    while (true) {
                        try {
                            String l = br.readLine();
                            doCommand(l);
                        } catch (Throwable e) {
                        }
                    }
                }
            };
            appT.start();
            return;
        }
        String command = cmd[lastCmd];
        doCommand(command);
    }

    private void doCommand(String cmd) {
        if (cmd.startsWith("S")) {
            doSleep(Integer.parseInt(cmd.substring(1)));
        } else if (cmd.startsWith("X")) {
            doShutdown();
        } else if (cmd.equals("help")) {
            doHelp();
            doNextCommand();
        } else if (cmd.equals("W")) {
            printdecisions();
            doNextCommand();
        } else if (cmd.startsWith("P")) {
            int x = cmd.indexOf("-");
            int i = Integer.parseInt(cmd.substring(1, x));
            int j = Integer.parseInt(cmd.substring(x + 1));
            ucPropose(i, j);
            doNextCommand();
        } else if (cmd.startsWith("D")) {
            linger(Integer.parseInt(cmd.substring(1)));
        } else {
            logger.info("Bad command: '{}'. Try 'help'", cmd);
            doNextCommand();
        }
    }

    private void doHelp() {
        logger.info("Available commands: S<n>, help, X, P<i>-<j>, D<k>, W");
        logger.info("Sn: sleeps 'n' milliseconds before the next command");
        logger.info("help: shows this help message");
        logger.info("X: terminates this process");
        logger.info("Pi-j: proposes the value 'j' for a consensus instance identified by 'i'");
        logger.info("W: sort all the decisions received thus far according to Paxos instance identifier and print them out");
        logger.info("Dk: waits untill getting decisions to all ongoing proposals made by the this node, then wait 'k' milliseconds, then process any further command");
        
        
    }

    private void doSleep(long d) {
        logger.info("Sleeping {} milliseconds...", d);

        ScheduleTimeout sto = new ScheduleTimeout(d);
        sto.setTimeoutEvent(new ApplicationContinue(sto));
        trigger(sto, timerPort);
    }

    private void doShutdown() {
        System.out.println("2DIE");
        System.out.close();
        System.err.close();
        Kompics.shutdown();
    }

    private void printdecisions() {
        StringBuilder z = new StringBuilder("received Decisions so far:");
        for (Entry<Integer, Object> ent : decidedValues.entrySet()) {
            z.append("\n   id=").append(ent.getKey()).append(" decided value=").append(ent.getValue());
        }
        logger.info(z.toString());
    }

    private void ucPropose(int id, int v) {
        logger.info("value={} proposed  for id={}...", v, id);

        underDecisions.add(id);
        trigger(new UcPropose(id, v), ucPort);
    }

    private void linger(long t) {
        logger.info("Waiting  until get decisions to all ongoing proposals...");

        if (underDecisions.isEmpty()) {
            doSleep(t);
        } else {
            linger = true;
            time = t;
        }
    }
}

