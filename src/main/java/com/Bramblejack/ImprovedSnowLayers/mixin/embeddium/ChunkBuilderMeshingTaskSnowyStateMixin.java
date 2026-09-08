package com.Bramblejack.ImprovedSnowLayers.mixin.embeddium;

import com.Bramblejack.ImprovedSnowLayers.client.snow.SnowNeighborChecker;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    targets = "me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask",
    remap = true
)
public class ChunkBuilderMeshingTaskSnowyStateMixin {

    @Inject(
        method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/BlockPos$MutableBlockPos;set(III)Lnet/minecraft/core/BlockPos$MutableBlockPos;",
            ordinal = 0,
            shift = At.Shift.AFTER
        ),
        remap = true
    )
    private void improvedsnowlayers$applySnowyVisual(
            CallbackInfoReturnable<ChunkBuildOutput> cir,
            @Local WorldSlice slice,
            @Local(ordinal = 0) BlockPos.MutableBlockPos pos,
            @Local LocalRef<BlockState> stateRef
    ) {
        BlockState state = stateRef.get();

        if (!(state.getBlock() instanceof SnowyDirtBlock)) return;
        if (state.getValue(BlockStateProperties.SNOWY)) return;

        if (SnowNeighborChecker.hasPhantomSnowAbove(slice, pos)) {
            stateRef.set(state.setValue(BlockStateProperties.SNOWY, Boolean.TRUE));
        }
    }
}
