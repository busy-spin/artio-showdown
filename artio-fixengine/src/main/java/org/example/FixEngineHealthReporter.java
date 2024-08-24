package org.example;

import org.agrona.concurrent.Agent;
import org.agrona.concurrent.SystemEpochClock;
import uk.co.real_logic.artio.engine.FixEngine;

public class FixEngineHealthReporter implements Agent {

    private FixEngine fixEngine;

    private long lastReportInterval = SystemEpochClock.INSTANCE.time();

    @Override
    public void onStart() {
        Agent.super.onStart();
    }

    @Override
    public int doWork() throws Exception {
        long timeNow = SystemEpochClock.INSTANCE.time();
        if (timeNow > lastReportInterval + 5_000) {
            lastReportInterval = timeNow;
            System.out.println("Reporting sessions >>>");
            fixEngine.allSessions().forEach(session -> {
                System.out.println(session);
            });
        }
        return 0;
    }

    @Override
    public void onClose() {
        Agent.super.onClose();
    }

    @Override
    public String roleName() {
        return "health-reporter";
    }

    public void setFixEngine(FixEngine fixEngine) {
        this.fixEngine = fixEngine;
    }
}
