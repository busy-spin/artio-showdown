package org.example;

import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.ShutdownSignalBarrier;
import org.agrona.concurrent.SigInt;
import org.agrona.concurrent.SleepingIdleStrategy;

import java.util.concurrent.ThreadFactory;

public class Main {

    public static void main(String[] args) {
        AgentRunner agentRunner = new AgentRunner(new SleepingIdleStrategy(),
                throwable -> {},
                null,
                new MediaDriverHealthReporterAgent());


        AgentRunner.startOnThread(agentRunner, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread();
                thread.setDaemon(false);
                return thread;
            }
        });

        ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();

        SigInt.register(() -> {
            barrier.signal();
            agentRunner.close();
        });

        barrier.await();
    }
}
