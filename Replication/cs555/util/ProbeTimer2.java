package cs555.util;

import cs555.nodes.Controller;
import cs555.transport.TCPConnection;
import cs555.wireformats.EventFactory;
import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;

public class ProbeTimer2 extends TimerTask {

    Controller controller;
    private long timer = Protocol.PROBE_TIMER;
    TCPConnection conn;
    EventFactory eventFactory = EventFactory.getInstance();

    public ProbeTimer2(Controller controller) {
        this.controller = controller;
    }

    public void run() {
        String keys = "";
        synchronized (this.controller.timerCache2) {
            for (Iterator<Map.Entry<Integer, Long>> it = this.controller.timerCache2.entrySet().iterator(); it.hasNext();) {
                Map.Entry<Integer, Long> entrySet = it.next();
                int key = entrySet.getKey();
                long value = entrySet.getValue();
                value -= this.timer;
                this.controller.timerCache2.put(key, value);
                if (value <= 0L) {
                    //remove node 
//                    System.out.println("--failure detected--");
                    it.remove();
                    keys = keys + key + ",";

                }
            }
        }
        if (!keys.equals("")) {
            String[] s = keys.split(",");
            for (String s1 : s) {
                int k = Integer.parseInt(s1);
                this.controller.removenode(k);
            }
        }
        keys = "";
    }
}
