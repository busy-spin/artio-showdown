package org.example;

import quickfix.*;

public class Main {
    public static void main(String[] args) throws ConfigError {
        SessionSettings sessionSettings = new SessionSettings(FileUtil.open(Main.class, "acceptor.cfg"));
        MessageStoreFactory messageStoreFactory = new NoopStoreFactory();
        SocketAcceptor connector = new SocketAcceptor(
                new AcceptorApplication(),
                messageStoreFactory,
                sessionSettings,
                new ScreenLogFactory(),
                new DefaultMessageFactory());

        connector.start();
    }
}