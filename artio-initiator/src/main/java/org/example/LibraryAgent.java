package org.example;

import io.aeron.CommonContext;
import org.agrona.concurrent.Agent;
import org.agrona.concurrent.AgentInvoker;
import org.agrona.concurrent.SystemEpochClock;
import uk.co.real_logic.artio.library.FixLibrary;
import uk.co.real_logic.artio.library.LibraryConfiguration;

import java.util.Collections;

public class LibraryAgent implements Agent {

    private final String aeronDirName;

    private FixLibrary fixLibrary;

    private long connectCheckWindow = 5_000;

    long timeNow = SystemEpochClock.INSTANCE.time();

    // initialize to a value so that first duty cycle will do a connection check
    private long lastConnectCheckTime = timeNow - connectCheckWindow - 1;
    private final ArtioLifecycleHandler lifecycleHandler;

    public LibraryAgent(String aeronDirName, ArtioLifecycleHandler lifecycleHandler) {
        this.aeronDirName = aeronDirName;
        this.lifecycleHandler = lifecycleHandler;
    }

    @Override
    public void onStart() {
        LibraryConfiguration libraryConfiguration = new LibraryConfiguration()
                .libraryConnectHandler(lifecycleHandler)
                .sessionAcquireHandler(lifecycleHandler)
                .sessionExistsHandler(lifecycleHandler)
                .libraryAeronChannels(Collections.singletonList(CommonContext.IPC_CHANNEL))
                .defaultHeartbeatIntervalInS(15);

        libraryConfiguration.aeronContext().aeronDirectoryName(aeronDirName);


        fixLibrary = FixLibrary.connect(libraryConfiguration);
    }

    @Override
    public int doWork() throws Exception {
        fixLibrary.poll(10);
        if (timeNow > lastConnectCheckTime + connectCheckWindow) {
            lifecycleHandler.keepConnected();
            lastConnectCheckTime = timeNow;
        }
        timeNow = SystemEpochClock.INSTANCE.time();
        return 1;
    }

    @Override
    public void onClose() {
        fixLibrary.close();
        Agent.super.onClose();
    }

    @Override
    public String roleName() {
        return "agent-one";
    }
}
