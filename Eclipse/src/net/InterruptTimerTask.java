package net;

import java.util.TimerTask;

public class InterruptTimerTask extends TimerTask {

    private Thread t;

    public InterruptTimerTask(Thread t) {
        this.t = t;
    }

    @Override
    public void run() {
        t.interrupt();
    }

}
