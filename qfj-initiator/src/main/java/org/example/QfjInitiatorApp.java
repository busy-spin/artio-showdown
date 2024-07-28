package org.example;

import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.SleepingIdleStrategy;
import quickfix.*;

public class QfjInitiatorApp {
    public static void main(String[] args) throws ConfigError {
        SessionSettings sessionSettings = new SessionSettings(FileUtil.open(QfjInitiatorApp.class, "initiator.cfg"));
        MessageStoreFactory messageStoreFactory = new NoopStoreFactory();
        InitiatorApplication application = new InitiatorApplication();
        SocketInitiator connector = new SocketInitiator(
                application,
                messageStoreFactory,
                sessionSettings,
                new NoopLogsFactory(),
                new DefaultMessageFactory());

        connector.start();

        AgentRunner agentRunner = new AgentRunner(new SleepingIdleStrategy(), Throwable::printStackTrace, null, new ReporterAgent(application));
        AgentRunner.startOnThread(agentRunner);
    }
}