package com.extremerisinglava.event;

import com.extremerisinglava.ExtremeRisingLavaMod;
import com.extremerisinglava.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventManager {
    private static boolean running = false;
    private static boolean paused = false;

    private static long elapsedTicks = 0;
    private static long totalBorderTicks = 0;

    private static int currentLavaY = 0;
    private static int maxLavaY = 0;
    private static long nextLavaRaiseTick = 0;
    private static int lavaRaiseIntervalTicks = 0;

    private static ServerBossEvent bossbar = null;

    private static double savedBorderSize = 0;
    private static double targetBorderSize = 0;
    private static long remainingBorderTimeMs = 0;

    private static boolean initialBorderSaved = false;
    private static double initialBorderCenterX = 0;
    private static double initialBorderCenterZ = 0;
    private static double initialBorderSize = 0;

    private static final Map<UUID, Integer> killCounts = new HashMap<>();

    public static boolean isRunning() {
        return running;
    }

    public static boolean isPaused() {
        return paused;
    }

    public static long getElapsedTicks() {
        return elapsedTicks;
    }

    public static long getTotalBorderTicks() {
        return totalBorderTicks;
    }

    public static int getCurrentLavaY() {
        return currentLavaY;
    }

    public static int getMaxLavaY() {
        return maxLavaY;
    }

    public static long getNextLavaRaiseTick() {
        return nextLavaRaiseTick;
    }

    public static int getLavaRaiseIntervalTicks() {
        return lavaRaiseIntervalTicks;
    }

    public static long getPausedRemainingBorderTimeMs() {
        return remainingBorderTimeMs;
    }

    public static int getKills(UUID playerId) {
        return killCounts.getOrDefault(playerId, 0);
    }

    public static void recordPvpKill(ServerPlayer killer) {
        if (killer == null) {
            return;
        }
        UUID id = killer.getUUID();
        killCounts.put(id, getKills(id) + 1);
    }

    public static String formatDurationTicks(long ticks) {
        long totalSeconds = Math.max(0, ticks / 20);
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }

    public static void broadcastElimination(MinecraftServer server, ServerPlayer eliminated) {
        if (server == null || eliminated == null) {
            return;
        }

        String duration = formatDurationTicks(elapsedTicks);
        int kills = getKills(eliminated.getUUID());

        Component message = Component.empty()
                .append(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        .withStyle(style -> style.withColor(0xFF0000)))
                .append(Component.literal("\n"))
                .append(Component.literal("  ☠ ").withStyle(style -> style.withColor(0xFF4444)))
                .append(eliminated.getName().copy().withStyle(style -> style.withColor(0xFF5555).withBold(true)))
                .append(Component.literal(" ").withStyle(style -> style.withColor(0xFF4444)))
                .append(Component.literal("ELIMINADO").withStyle(style -> style.withColor(0xFF0000).withBold(true)))
                .append(Component.literal(" ☠").withStyle(style -> style.withColor(0xFF4444)))
                .append(Component.literal("\n\n"))
                .append(Component.literal("  ⏱ Duró: ").withStyle(style -> style.withColor(0xAAAAAA)))
                .append(Component.literal(duration).withStyle(style -> style.withColor(0x00FF7F).withBold(true)))
                .append(Component.literal("\n"))
                .append(Component.literal("  ⚔ Asesinatos: ").withStyle(style -> style.withColor(0xAAAAAA)))
                .append(Component.literal(String.valueOf(kills))
                        .withStyle(style -> style.withColor(0xFFD700).withBold(true)))
                .append(Component.literal("\n"))
                .append(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        .withStyle(style -> style.withColor(0xFF0000)));

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(message);
        }
    }

    public static void checkForWinner(MinecraftServer server) {
        if (!running || server == null) {
            return;
        }

        int aliveOverworld = 0;
        int aliveOutsideOverworld = 0;
        ServerPlayer lastAliveOverworld = null;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator() || player.isCreative()) {
                continue;
            }

            if (player.level().dimension() == Level.OVERWORLD) {
                aliveOverworld++;
                lastAliveOverworld = player;
            } else {
                aliveOutsideOverworld++;
            }
        }

        if (aliveOverworld > 1) {
            return;
        }

        if (aliveOverworld == 0 && aliveOutsideOverworld > 0) {
            return;
        }

        long durationTicks = elapsedTicks;
        Component message;
        if (aliveOverworld == 1 && aliveOutsideOverworld == 0 && lastAliveOverworld != null) {
            String duration = formatDurationTicks(durationTicks);
            int kills = getKills(lastAliveOverworld.getUUID());
            message = Component.empty()
                    .append(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                            .withStyle(style -> style.withColor(0x00FF7F)))
                    .append(Component.literal("\n"))
                    .append(Component.literal("  ✦ ").withStyle(style -> style.withColor(0x00FF7F)))
                    .append(Component.literal("GANADOR").withStyle(style -> style.withColor(0x00FF7F).withBold(true)))
                    .append(Component.literal(" ✦").withStyle(style -> style.withColor(0x00FF7F)))
                    .append(Component.literal("\n\n"))
                    .append(Component.literal("  ").withStyle(style -> style.withColor(0xAAAAAA)))
                    .append(lastAliveOverworld.getName().copy()
                            .withStyle(style -> style.withColor(0xFFFFFF).withBold(true)))
                    .append(Component.literal("\n"))
                    .append(Component.literal("  ⏱ Duración: ").withStyle(style -> style.withColor(0xAAAAAA)))
                    .append(Component.literal(duration).withStyle(style -> style.withColor(0x00FF7F).withBold(true)))
                    .append(Component.literal("\n"))
                    .append(Component.literal("  ⚔ Asesinatos: ").withStyle(style -> style.withColor(0xAAAAAA)))
                    .append(Component.literal(String.valueOf(kills))
                            .withStyle(style -> style.withColor(0xFFD700).withBold(true)))
                    .append(Component.literal("\n"))
                    .append(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                            .withStyle(style -> style.withColor(0x00FF7F)));
        } else {
            if (aliveOverworld > 0 || aliveOutsideOverworld > 0) {
                return;
            }

            message = Component.empty()
                    .append(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                            .withStyle(style -> style.withColor(0xFF0000)))
                    .append(Component.literal("\n"))
                    .append(Component.literal("  ☠ ").withStyle(style -> style.withColor(0xFF4444)))
                    .append(Component.literal("No hubo sobrevivientes")
                            .withStyle(style -> style.withColor(0xFF0000).withBold(true)))
                    .append(Component.literal(" ☠").withStyle(style -> style.withColor(0xFF4444)))
                    .append(Component.literal("\n"))
                    .append(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                            .withStyle(style -> style.withColor(0xFF0000)));
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(message);
        }

        stop(server);
    }

    public static void start(ServerLevel level) {
        if (running) {
            return;
        }

        int initialRadius = ModConfig.BORDER_INITIAL_RADIUS.get();
        int finalRadius = ModConfig.BORDER_FINAL_RADIUS.get();
        int closeTimeSeconds = ModConfig.BORDER_CLOSE_TIME_SECONDS.get();

        currentLavaY = ModConfig.LAVA_INITIAL_Y.get();
        maxLavaY = ModConfig.LAVA_MAX_Y.get();
        int raiseEverySeconds = ModConfig.LAVA_RAISE_EVERY_SECONDS.get();
        lavaRaiseIntervalTicks = raiseEverySeconds * 20;

        elapsedTicks = 0;
        totalBorderTicks = (long) closeTimeSeconds * 20;
        nextLavaRaiseTick = lavaRaiseIntervalTicks;
        paused = false;
        running = true;

        killCounts.clear();

        WorldBorder worldBorder = level.getWorldBorder();

        if (!initialBorderSaved) {
            initialBorderCenterX = worldBorder.getCenterX();
            initialBorderCenterZ = worldBorder.getCenterZ();
            initialBorderSize = worldBorder.getSize();
            initialBorderSaved = true;
        }

        worldBorder.setCenter(0, 0);
        worldBorder.setSize(initialRadius * 2.0);
        worldBorder.lerpSizeBetween(initialRadius * 2.0, finalRadius * 2.0, closeTimeSeconds * 1000L);

        bossbar = new ServerBossEvent(
                Component.literal("Borde: " + initialRadius + "m | Lava: Y=" + currentLavaY),
                BossEvent.BossBarColor.GREEN,
                BossEvent.BossBarOverlay.PROGRESS);
        bossbar.setProgress(1.0f);

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level().dimension() == Level.OVERWORLD) {
                bossbar.addPlayer(player);
            }
        }

        fillLavaLayer(level, currentLavaY);

        ExtremeRisingLavaMod.LOGGER.info("Extreme Rising Lava event started!");
    }

    public static void stop(MinecraftServer server) {
        if (!running) {
            return;
        }

        running = false;
        paused = false;
        elapsedTicks = 0;

        if (bossbar != null) {
            bossbar.removeAllPlayers();
            bossbar = null;
        }

        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld != null) {
            WorldBorder worldBorder = overworld.getWorldBorder();
            if (initialBorderSaved) {
                worldBorder.setCenter(initialBorderCenterX, initialBorderCenterZ);
                worldBorder.setSize(initialBorderSize);
                initialBorderSaved = false;
            } else {
                worldBorder.setSize(worldBorder.getSize());
            }
        }

        killCounts.clear();

        ExtremeRisingLavaMod.LOGGER.info("Extreme Rising Lava event stopped!");
    }

    public static void pause(MinecraftServer server) {
        if (!running) {
            return;
        }

        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }

        WorldBorder worldBorder = overworld.getWorldBorder();

        if (!paused) {
            savedBorderSize = worldBorder.getSize();
            targetBorderSize = ModConfig.BORDER_FINAL_RADIUS.get() * 2.0;
            remainingBorderTimeMs = worldBorder.getLerpRemainingTime();
            worldBorder.setSize(savedBorderSize);
            paused = true;
        } else {
            if (remainingBorderTimeMs > 0) {
                worldBorder.lerpSizeBetween(savedBorderSize, targetBorderSize, remainingBorderTimeMs);
            }
            paused = false;
        }

        ExtremeRisingLavaMod.LOGGER.info("Extreme Rising Lava event " + (paused ? "paused" : "resumed") + "!");
    }

    public static void tick(MinecraftServer server) {
        if (!running) {
            return;
        }

        if (paused) {
            checkForWinner(server);
            return;
        }

        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }

        elapsedTicks++;

        if (elapsedTicks >= nextLavaRaiseTick && currentLavaY < maxLavaY) {
            currentLavaY++;
            fillLavaLayer(overworld, currentLavaY);
            nextLavaRaiseTick = elapsedTicks + lavaRaiseIntervalTicks;
        }

        if (currentLavaY >= maxLavaY && elapsedTicks > totalBorderTicks) {
            stop(server);
            return;
        }

        checkForWinner(server);
        if (!running) {
            return;
        }

        updateBossbar(overworld);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.level().dimension() == Level.OVERWORLD && bossbar != null
                    && !bossbar.getPlayers().contains(player)) {
                bossbar.addPlayer(player);
            }
        }
    }

    private static void fillLavaLayer(ServerLevel level, int y) {
        int halfSize = ModConfig.LAVA_FILL_HALF_SIZE.get();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = -halfSize; x < halfSize; x++) {
            for (int z = -halfSize; z < halfSize; z++) {
                pos.set(x, y, z);

                if (!level.hasChunkAt(pos)) {
                    continue;
                }

                if (level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 2);
                }
            }
        }

        ExtremeRisingLavaMod.LOGGER.debug("Filled lava layer at Y={}", y);
    }

    private static void updateBossbar(ServerLevel level) {
        if (bossbar == null) {
            return;
        }

        WorldBorder worldBorder = level.getWorldBorder();
        int currentBorderSize = (int) (worldBorder.getSize() / 2.0);

        float progress;
        if (elapsedTicks < totalBorderTicks) {
            progress = Math.max(0f, 1f - (elapsedTicks / (float) totalBorderTicks));
        } else {
            progress = 0f;
        }

        bossbar.setProgress(progress);
        bossbar.setName(Component.literal("Borde: " + currentBorderSize + "m | Lava: Y=" + currentLavaY));

        BossEvent.BossBarColor color;
        if (progress > 0.66f) {
            color = BossEvent.BossBarColor.GREEN;
        } else if (progress > 0.33f) {
            color = BossEvent.BossBarColor.YELLOW;
        } else {
            color = BossEvent.BossBarColor.RED;
        }
        bossbar.setColor(color);
    }
}
