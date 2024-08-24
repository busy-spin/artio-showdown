package org.example;

import io.aeron.CommonContext;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.ShutdownSignalBarrier;
import org.agrona.concurrent.SigInt;
import org.agrona.concurrent.SleepingIdleStrategy;
import uk.co.real_logic.artio.MonitoringAgentFactory;
import uk.co.real_logic.artio.engine.EngineConfiguration;
import uk.co.real_logic.artio.engine.FixEngine;
import uk.co.real_logic.artio.engine.LowResourceEngineScheduler;

import java.util.concurrent.ThreadFactory;

public class FixEngineApp {
    public static void main(String[] args) {
        EngineConfiguration configuration = new EngineConfiguration().logOutboundMessages(false)
                .logInboundMessages(false)
                .monitoringAgentFactory(MonitoringAgentFactory.none())
                .libraryAeronChannel(CommonContext.IPC_CHANNEL)
                .scheduler(new LowResourceEngineScheduler());

        configuration.aeronContext().aeronDirectoryName(CommonContext.getAeronDirectoryName() + "-xyz");

        FixEngine fixEngine = FixEngine.launch(configuration);

        FixEngineHealthReporter fixEngineHealthReporter = new FixEngineHealthReporter();
        fixEngineHealthReporter.setFixEngine(fixEngine);
        AgentRunner agentRunner = new AgentRunner(new SleepingIdleStrategy(), throwable -> {
        }, null, fixEngineHealthReporter);

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
            System.out.println("Shutdown signal received");
            barrier.signal();
        });

        barrier.await();
        System.out.println("Closing engine");
        fixEngine.close();
    }
}