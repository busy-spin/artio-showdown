package org.example;

import quickfix.Log;
import quickfix.LogFactory;
import quickfix.SessionID;

public class NoopLogsFactory implements LogFactory {
    @Override
    public Log create(SessionID sessionID) {
        return new NoopLog();
    }
}
