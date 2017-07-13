
package abortableconsensus.component;

import abortableconsensus.port.ACPort;
import abortableconsensus.port.AcPropose;
import abortableconsensus.port.AcReturn;
import bebbroadcast.port.BebBroadcast;
import bebbroadcast.port.BebPort;
import id2203.link.pp2p.PerfectPointToPointLink;
import id2203.link.pp2p.Pp2pSend;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;

/**
 *
 * @author M&M
 */
public class AbortableConsensusComponent extends ComponentDefinition {

    private static final Logger logger = LoggerFactory.getLogger(AbortableConsensusComponent.class);
    
    Positive<BebPort> beb = requires(BebPort.class);
    Positive<PerfectPointToPointLink> pp2pl = requires(PerfectPointToPointLink.class);
    Negative<ACPort> ac = provides(ACPort.class);
    
    private int majority;
    private Address myAddress;
    private int numberOfNodes;
    private Set<Integer> seenIds;
    private Map<Integer, Object> tempV;
    private Map<Integer, Object> value;
    private Map<Integer, Integer> writeAcks;
    private Map<Integer, Integer> readTS;
    private Map<Integer, Integer> writeTS;
    private Map<Integer, Integer> timestamp;
    private Map<Integer, Set<TimestampValuePair>> readSet;

    public AbortableConsensusComponent() {
        subscribe(initHandler, control);
        subscribe(hAcPropose, ac);
        subscribe(hRead, beb);
        subscribe(hWrite, beb);
        subscribe(hNACK, pp2pl);
        subscribe(hReadAck, pp2pl);
        subscribe(hWriteAck, pp2pl);
    }

    Handler<AbortableConsensusInit> initHandler = new Handler<AbortableConsensusInit>() {
        @Override
        public void handle(AbortableConsensusInit event) {
            myAddress = event.getMyAddress();
            numberOfNodes = event.getNumberOfNodes();
            majority = numberOfNodes / 2 + 1;
            seenIds = new TreeSet<>();
            tempV = new HashMap<>();
            value = new HashMap<>();
            writeAcks = new HashMap<>();
            readTS = new HashMap<>();
            writeTS = new HashMap<>();
            timestamp = new HashMap<>();
            readSet = new HashMap<>();
            
        }
    };
    
    private void initInstance(int id) {
        if (!seenIds.contains(id)) {
            tempV.put(id, null);
            value.put(id, null);
            writeAcks.put(id, 0);
            readTS.put(id, 0);
            writeTS.put(id, 0);
            timestamp.put(id, rank(myAddress));
            readSet.put(id, new HashSet<TimestampValuePair>());
            seenIds.add(id);
        }
    }
    
    Handler<AcPropose> hAcPropose = new Handler<AcPropose>() {
        @Override
        public void handle(AcPropose e) {
            int id = e.getId();
            Object v = e.getProposal();

            initInstance(id);
            timestamp.put(id, timestamp.get(id) + numberOfNodes);
            tempV.put(id, v);
            trigger(new BebBroadcast(new Read(myAddress, id, timestamp.get(id))), beb);
        }
    };
    Handler<Read> hRead = new Handler<Read>() {
        @Override
        public void handle(Read e) {
            Address src = e.getSource();
            int id = e.getId();
            int timestamp = e.getTimestamp();
            logger.debug("read message recieved from node {} ts={}", src, timestamp);

            initInstance(id);
            if (readTS.get(id) >= timestamp || writeTS.get(id) >= timestamp) {
                trigger(new Pp2pSend(src, new NACK(myAddress, id)), pp2pl);
                logger.debug("sending nack to {}, ts={}", src, timestamp);
            } else {
                readTS.put(id, timestamp);
                trigger(new Pp2pSend(src, new ReadAck(myAddress, id, writeTS.get(id), value.get(id), timestamp)), pp2pl);
                logger.debug("sending ack to {} wts={} val={}", new Object[]{src, writeTS.get(id), value.get(id)});
            }
        }
    };
    Handler<NACK> hNACK = new Handler<NACK>() {
        @Override
        public void handle(NACK e) {
            int id = e.getId();

            readSet.get(id).clear();
            writeAcks.put(id, 0);
            trigger(new AcReturn(id, null), ac);
        }
    };
    Handler<ReadAck> hReadAck = new Handler<ReadAck>() {
        @Override
        public void handle(ReadAck e) {
            int id = e.getId();
            int tstamp = e.getTS();
            Object val = e.getVal();
            int sts = e.getSentTS();
            Address source=e.getSource();
            logger.debug("read ack recieved ts= {} value={}", tstamp, val);

            if (sts == timestamp.get(id)) {
                readSet.get(id).add(new TimestampValuePair(tstamp, val));
                if (readSet.get(id).size() == majority) {
                    TimestampValuePair valueTSPair = highest(readSet.get(id));
                    if (valueTSPair.getValue() != null) {
                        tempV.put(id, valueTSPair.getValue());
                    }
                    trigger(new BebBroadcast(new Write(myAddress, id, timestamp.get(id), tempV.get(id))), beb);
                    logger.debug("sending write to ts={} val is {}", timestamp.get(id),tempV.get(id));
                }
            }
        }
    };
    Handler<Write> hWrite = new Handler<Write>() {
        @Override
        public void handle(Write e) {
            Address src = e.getSource();
            int id = e.getId();
            int timestamp = e.getTS();
            Object val = e.getValue();

            initInstance(id);
            if (readTS.get(id) > timestamp || writeTS.get(id) > timestamp) {
                trigger(new Pp2pSend(src, new NACK(myAddress, id)), pp2pl);
                logger.debug("sending write nack to {} ts={} wts{} rts{} val was{}", new Object[]{src, timestamp,writeTS.get(id), readTS.get(id), val});
            } else {
                value.put(id, val);
                writeTS.put(id, timestamp);
                trigger(new Pp2pSend(src, new WriteAck(myAddress, id, timestamp)), pp2pl);
                logger.debug("sending write ack to {} ts={} wts{} rts{} val was{}", new Object[]{src, timestamp,writeTS.get(id), readTS.get(id), val});
            }
        }
    };
    Handler<WriteAck> hWriteAck = new Handler<WriteAck>() {
        @Override
        public void handle(WriteAck e) {
            int id = e.getId();
            int writets = e.getWTS();

            if (writets == timestamp.get(id)) {
                writeAcks.put(id, writeAcks.get(id) + 1);
                if (writeAcks.get(id) == majority) {
                    readSet.get(id).clear();
                    writeAcks.put(id, 0);
                    trigger(new AcReturn(id, tempV.get(id)), ac);
                }
            }
        }
    };
//    Handler<Acremove> kir = new Handler<Acremove>() {
//        @Override
//        public void handle(Acremove event) {
//            int id = event.getId();
//            
//            seenIds.remove(id);
//        }
//    };

    

    private int rank(Address myAdd) {
        return myAdd.getId();
    }

    private TimestampValuePair highest(Set<TimestampValuePair> pairs) {
        int timeS;
        timeS = Integer.MIN_VALUE;
        TimestampValuePair highest_tsvp = null;
        for (TimestampValuePair tsvp : pairs) {
            if (tsvp.getTstamp() > timeS) {
                timeS = tsvp.getTstamp();
                highest_tsvp = tsvp;
            }
        }
        return highest_tsvp;
    }

    private class TimestampValuePair {
        private int ts;
        private Object v;

        public TimestampValuePair(int ts, Object v) {
            this.ts = ts;
            this.v = v;
        }

        public int getTstamp() {
            return ts;
        }

        public Object getValue() {
            return v;
        }
    }
}

