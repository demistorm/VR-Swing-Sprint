package win.demistorm.vr_swing_sprint;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public final class VRSwingSprint {
    public static final String MOD_ID = "vr_swing_sprint";
    public static final Logger LOGGER = LogManager.getLogger(VRSwingSprint.class);

    // Debug mode switch
    public static final boolean debugMode = true;

    static {
        Configurator.setLevel(MOD_ID, debugMode ? Level.DEBUG : Level.INFO);
    }

    public static void initialize() {
        // Initialize networking system
        win.demistorm.vr_swing_sprint.network.Network.initialize();
    }
}
