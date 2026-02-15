package com.extremerisinglava.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig {
        public static final ModConfigSpec SPEC;

        public static final ModConfigSpec.IntValue BORDER_INITIAL_RADIUS;
        public static final ModConfigSpec.IntValue BORDER_FINAL_RADIUS;
        public static final ModConfigSpec.IntValue BORDER_CLOSE_TIME_SECONDS;

        public static final ModConfigSpec.IntValue LAVA_INITIAL_Y;
        public static final ModConfigSpec.IntValue LAVA_MAX_Y;
        public static final ModConfigSpec.IntValue LAVA_RAISE_EVERY_SECONDS;
        public static final ModConfigSpec.IntValue LAVA_FILL_HALF_SIZE;

        public static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> CHEST_ITEMS;
        public static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> SUPPLY_ITEMS;

        static {
                ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

                builder.comment("WorldBorder Settings").push("border");
                BORDER_INITIAL_RADIUS = builder
                                .comment("Initial radius of the WorldBorder (in blocks)")
                                .defineInRange("initialRadius", 500, 10, 30000);
                BORDER_FINAL_RADIUS = builder
                                .comment("Final radius of the WorldBorder (in blocks)")
                                .defineInRange("finalRadius", 10, 1, 30000);
                BORDER_CLOSE_TIME_SECONDS = builder
                                .comment("Time in seconds for the border to close from initial to final radius")
                                .defineInRange("closeTimeSeconds", 600, 10, 36000);
                builder.pop();

                builder.comment("Lava Settings").push("lava");
                LAVA_INITIAL_Y = builder
                                .comment("Initial Y level where lava starts (absolute value)")
                                .defineInRange("initialY", 1, -64, 319);
                LAVA_MAX_Y = builder
                                .comment("Maximum Y level the lava will rise to")
                                .defineInRange("maxY", 100, -64, 319);
                LAVA_RAISE_EVERY_SECONDS = builder
                                .comment("Seconds between each lava layer rise")
                                .defineInRange("raiseEverySeconds", 30, 1, 3600);
                LAVA_FILL_HALF_SIZE = builder
                                .comment("Half-size of the square lava fill area (e.g., 100 = 200x200 centered at 0,0)")
                                .defineInRange("fillHalfSize", 100, 10, 1000);
                builder.pop();

                builder.comment("Ordinary Chest Settings").push("chests");
                CHEST_ITEMS = builder
                                .comment("Items for ordinary chests (format: 'minecraft:item_id,quantity')")
                                .defineList("ordinaryItems",
                                                java.util.List.of(
                                                                "minecraft:bread,8",
                                                                "minecraft:iron_ingot,4",
                                                                "minecraft:oak_planks,16",
                                                                "minecraft:stone_sword,1",
                                                                "minecraft:leather_chestplate,1",
                                                                "minecraft:bow,1",
                                                                "minecraft:arrow,16"),
                                                obj -> obj instanceof String);
                builder.pop();

                builder.comment("Supply Drop Settings").push("supply");
                SUPPLY_ITEMS = builder
                                .comment("Items for epic supply drop chests (format: 'minecraft:item_id,quantity')")
                                .defineList("epicItems",
                                                java.util.List.of(
                                                                "minecraft:diamond_sword,1",
                                                                "minecraft:diamond_chestplate,1",
                                                                "minecraft:golden_apple,4",
                                                                "minecraft:ender_pearl,2",
                                                                "minecraft:enchanted_golden_apple,1"),
                                                obj -> obj instanceof String);
                builder.pop();

                SPEC = builder.build();
        }
}
