package win.demistorm.vr_swing_sprint.forge;

import win.demistorm.vr_swing_sprint.client.VRSwingSprintClient;

// Forge client initialization
public class ForgeClient {

    // Initialize client-side platform code
    public static void initialize() {
        // Initialize client-side systems (tracker registration, etc.)
        VRSwingSprintClient.initializeClient();
    }
}
