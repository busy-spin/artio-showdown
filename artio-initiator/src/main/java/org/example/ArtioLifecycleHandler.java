package org.example;

import io.aeron.logbuffer.ControlledFragmentHandler;
import io.github.busy_spin.artio_initiator.codecs.FixDictionaryImpl;
import io.github.busy_spin.artio_initiator.codecs.decoder.MarketDataIncrementalRefreshDecoder;
import org.HdrHistogram.Histogram;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.SystemEpochClock;
import uk.co.real_logic.artio.Reply;
import uk.co.real_logic.artio.fields.UtcTimestampDecoder;
import uk.co.real_logic.artio.library.*;
import uk.co.real_logic.artio.messages.DisconnectReason;
import uk.co.real_logic.artio.session.Session;
import uk.co.real_logic.artio.util.MutableAsciiBuffer;

import java.nio.ByteBuffer;

public class ArtioLifecycleHandler implements SessionHandler, SessionAcquireHandler, LibraryConnectHandler, SessionExistsHandler {


    private FixLibrary fixLibrary;
    private Reply<Session> sessionReply;

    private Histogram histogram = new Histogram(3);

    UtcTimestampDecoder timestampDecoder = new UtcTimestampDecoder(false);

    MarketDataIncrementalRefreshDecoder decoder = new MarketDataIncrementalRefreshDecoder();

    MutableAsciiBuffer mutableAsciiBuffer = new MutableAsciiBuffer(ByteBuffer.allocateDirect(4 * 1014));

    SessionConfiguration sessionConfig = SessionConfiguration.builder()
            .fixDictionary(FixDictionaryImpl.class)
            .targetCompId("EXCHANGE")
            .senderCompId("TAKER_FIRM")
            .address("localhost", 9880)
            .build();
    @Override
    public void onConnect(FixLibrary fixLibrary) {
        System.out.println("Library connected ");
        this.fixLibrary = fixLibrary;
        sessionReply = fixLibrary.initiate(sessionConfig);
    }

    @Override
    public void onDisconnect(FixLibrary fixLibrary) {
        System.out.println("Library disconnected");
    }

    @Override
    public SessionHandler onSessionAcquired(Session session, SessionAcquiredInfo sessionAcquiredInfo) {
        return this;
    }

    @Override
    public ControlledFragmentHandler.Action onMessage(DirectBuffer buffer,
                                                      int offset,
                                                      int length,
                                                      int libraryId,
                                                      Session session,
                                                      int sequenceIndex,
                                                      long messageType,
                                                      long timestampInNs,
                                                      long position,
                                                      OnMessageInfo messageInfo) {
        if (MarketDataIncrementalRefreshDecoder.MESSAGE_TYPE == messageType) {
            mutableAsciiBuffer.wrap(buffer, offset, length);

            decoder.decode(mutableAsciiBuffer, 0, length);
            long time = timestampDecoder.decode(decoder.header().sendingTime());

            histogram.recordValue(SystemEpochClock.INSTANCE.time() - time);
        }
        return ControlledFragmentHandler.Action.CONTINUE;
    }

    @Override
    public void onTimeout(int i, Session session) {
        sessionReply = fixLibrary.initiate(sessionConfig);
    }

    @Override
    public void onSlowStatus(int i, Session session, boolean b) {

    }

    @Override
    public ControlledFragmentHandler.Action onDisconnect(int i, Session session, DisconnectReason disconnectReason) {
        System.out.println("Disconnected !!!");
        sessionReply = fixLibrary.initiate(sessionConfig);
        return ControlledFragmentHandler.Action.CONTINUE;
    }

    @Override
    public void onSessionStart(Session session) {
        System.out.println("Session started" + session.state().name());
    }

    @Override
    public void onSessionExists(FixLibrary fixLibrary, long l, String s, String s1, String s2,
                                String s3, String s4, String s5, int i, int i1) {

    }

    public Histogram getHistogram() {
        return histogram;
    }

    public void keepConnected() {
        if (sessionReply != null) {
            if (sessionReply.isExecuting()) {
                System.out.println("Waiting for session reply");
            } else {
                if (sessionReply.hasCompleted()) {
                    System.out.println("Session state " + sessionReply.resultIfPresent().state());
                    sessionReply = null;
                } else {
                    System.out.println("Error occurred" + sessionReply.error());
                    sessionReply = fixLibrary.initiate(sessionConfig);
                }
            }
        }
    }
}
