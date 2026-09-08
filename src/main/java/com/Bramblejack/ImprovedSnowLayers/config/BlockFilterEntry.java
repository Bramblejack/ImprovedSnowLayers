package com.Bramblejack.ImprovedSnowLayers.config;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Predicate;

public final class BlockFilterEntry {

    private BlockFilterEntry() {}

    public static Predicate<BlockState> parse(String entry) {
        if (entry == null) return null;
        String trimmed = entry.trim();
        if (trimmed.isEmpty()) return null;

        boolean isTag = trimmed.startsWith("#");
        String idPart = isTag ? trimmed.substring(1) : trimmed;

        ResourceLocation id = ResourceLocation.tryParse(idPart);
        if (id == null) return null;

        if (isTag) {
            TagKey<Block> tag = TagKey.create(Registries.BLOCK, id);
            return state -> state.is(tag);
        }

        if (!ForgeRegistries.BLOCKS.containsKey(id)) return null;
        Block block = ForgeRegistries.BLOCKS.getValue(id);
        return state -> state.is(block);
    }
}
