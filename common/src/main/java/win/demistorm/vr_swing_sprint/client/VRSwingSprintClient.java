package win.demistorm.vr_swing_sprint.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vivecraft.api.client.VRClientAPI;

// Client initialization (called by each platform)
public class VRSwingSprintClient {

    private static final Logger log = LogManager.getLogger(VRSwingSprintClient.class);

    // Set up clientside systems
    public static void initializeClient() {
        log.info("VR Swing Sprint (CLIENT) starting!");
        // Register VR tracker with Vivecraft
        registerTracker();
    }

    // Add tracker to Vivecraft system
    private static void registerTracker() {
        VRClientAPI.instance().addClientRegistrationHandler(event ->
                event.registerTrackers(new SwingTracker()));
        log.info("Registered SwingTracker with Vivecraft");
    }
}
