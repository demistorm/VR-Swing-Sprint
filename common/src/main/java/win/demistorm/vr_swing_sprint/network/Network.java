package win.demistorm.vr_swing_sprint.network;

import win.demistorm.vr_swing_sprint.network.data.ServerCapabilityQuery;
import win.demistorm.vr_swing_sprint.network.data.ServerCapabilityResponse;
import win.demistorm.vr_swing_sprint.network.data.SprintSpeedData;

// Handles all networking between client and server
public class Network {

    // Main network channel
    public static final NetworkChannel INSTANCE = new NetworkChannel();

    // Set up networking (call from main mod class)
    public static void initialize() {
        registerPackets();
    }

    // Register all packet types
    private static void registerPackets() {
        // Server capability query (client asks server if mod is installed)
        INSTANCE.register(ServerCapabilityQuery.class,
            // Empty packet, no data to write
            (data, buf) -> {},
            // Empty packet, no data to read
            (buf) -> new ServerCapabilityQuery(),
            // Process query - server responds with capability
            (data, player) -> NetworkHandlers.handleCapabilityQuery(player, data)
        );

        // Server capability response (server confirms it has the mod)
        INSTANCE.register(ServerCapabilityResponse.class,
            // Write response
            (data, buf) -> buf.writeBoolean(data.supportsCustomSpeeds()),
            // Read response
            (buf) -> new ServerCapabilityResponse(buf.readBoolean()),
            // Process response - client side only
            (data, player) -> NetworkHandlers.handleCapabilityResponse(player, data)
        );

        // Sprint speed packet (client sends calculated speed multiplier)
        INSTANCE.register(SprintSpeedData.class,
            // Write speed multiplier
            (data, buf) -> buf.writeFloat(data.speedMultiplier()),
            // Read speed multiplier
            (buf) -> new SprintSpeedData(buf.readFloat()),
            // Process speed update
            (data, player) -> NetworkHandlers.handleSprintSpeed(player, data)
        );
    }
}
