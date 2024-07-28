package org.example;

import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.SleepingIdleStrategy;
import quickfix.*;

public class Main {
    public static void main(String[] args) throws ConfigError {
        SessionSettings sessionSettings = new SessionSettings(FileUtil.open(Main.class, "acceptor.cfg"));
        MessageStoreFactory messageStoreFactory = new NoopStoreFactory();
        AcceptorApplication application = new AcceptorApplication();
        SocketAcceptor connector = new SocketAcceptor(
                application,
                messageStoreFactory,
                sessionSettings,
                new ScreenLogFactory(),
                new DefaultMessageFactory());

        connector.start();

        MarketDataPublisherAgent marketDataPublisherAgent = new MarketDataPublisherAgent(application);

        AgentRunner.startOnThread(new AgentRunner(new SleepingIdleStrategy(), Throwable::printStackTrace, null, marketDataPublisherAgent));
    }
}