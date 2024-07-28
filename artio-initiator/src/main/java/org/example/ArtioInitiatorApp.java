package org.example;

import io.aeron.CommonContext;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import org.agrona.concurrent.*;
import uk.co.real_logic.artio.MonitoringAgentFactory;
import uk.co.real_logic.artio.engine.EngineConfiguration;
import uk.co.real_logic.artio.engine.FixEngine;

public class ArtioInitiatorApp {
    public static void main(String[] args) {
        MediaDriver.Context ctx = new MediaDriver
                .Context().threadingMode(ThreadingMode.SHARED);
        MediaDriver mediaDriver = MediaDriver.launchEmbedded(ctx);

        EngineConfiguration enginConfig = new EngineConfiguration()
                .monitoringAgentFactory(MonitoringAgentFactory.none())
                .logInboundMessages(false)
                .logOutboundMessages(false)
                .libraryAeronChannel(CommonContext.IPC_CHANNEL)
                .acceptorfixDictionary(io.github.busy_spin.artio_initiator.codecs.FixDictionaryImpl.class)
                .defaultHeartbeatIntervalInS(15);

        enginConfig.aeronContext()
                .aeronDirectoryName(mediaDriver.aeronDirectoryName());

        ArtioLifecycleHandler lifecycleHandler = new ArtioLifecycleHandler();

        AgentRunner agentRunner = new AgentRunner(new SleepingIdleStrategy(), Throwable::printStackTrace,
                null,
                new CompositeAgent(new LibraryAgent(mediaDriver.aeronDirectoryName(), lifecycleHandler),
                        new ReporterAgent(lifecycleHandler)));

        AgentRunner.startOnThread(agentRunner);

        ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();
        SigInt.register(barrier::signal);

        FixEngine fixEngine = FixEngine.launch(enginConfig);


        barrier.await();
        agentRunner.close();
        fixEngine.close();
        mediaDriver.close();
    }
}