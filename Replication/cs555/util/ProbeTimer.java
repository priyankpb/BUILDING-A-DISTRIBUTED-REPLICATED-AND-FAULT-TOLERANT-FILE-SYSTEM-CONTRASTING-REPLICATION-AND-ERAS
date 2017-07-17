package cs555.util;

import cs555.nodes.Controller;
import cs555.transport.TCPConnection;
import cs555.wireformats.ControllerProbe;
import cs555.wireformats.EventFactory;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;

public class ProbeTimer
        extends TimerTask {

    Controller controller;
    private long timer = Protocol.PROBE_TIMER;
    TCPConnection conn;
    EventFactory eventFactory = EventFactory.getInstance();

    public ProbeTimer(Controller controller) {
        this.controller = controller;
    }

    public void run() {

        synchronized (this.controller.timerCache) {
            for (Iterator<Map.Entry<Integer, Long>> it = this.controller.timerCache.entrySet().iterator(); it.hasNext();) {
                Map.Entry<Integer, Long> entrySet = it.next();
                int key = entrySet.getKey();
                long value = entrySet.getValue();
                value -= this.timer;
                this.controller.timerCache.put(key, value);
                if (value <= 0L) {
                    try {
//                        this.controller.timerCache.remove(key);
                        it.remove();
//                        System.out.println("[INFO] Probe sent to node " + key);
                        synchronized (this.controller.timerCache2) {
                            this.controller.timerCache2.put(key, Protocol.PROBE_TIMEOUT2);
                        }

                        this.conn = ((TCPConnection) this.controller.connectionCache.get(Integer.valueOf(key)));
                        ControllerProbe localControllerProbe = (ControllerProbe) this.eventFactory.createEvent(Protocol.CONTROLLER_PROBE);
                        localControllerProbe.setNodeID(key);
                        conn.getSender().sendData(localControllerProbe.getByte());

//                        System.out.println("[INFO] Probe sent to node " + key);
                    } catch (IOException ex) {
                        System.err.printf("[ERROR] Probe", new Object[0]);
                    }
                }
            }
        }
    }
}
