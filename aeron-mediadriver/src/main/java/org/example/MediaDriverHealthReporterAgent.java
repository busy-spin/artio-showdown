package org.example;

import org.agrona.concurrent.Agent;
import org.agrona.concurrent.SystemEpochClock;

public class MediaDriverHealthReporterAgent implements Agent {

    private long lastReportTime = SystemEpochClock.INSTANCE.time();

    @Override
    public void onStart() {
        System.out.println("Created "  + Thread.currentThread().getName());
        Agent.super.onStart();
    }

    @Override
    public int doWork() throws Exception {
        long timeNow = SystemEpochClock.INSTANCE.time();
        if (lastReportTime + 1_000 < timeNow) {
            lastReportTime = timeNow;
            System.out.println("Media driver active !!!");
        }
        return 0;
    }

    @Override
    public void onClose() {
        System.out.println("Closed " + Thread.currentThread().getName() );
        Agent.super.onClose();
    }

    @Override
    public String roleName() {
        return "md-health-agent";
    }
}
