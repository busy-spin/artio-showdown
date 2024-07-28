package org.example;

import io.aeron.logbuffer.ControlledFragmentHandler;
import io.github.busy_spin.artio_acceptor.codecs.decoder.HeartbeatDecoder;
import io.github.busy_spin.artio_acceptor.codecs.builder.MarketDataIncrementalRefreshEncoder;
import org.agrona.DirectBuffer;
import uk.co.real_logic.artio.library.*;
import uk.co.real_logic.artio.messages.DisconnectReason;
import uk.co.real_logic.artio.session.Session;
import uk.co.real_logic.artio.util.MutableAsciiBuffer;

import java.nio.ByteBuffer;

public class ArtioLifecycleHandler implements SessionHandler, SessionAcquireHandler, LibraryConnectHandler, SessionExistsHandler {


    private FixLibrary fixLibrary;

    private boolean readyToFire = false;

    MarketDataIncrementalRefreshEncoder encoder = new MarketDataIncrementalRefreshEncoder();

    MutableAsciiBuffer mutableAsciiBuffer = new MutableAsciiBuffer(ByteBuffer.allocateDirect(4 * 1014));

    @Override
    public void onConnect(FixLibrary fixLibrary) {
        System.out.println("Library connected ");
        this.fixLibrary = fixLibrary;
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
        if (HeartbeatDecoder.MESSAGE_TYPE == messageType) {
            System.out.println("Heart beat received");
            readyToFire  = true;
        }
        return ControlledFragmentHandler.Action.CONTINUE;
    }

    @Override
    public void onTimeout(int i, Session session) {

    }

    @Override
    public void onSlowStatus(int i, Session session, boolean b) {

    }

    @Override
    public ControlledFragmentHandler.Action onDisconnect(int i, Session session, DisconnectReason disconnectReason) {
        System.out.println("Disconnected " + session);
        readyToFire = false;
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

}
