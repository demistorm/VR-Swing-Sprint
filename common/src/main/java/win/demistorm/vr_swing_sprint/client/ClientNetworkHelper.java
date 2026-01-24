package win.demistorm.vr_swing_sprint.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import win.demistorm.vr_swing_sprint.network.Network;
import win.demistorm.vr_swing_sprint.network.data.ServerCapabilityQuery;
import win.demistorm.vr_swing_sprint.network.data.SprintSpeedData;

// Forwards client network information to cross-platform Network system
public final class ClientNetworkHelper {
    private static final Logger log = LogManager.getLogger(ClientNetworkHelper.class);

    private ClientNetworkHelper() {}

    // Send server capability query (call when player joins world)
    public static void sendCapabilityQuery() {
        log.debug("[VR Swing Sprint] Sending server capability query");
        Network.INSTANCE.sendToServer(new ServerCapabilityQuery());
    }

    // Send sprint speed multiplier to server
    public static void sendSprintSpeed(float multiplier) {
        // Safety check: Don't send packets to vanilla servers!
        if (!SprintHelper.hasServerCapability()) {
            log.debug("[VR Swing Sprint] Skipping speed packet - server doesn't have mod (multiplier: {})", multiplier);
            return;
        }

        log.debug("[VR Swing Sprint] Sending sprint speed multiplier: {}", multiplier);
        Network.INSTANCE.sendToServer(new SprintSpeedData(multiplier));
    }
}
