package org.example;

import org.HdrHistogram.Histogram;
import quickfix.*;
import quickfix.field.MsgType;
import quickfix.field.SendingTime;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static quickfix.field.MsgType.MARKET_DATA_INCREMENTAL_REFRESH;

public class InitiatorApplication implements Application {

    private SessionID sessionId;

    private volatile boolean hasLogOn;

    Histogram histogram = new Histogram(3);
    @Override
    public void onCreate(SessionID sessionID) {

    }

    @Override
    public void onLogon(SessionID sessionID) {
        this.sessionId = sessionID;
        hasLogOn = true;
        System.out.println("Logged on !!!");
    }

    @Override
    public void onLogout(SessionID sessionID) {
        this.sessionId = null;
        hasLogOn = false;
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
        try {
            if (message.getHeader().getString(MsgType.FIELD).equals(MARKET_DATA_INCREMENTAL_REFRESH)) {
                LocalDateTime localDateTime = message.getHeader().getUtcTimeStamp(SendingTime.FIELD);
                LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
                long diff = ChronoUnit.MILLIS.between(localDateTime, now);
                histogram.recordValue(diff);
            }
        } catch (FieldNotFound e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Histogram getHistogram() {
        return histogram;
    }

    public SessionID sessionID() {
        return sessionId;
    }

    public boolean isHasLogOn() {
        return hasLogOn;
    }
}
