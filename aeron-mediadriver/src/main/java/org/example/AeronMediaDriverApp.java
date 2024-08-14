package org.example;

import io.aeron.CommonContext;
import io.aeron.driver.MediaDriver;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.ShutdownSignalBarrier;
import org.agrona.concurrent.SigInt;
import org.agrona.concurrent.SleepingIdleStrategy;

import java.util.concurrent.ThreadFactory;

public class AeronMediaDriverApp {

    public static void main(String[] args) {
        try {
            System.out.println("Media driver started");

            MediaDriver.Context context = new MediaDriver.Context()
                    .aeronDirectoryName(CommonContext.getAeronDirectoryName() + "-xyz");

            MediaDriver mediaDriver = MediaDriver.launch(context);

            AgentRunner agentRunner = new AgentRunner(new SleepingIdleStrategy(),
                    Throwable::printStackTrace,
                    null,
                    new MediaDriverHealthReporterAgent());

            AgentRunner.startOnThread(agentRunner, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    return thread;
                }
            });

            ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();

            SigInt.register(() -> {
                barrier.signal();
                agentRunner.close();
                mediaDriver.close();
            });

            barrier.await();
        } finally {
            System.out.println("Kill signal received");
        }
    }

}