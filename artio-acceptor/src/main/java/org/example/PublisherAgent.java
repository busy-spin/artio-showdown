package org.example;

import io.github.busy_spin.artio_acceptor.codecs.MDUpdateAction;
import io.github.busy_spin.artio_acceptor.codecs.builder.MarketDataIncrementalRefreshEncoder;
import io.github.busy_spin.artio_acceptor.codecs.builder.InstrumentEncoder;
import org.agrona.concurrent.Agent;
import org.agrona.concurrent.SystemEpochClock;

public class PublisherAgent implements Agent {

    private final ArtioLifecycleHandler lifecycleHandler;

    private final int maxMessagePerWindow = 10;

    private int messageInThisWindow = 0;

    private long windowStartTime = SystemEpochClock.INSTANCE.time();

    private final long windowLengthInMs = 10;

    private final MarketDataIncrementalRefreshEncoder encoder = new MarketDataIncrementalRefreshEncoder();

    public PublisherAgent(ArtioLifecycleHandler lifecycleHandler) {

        this.lifecycleHandler = lifecycleHandler;
    }


    @Override
    public void onStart() {
        Agent.super.onStart();
    }

    @Override
    public int doWork() throws Exception {
        if (lifecycleHandler.isReadyToFire()) {
            long timeNow = SystemEpochClock.INSTANCE.time();
            if (timeNow > windowStartTime + windowLengthInMs) {
                windowStartTime = timeNow;
                messageInThisWindow = 0;
            }

            if (messageInThisWindow < maxMessagePerWindow) {
                MarketDataIncrementalRefreshEncoder.MDEntriesGroupEncoder groupEncoder = encoder.mDEntriesGroup(1);
                groupEncoder.orderID("BNA");
                groupEncoder.mDEntrySize(1000, 0);
                groupEncoder.mDEntryPx(1297, -2);
                groupEncoder.mDUpdateAction(MDUpdateAction.NEW);
                InstrumentEncoder instrument = groupEncoder.instrument();
                instrument.symbol("BNA");

                lifecycleHandler.session().trySend(encoder);
                messageInThisWindow++;
            }
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void onClose() {
        Agent.super.onClose();
    }

    @Override
    public String roleName() {
        return "reporter";
    }
}
