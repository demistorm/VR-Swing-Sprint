package win.demistorm.vr_swing_sprint.forge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.util.function.Consumer;

// Forge platform implementations
@SuppressWarnings("unused")
public class PlatformImpl {

    // Check if running on client
    public static boolean isClientSide() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }

    // Check if in dev mode
    public static boolean isDevelopmentEnvironment() {
        return !FMLEnvironment.production;
    }

    // Check if Forge-like (always true for Forge)
    public static boolean isForgeLike() {
        return true;
    }

    // Check if mod is installed
    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    // Get config directory
    public static File getConfigFolder() {
        return FMLPaths.CONFIGDIR.get().toFile();
    }

    // Listen for server ticks
    public static void registerServerPostTickListener(Consumer<MinecraftServer> listener) {
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                listener.accept(event.getServer());
            }
        });
    }

    // Listen for player ticks
    public static void registerServerPlayerPostTickListener(Consumer<ServerPlayer> listener) {
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                event.getServer().getPlayerList().getPlayers().forEach(listener);
            }
        });
    }

    // Listen for player joins
    public static void registerServerPlayerJoinListener(Consumer<ServerPlayer> listener) {
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                listener.accept(sp);
            }
        });
    }

    // Listen for player leaves
    public static void registerServerPlayerLeaveListener(Consumer<ServerPlayer> listener) {
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                listener.accept(sp);
            }
        });
    }

    // Register commands
    public static void registerCommands(Consumer<CommandDispatcher<CommandSourceStack>> listener) {
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> listener.accept(event.getDispatcher()));
    }

    // Get Minecraft version
    public static String getMinecraftVersion() {
        return ModList.get().getModContainerById("minecraft")
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("unknown");
    }

    // Get mod loader name
    public static String getLoaderName() {
        return "forge";
    }

    // Get client registry access
    public static RegistryAccess getClientRegistryAccess() {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() != null) {
            return client.getConnection().registryAccess();
        }
        return null;
    }

    // Register input events (handled in client code)
    public static void registerClientInputEventHandlers() {
        // Implemented in PlatformClientImpl with @SubscribeEvent
    }
}
