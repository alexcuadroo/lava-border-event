package com.extremerisinglava;

import com.extremerisinglava.config.ModConfig;
import com.extremerisinglava.event.EventManager;
import com.extremerisinglava.command.ModCommands;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ExtremeRisingLavaMod.MOD_ID)
public class ExtremeRisingLavaMod {
    public static final String MOD_ID = "extremerisinglava";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static ModContainer container;

    public ExtremeRisingLavaMod(IEventBus modEventBus, ModContainer modContainer) {
        container = modContainer;
        modContainer.registerConfig(Type.COMMON, ModConfig.SPEC);

        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::onPlayerDeath);

        LOGGER.info("Evento lava-borde UruLand iniciado!");
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    private void onServerTick(ServerTickEvent.Post event) {
        EventManager.tick(event.getServer());
    }

    private void onPlayerDeath(LivingDeathEvent event) {
        if (!EventManager.isRunning()) {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.level().dimension() == Level.OVERWORLD) {
                player.setGameMode(GameType.SPECTATOR);
                player.sendSystemMessage(Component.empty()
                        .append(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                                .withStyle(style -> style.withColor(0xFF0000)))
                        .append(Component.literal("\n"))
                        .append(Component.literal("  ☠ ").withStyle(style -> style.withColor(0xFF4444)))
                        .append(Component.literal("¡HAS SIDO ELIMINADO!")
                                .withStyle(style -> style.withColor(0xFF0000).withBold(true)))
                        .append(Component.literal(" ☠").withStyle(style -> style.withColor(0xFF4444)))
                        .append(Component.literal("\n\n"))
                        .append(Component.literal("  Ahora eres ").withStyle(style -> style.withColor(0xAAAAAA)))
                        .append(Component.literal("ESPECTADOR")
                                .withStyle(style -> style.withColor(0x00BFFF).withBold(true)))
                        .append(Component.literal("\n"))
                        .append(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                                .withStyle(style -> style.withColor(0xFF0000))));
            }
        }
    }

    public static ModContainer getModContainer() {
        return container;
    }
}
