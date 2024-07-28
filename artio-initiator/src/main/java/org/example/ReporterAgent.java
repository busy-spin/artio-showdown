package org.example;

import org.agrona.concurrent.Agent;
import org.agrona.concurrent.SystemEpochClock;

public class ReporterAgent implements Agent {


    private final int reportingInterval = 1_000;

    private long lastReportTime = SystemEpochClock.INSTANCE.time();

    private final ArtioLifecycleHandler lifecycleHandler;

    public ReporterAgent(ArtioLifecycleHandler lifecycleHandler) {

        this.lifecycleHandler = lifecycleHandler;
    }


    @Override
    public void onStart() {
        Agent.super.onStart();
    }

    @Override
    public int doWork() throws Exception {
        long timeNow = SystemEpochClock.INSTANCE.time();
        if (timeNow > lastReportTime + reportingInterval) {
            lastReportTime = timeNow;
            System.out.println("Total count     " + lifecycleHandler.getHistogram().getTotalCount());
            System.out.println("99   Percentile " + lifecycleHandler.getHistogram().getValueAtPercentile(99));
            System.out.println("99.9 Percentile " + lifecycleHandler.getHistogram().getValueAtPercentile(99.9));

            lifecycleHandler.getHistogram().reset();
        }
        return 0;
    }

    @Override
    public void onClose() {
        Agent.super.onClose();
    }

    @Override
    public String roleName() {
        return "reporter";
    }
}
