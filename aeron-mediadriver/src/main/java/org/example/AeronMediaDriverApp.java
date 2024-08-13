package org.example;

import io.aeron.CommonContext;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.ShutdownSignalBarrier;
import org.agrona.concurrent.SigInt;
import org.agrona.concurrent.SleepingIdleStrategy;

import java.util.concurrent.ThreadFactory;

public class AeronMediaDriverApp {

    public static void main(String[] args) {
        try {
            System.out.println("Media driver started");

            AgentRunner agentRunner = new AgentRunner(new SleepingIdleStrategy(),
                    Throwable::printStackTrace,
                    null,
                    new MediaDriverHealthReporterAgent());

            AgentRunner.startOnThread(agentRunner);

            ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();

            SigInt.register(() -> {
                barrier.signal();
                agentRunner.close();
            });

            barrier.await();
        } finally {
            System.out.println("Kill signal received");
        }
    }

}