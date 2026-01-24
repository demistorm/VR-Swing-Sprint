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
        // Server query (client asks server if mod is installed)
        INSTANCE.register(ServerCapabilityQuery.class,
            (data, buf) -> {},
            (buf) -> new ServerCapabilityQuery(),
            (data, player) -> NetworkHandlers.handleCapabilityQuery(player)
        );

        // Server capability response (server confirms it has mod)
        INSTANCE.register(ServerCapabilityResponse.class,
            (data, buf) -> buf.writeBoolean(data.supportsCustomSpeeds()),
            (buf) -> new ServerCapabilityResponse(buf.readBoolean()),
            (data, player) -> NetworkHandlers.handleCapabilityResponse(player, data)
        );

        // Sprint speed packet (client sends speed multiplier)
        INSTANCE.register(SprintSpeedData.class,
            (data, buf) -> buf.writeFloat(data.speedMultiplier()),
            (buf) -> new SprintSpeedData(buf.readFloat()),
            (data, player) -> NetworkHandlers.handleSprintSpeed(player, data)
        );
    }
}
