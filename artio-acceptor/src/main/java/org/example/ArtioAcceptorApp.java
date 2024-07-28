package org.example;

import io.aeron.CommonContext;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import org.agrona.concurrent.*;
import uk.co.real_logic.artio.MonitoringAgentFactory;
import uk.co.real_logic.artio.engine.EngineConfiguration;
import uk.co.real_logic.artio.engine.FixEngine;
import uk.co.real_logic.artio.validation.AuthenticationStrategy;
import uk.co.real_logic.artio.validation.MessageValidationStrategy;

import java.util.Collections;

public class ArtioAcceptorApp {

    public static final String ACCEPTOR_COMP_ID = "EXCHANGE";
    public static final String INITIATOR_COMP_ID = "TAKER_FIRM";

    public static void main(String[] args) {

        int outboundReplayStream = 13;
        int archiveReplayStream = 14;
        int reproductionLogStream = 16;
        int reproductionReplayStream = 17;
        int inboundLibraryStream = 11;
        int outboundLibraryStream = 12;
        int inboundAdminStream = 121;
        int outboundAdminStream = 122;

        final MessageValidationStrategy validationStrategy = MessageValidationStrategy.targetCompId(ACCEPTOR_COMP_ID)
                .and(MessageValidationStrategy.senderCompId(Collections.singletonList(INITIATOR_COMP_ID)));

        final AuthenticationStrategy authenticationStrategy = AuthenticationStrategy.of(validationStrategy);

        MediaDriver.Context ctx = new MediaDriver
                .Context().threadingMode(ThreadingMode.SHARED);
        MediaDriver mediaDriver = MediaDriver.launchEmbedded(ctx);

        EngineConfiguration enginConfig = new EngineConfiguration()
                .monitoringAgentFactory(MonitoringAgentFactory.none())
                .logInboundMessages(false)
                .logOutboundMessages(false)
                .libraryAeronChannel(CommonContext.IPC_CHANNEL)
                .acceptorfixDictionary(io.github.busy_spin.artio_acceptor.codecs.FixDictionaryImpl.class)
                .authenticationStrategy(authenticationStrategy)
                .archiveReplayStream(archiveReplayStream)
                .outboundReplayStream(outboundReplayStream)
                .reproductionLogStream(reproductionLogStream)
                .reproductionReplayStream(reproductionReplayStream)
                .outboundLibraryStream(outboundLibraryStream)
                .inboundLibraryStream(inboundLibraryStream)
                .inboundAdminStream(inboundAdminStream)
                .outboundAdminStream(outboundAdminStream)
                .bindTo("localhost", 9880);

        enginConfig.aeronContext()
                .aeronDirectoryName(mediaDriver.aeronDirectoryName());

        ArtioLifecycleHandler lifecycleHandler = new ArtioLifecycleHandler();

        AgentRunner agentRunner = new AgentRunner(new SleepingIdleStrategy(), Throwable::printStackTrace,
                null,
                new CompositeAgent(new LibraryAgent(mediaDriver.aeronDirectoryName(), lifecycleHandler),
                        new PublisherAgent(lifecycleHandler)));

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