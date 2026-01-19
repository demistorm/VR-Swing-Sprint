package win.demistorm.vr_swing_sprint.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import win.demistorm.vr_swing_sprint.client.VRSwingSprintClient;

public final class FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Initialize client-side systems (tracker registration, etc.)
        VRSwingSprintClient.initializeClient();
    }
}
