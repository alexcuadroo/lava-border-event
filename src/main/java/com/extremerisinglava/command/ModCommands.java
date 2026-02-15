package com.extremerisinglava.command;

import com.extremerisinglava.config.ModConfig;
import com.extremerisinglava.event.EventManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.border.WorldBorder;

import java.util.List;
import java.util.Random;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("lavaevent")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("start")
                        .executes(ModCommands::startEvent))
                .then(Commands.literal("stop")
                        .executes(ModCommands::stopEvent))
                .then(Commands.literal("pause")
                        .executes(ModCommands::pauseEvent))
                .then(Commands.literal("reload")
                        .executes(ModCommands::reloadConfig))
                .then(Commands.literal("glowing")
                        .then(Commands.argument("duration", StringArgumentType.word())
                                .executes(ModCommands::applyGlowing)))
                .then(Commands.literal("scatter")
                        .executes(ModCommands::scatterPlayers))
                .then(Commands.literal("chest")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                                .executes(ModCommands::spawnChests)))
                .then(Commands.literal("supply")
                        .executes(ModCommands::spawnSupplyDrop)));
    }

    private static int startEvent(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (EventManager.isRunning()) {
            source.sendFailure(Component.literal("El evento ya está en ejecución!"));
            return 0;
        }

        ServerLevel overworld = source.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            source.sendFailure(Component.literal("No se pudo obtener el Overworld!"));
            return 0;
        }

        EventManager.start(overworld);
        source.sendSuccess(() -> Component.literal("§a¡Evento Extreme Rising Lava iniciado!"), true);
        return 1;
    }

    private static int stopEvent(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!EventManager.isRunning()) {
            source.sendFailure(Component.literal("No hay ningún evento en ejecución!"));
            return 0;
        }

        EventManager.stop(source.getServer());
        source.sendSuccess(() -> Component.literal("§c¡Evento Extreme Rising Lava detenido!"), true);
        return 1;
    }

    private static int pauseEvent(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!EventManager.isRunning()) {
            source.sendFailure(Component.literal("No hay ningún evento en ejecución!"));
            return 0;
        }

        EventManager.pause(source.getServer());
        boolean isPaused = EventManager.isPaused();
        source.sendSuccess(() -> Component.literal(isPaused ? "§e¡Evento pausado!" : "§a¡Evento reanudado!"), true);
        return 1;
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            ModConfig.SPEC.afterReload();
            source.sendSuccess(() -> Component
                    .literal("§a¡Configuración recargada! Los nuevos valores se aplicarán en el próximo evento."),
                    true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cError al recargar: " + e.getMessage()));
            return 0;
        }
    }

    private static int applyGlowing(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String durationArg = StringArgumentType.getString(context, "duration");

        int seconds = parseDuration(durationArg);
        if (seconds <= 0) {
            source.sendFailure(Component.literal("§cFormato inválido. Usa: 1m, 30s, 2m30s"));
            return 0;
        }

        int ticks = seconds * 20;
        int playerCount = 0;

        String timeDisplay = formatDuration(seconds);

        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            player.addEffect(new MobEffectInstance(MobEffects.GLOWING, ticks, 0, false, false, true));
            playerCount++;

            player.sendSystemMessage(Component.empty()
                    .append(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                            .withStyle(style -> style.withColor(0xFF6B00)))
                    .append(Component.literal("\n"))
                    .append(Component.literal("  ✦ ").withStyle(style -> style.withColor(0xFFD700)))
                    .append(Component.literal("GLOWING ACTIVADO")
                            .withStyle(style -> style.withColor(0xFF4500).withBold(true)))
                    .append(Component.literal(" ✦").withStyle(style -> style.withColor(0xFFD700)))
                    .append(Component.literal("\n\n"))
                    .append(Component.literal("  ⏱ Duración: ").withStyle(style -> style.withColor(0xAAAAAA)))
                    .append(Component.literal(timeDisplay).withStyle(style -> style.withColor(0x00FF7F).withBold(true)))
                    .append(Component.literal("\n"))
                    .append(Component.literal("  👁 ¡Todos los jugadores son visibles!")
                            .withStyle(style -> style.withColor(0xFFFF00)))
                    .append(Component.literal("\n"))
                    .append(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                            .withStyle(style -> style.withColor(0xFF6B00))));
        }

        int finalCount = playerCount;
        source.sendSuccess(
                () -> Component
                        .literal("§e¡Efecto Glowing aplicado a " + finalCount + " jugadores por " + timeDisplay + "!"),
                true);
        return 1;
    }

    private static String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        if (minutes > 0 && seconds > 0) {
            return minutes + "m " + seconds + "s";
        } else if (minutes > 0) {
            return minutes + " minuto" + (minutes > 1 ? "s" : "");
        } else {
            return seconds + " segundo" + (seconds > 1 ? "s" : "");
        }
    }

    private static int parseDuration(String input) {
        int totalSeconds = 0;
        String current = "";

        for (char c : input.toLowerCase().toCharArray()) {
            if (Character.isDigit(c)) {
                current += c;
            } else if (c == 'm') {
                if (!current.isEmpty()) {
                    totalSeconds += Integer.parseInt(current) * 60;
                    current = "";
                }
            } else if (c == 's') {
                if (!current.isEmpty()) {
                    totalSeconds += Integer.parseInt(current);
                    current = "";
                }
            }
        }

        if (!current.isEmpty()) {
            totalSeconds += Integer.parseInt(current);
        }

        return totalSeconds;
    }

    private static final Random RANDOM = new Random();

    private static int scatterPlayers(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel overworld = source.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            source.sendFailure(Component.literal("No se pudo obtener el Overworld!"));
            return 0;
        }

        WorldBorder border = overworld.getWorldBorder();
        int radius = (int) (border.getSize() / 2) - 5;
        if (radius < 10)
            radius = 10;

        int scattered = 0;
        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            if (player.level().dimension() != Level.OVERWORLD)
                continue;

            BlockPos safePos = findSafePosition(overworld, radius);
            if (safePos != null) {
                player.teleportTo(overworld, safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5,
                        java.util.Set.of(), player.getYRot(), player.getXRot(), true);
                scattered++;
                player.sendSystemMessage(Component
                        .literal("§e¡Has sido teletransportado a X=" + safePos.getX() + " Z=" + safePos.getZ() + "!"));
            }
        }

        int finalScattered = scattered;
        source.sendSuccess(() -> Component.literal("§a¡" + finalScattered + " jugadores dispersados dentro del borde!"),
                true);
        return 1;
    }

    private static BlockPos findSafePosition(ServerLevel level, int radius) {
        for (int attempts = 0; attempts < 50; attempts++) {
            int x = RANDOM.nextInt(radius * 2) - radius;
            int z = RANDOM.nextInt(radius * 2) - radius;
            int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z);

            BlockPos groundPos = new BlockPos(x, y - 1, z);
            BlockPos feetPos = new BlockPos(x, y, z);
            BlockPos headPos = new BlockPos(x, y + 1, z);

            boolean groundSolid = level.getBlockState(groundPos).isSolid();
            boolean feetClear = level.getBlockState(feetPos).isAir() || !level.getBlockState(feetPos).isSolid();
            boolean headClear = level.getBlockState(headPos).isAir() || !level.getBlockState(headPos).isSolid();

            if (groundSolid && feetClear && headClear) {
                return feetPos;
            }
        }
        return null;
    }

    private static int spawnChests(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        int amount = IntegerArgumentType.getInteger(context, "amount");

        ServerLevel overworld = source.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            source.sendFailure(Component.literal("No se pudo obtener el Overworld!"));
            return 0;
        }

        WorldBorder border = overworld.getWorldBorder();
        int radius = (int) (border.getSize() / 2) - 5;
        if (radius < 10)
            radius = 10;

        List<? extends String> itemConfigs = ModConfig.CHEST_ITEMS.get();
        int spawned = 0;

        for (int i = 0; i < amount; i++) {
            int x = RANDOM.nextInt(radius * 2) - radius;
            int z = RANDOM.nextInt(radius * 2) - radius;
            int y = overworld.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z);

            BlockPos pos = new BlockPos(x, y, z);
            overworld.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);

            if (overworld.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
                fillChestWithItems(chest, itemConfigs, false);
                spawned++;
            }
        }

        int finalSpawned = spawned;
        source.sendSuccess(() -> Component.literal("§a¡" + finalSpawned + " cofres spawneados en el área!"), true);
        return 1;
    }

    private static int spawnSupplyDrop(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        ServerLevel overworld = source.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            source.sendFailure(Component.literal("No se pudo obtener el Overworld!"));
            return 0;
        }

        WorldBorder border = overworld.getWorldBorder();
        int radius = (int) (border.getSize() / 2) - 5;
        if (radius < 10)
            radius = 10;

        int x = RANDOM.nextInt(radius * 2) - radius;
        int z = RANDOM.nextInt(radius * 2) - radius;
        int y = overworld.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z);

        BlockPos pos = new BlockPos(x, y, z);
        overworld.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);

        if (overworld.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            List<? extends String> itemConfigs = ModConfig.SUPPLY_ITEMS.get();
            fillChestWithItems(chest, itemConfigs, true);
        }

        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            player.sendSystemMessage(Component.empty()
                    .append(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                            .withStyle(style -> style.withColor(0xFFD700)))
                    .append(Component.literal("\n"))
                    .append(Component.literal("  ✦ ").withStyle(style -> style.withColor(0xFFD700)))
                    .append(Component.literal("SUPPLY DROP")
                            .withStyle(style -> style.withColor(0xFF6600).withBold(true)))
                    .append(Component.literal(" ✦").withStyle(style -> style.withColor(0xFFD700)))
                    .append(Component.literal("\n\n"))
                    .append(Component.literal("  📍 Coords: ").withStyle(style -> style.withColor(0xAAAAAA)))
                    .append(Component.literal("X=" + x + " Y=" + y + " Z=" + z)
                            .withStyle(style -> style.withColor(0x00FF7F).withBold(true)))
                    .append(Component.literal("\n"))
                    .append(Component.literal("  ¡Loot épico disponible!")
                            .withStyle(style -> style.withColor(0xFFFF00)))
                    .append(Component.literal("\n"))
                    .append(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                            .withStyle(style -> style.withColor(0xFFD700))));
        }

        source.sendSuccess(() -> Component.literal("§6¡Supply drop spawneado en X=" + x + " Y=" + y + " Z=" + z + "!"),
                true);
        return 1;
    }

    private static void fillChestWithItems(ChestBlockEntity chest, List<? extends String> itemConfigs,
            boolean allItems) {
        int[] slotHolder = { 0 };
        for (String config : itemConfigs) {
            if (slotHolder[0] >= 27)
                break;
            if (!allItems && RANDOM.nextFloat() > 0.6f)
                continue;

            String[] parts = config.split(",");
            if (parts.length != 2)
                continue;

            try {
                ResourceLocation itemId = ResourceLocation.parse(parts[0].trim());
                int quantity = Integer.parseInt(parts[1].trim());

                BuiltInRegistries.ITEM.get(itemId).ifPresent(itemRef -> {
                    chest.setItem(slotHolder[0]++, new ItemStack(itemRef.value(), quantity));
                });
            } catch (Exception ignored) {
            }
        }
    }
}
