package org.example;

import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.SleepingIdleStrategy;
import quickfix.*;

public class QfjAcceptorApp {
    public static void main(String[] args) throws ConfigError {
        SessionSettings sessionSettings = new SessionSettings(FileUtil.open(QfjAcceptorApp.class, "acceptor.cfg"));
        MessageStoreFactory messageStoreFactory = new NoopStoreFactory();
        AcceptorApplication application = new AcceptorApplication();
        SocketAcceptor connector = new SocketAcceptor(
                application,
                messageStoreFactory,
                sessionSettings,
                new NoopLogsFactory(),
                new DefaultMessageFactory());

        connector.start();

        MarketDataPublisherAgent marketDataPublisherAgent = new MarketDataPublisherAgent(application);

        AgentRunner.startOnThread(new AgentRunner(new SleepingIdleStrategy(), Throwable::printStackTrace, null, marketDataPublisherAgent));
    }
}