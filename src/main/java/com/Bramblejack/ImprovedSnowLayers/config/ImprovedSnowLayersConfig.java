package com.Bramblejack.ImprovedSnowLayers.config;

import com.Bramblejack.ImprovedSnowLayers.ImprovedSnowLayersMod;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionFlag;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.config.ConfigInstance;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.loading.FMLPaths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class ImprovedSnowLayersConfig {

    private static final ConfigInstance<ConfigData> INSTANCE = GsonConfigInstance.createBuilder(ConfigData.class)
            .setPath(FMLPaths.CONFIGDIR.get().resolve(ImprovedSnowLayersMod.MODID + ".json"))
            .build();

    private static volatile List<Predicate<BlockState>> whitelistMatchers = Collections.emptyList();
    private static volatile List<Predicate<BlockState>> blacklistMatchers = Collections.emptyList();

    public static void load() {
        INSTANCE.load();
        rebuildMatchers();
    }

    public static void save() {
        INSTANCE.save();
        rebuildMatchers();
    }

    private static void rebuildMatchers() {
        whitelistMatchers = compile(INSTANCE.getConfig().whitelist);
        blacklistMatchers = compile(INSTANCE.getConfig().blacklist);
    }

    private static List<Predicate<BlockState>> compile(List<String> entries) {
        List<Predicate<BlockState>> compiled = new ArrayList<>();
        for (String entry : entries) {
            Predicate<BlockState> matcher = BlockFilterEntry.parse(entry);
            if (matcher != null) compiled.add(matcher);
        }
        return compiled;
    }

    public static boolean isBlockAllowed(BlockState state) {
        for (Predicate<BlockState> matcher : blacklistMatchers) {
            if (matcher.test(state)) return false;
        }
        if (whitelistMatchers.isEmpty()) return true;
        for (Predicate<BlockState> matcher : whitelistMatchers) {
            if (matcher.test(state)) return true;
        }
        return false;
    }

    public static SnowCheckerMode getCheckerMode() {
        return INSTANCE.getConfig().checkerMode;
    }

    public static boolean isReduceZFightingEnabled() {
        return INSTANCE.getConfig().reduceZFighting;
    }

    public static boolean isRaySearchEnabled() {
        return INSTANCE.getConfig().useRaySearch;
    }

    public static int getMaxHorizontalDistance() {
        return INSTANCE.getConfig().maxHorizontalDistance;
    }

    public static int getMaxVerticalDistance() {
        return INSTANCE.getConfig().maxVerticalDistance;
    }

    public static Screen buildScreen(Screen parent) {
        YetAnotherConfigLib yacl = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("config.improvedsnowlayers.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.improvedsnowlayers.category.general"))
                        .tooltip(Component.translatable("config.improvedsnowlayers.category.general.tooltip"))
                        .option(Option.<SnowCheckerMode>createBuilder(SnowCheckerMode.class)
                                .name(Component.translatable("config.improvedsnowlayers.option.checkerMode"))
                                .description(OptionDescription.of(Component.translatable("config.improvedsnowlayers.option.checkerMode.description")))
                                .binding(SnowCheckerMode.THREE_OF_FOUR,
                                        () -> INSTANCE.getConfig().checkerMode,
                                        mode -> INSTANCE.getConfig().checkerMode = mode)
                                .controller(opt -> EnumControllerBuilder.create(opt)
                                        .enumClass(SnowCheckerMode.class)
                                        .valueFormatter(mode -> Component.translatable("config.improvedsnowlayers.checkerMode." + mode.name())))
                                .flag(OptionFlag.RELOAD_CHUNKS)
                                .build())
                        .option(Option.<Boolean>createBuilder(Boolean.class)
                                .name(Component.translatable("config.improvedsnowlayers.option.useRaySearch"))
                                .description(OptionDescription.of(Component.translatable("config.improvedsnowlayers.option.useRaySearch.description")))
                                .binding(false,
                                        () -> INSTANCE.getConfig().useRaySearch,
                                        value -> INSTANCE.getConfig().useRaySearch = value)
                                .controller(TickBoxControllerBuilder::create)
                                .flag(OptionFlag.RELOAD_CHUNKS)
                                .build())
                        .option(Option.<Integer>createBuilder(Integer.class)
                                .name(Component.translatable("config.improvedsnowlayers.option.maxHorizontalDistance"))
                                .description(OptionDescription.of(Component.translatable("config.improvedsnowlayers.option.maxHorizontalDistance.description")))
                                .binding(1,
                                        () -> INSTANCE.getConfig().maxHorizontalDistance,
                                        value -> INSTANCE.getConfig().maxHorizontalDistance = value)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 8).step(1))
                                .flag(OptionFlag.RELOAD_CHUNKS)
                                .build())
                        .option(Option.<Integer>createBuilder(Integer.class)
                                .name(Component.translatable("config.improvedsnowlayers.option.maxVerticalDistance"))
                                .description(OptionDescription.of(Component.translatable("config.improvedsnowlayers.option.maxVerticalDistance.description")))
                                .binding(2,
                                        () -> INSTANCE.getConfig().maxVerticalDistance,
                                        value -> INSTANCE.getConfig().maxVerticalDistance = value)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 8).step(1))
                                .flag(OptionFlag.RELOAD_CHUNKS)
                                .build())
                        .option(Option.<Boolean>createBuilder(Boolean.class)
                                .name(Component.translatable("config.improvedsnowlayers.option.reduceZFighting"))
                                .description(OptionDescription.of(Component.translatable("config.improvedsnowlayers.option.reduceZFighting.description")))
                                .binding(true,
                                        () -> INSTANCE.getConfig().reduceZFighting,
                                        value -> INSTANCE.getConfig().reduceZFighting = value)
                                .controller(TickBoxControllerBuilder::create)
                                .flag(OptionFlag.RELOAD_CHUNKS)
                                .build())
                        .group(ListOption.<String>createBuilder(String.class)
                                .name(Component.translatable("config.improvedsnowlayers.option.whitelist"))
                                .description(OptionDescription.of(Component.translatable("config.improvedsnowlayers.option.whitelist.description")))
                                .binding(new ArrayList<>(),
                                        () -> INSTANCE.getConfig().whitelist,
                                        list -> INSTANCE.getConfig().whitelist = list)
                                .controller(StringControllerBuilder::create)
                                .initial("")
                                .insertEntriesAtEnd(true)
                                .flag(OptionFlag.RELOAD_CHUNKS)
                                .build())
                        .group(ListOption.<String>createBuilder(String.class)
                                .name(Component.translatable("config.improvedsnowlayers.option.blacklist"))
                                .description(OptionDescription.of(Component.translatable("config.improvedsnowlayers.option.blacklist.description")))
                                .binding(new ArrayList<>(),
                                        () -> INSTANCE.getConfig().blacklist,
                                        list -> INSTANCE.getConfig().blacklist = list)
                                .controller(StringControllerBuilder::create)
                                .initial("")
                                .insertEntriesAtEnd(true)
                                .flag(OptionFlag.RELOAD_CHUNKS)
                                .build())
                        .build())
                .save(ImprovedSnowLayersConfig::save)
                .build();

        return yacl.generateScreen(parent);
    }
}
