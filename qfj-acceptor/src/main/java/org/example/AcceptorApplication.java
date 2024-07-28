package org.example;

import quickfix.*;

public class AcceptorApplication implements Application {

    private SessionID sessionId;

    private volatile boolean hasLogOn;
    @Override
    public void onCreate(SessionID sessionID) {

    }

    @Override
    public void onLogon(SessionID sessionID) {
        this.sessionId = sessionID;
        hasLogOn = true;
    }

    @Override
    public void onLogout(SessionID sessionID) {
        hasLogOn = true;
    }

    @Override
    public void toAdmin(Message message, SessionID sessionID) {

    }

    @Override
    public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {

    }

    @Override
    public void toApp(Message message, SessionID sessionID) throws DoNotSend {

    }

    @Override
    public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {

    }

    public SessionID sessionID() {
        return sessionId;
    }

    public boolean isHasLogOn() {
        return hasLogOn;
    }
}
