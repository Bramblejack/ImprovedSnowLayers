package com.Bramblejack.ImprovedSnowLayers.mixin.embeddium;

import com.Bramblejack.ImprovedSnowLayers.client.snow.PhantomSnowModels;
import com.Bramblejack.ImprovedSnowLayers.client.snow.SnowNeighborChecker;
import com.Bramblejack.ImprovedSnowLayers.config.ImprovedSnowLayersConfig;
import com.llamalad7.mixinextras.sugar.Local;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    targets = "me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask",
    remap = false
)
public class ChunkBuilderMeshingTaskSnowWrapMixin {

    @Inject(
        method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
        at = @At(
            value = "INVOKE",
            target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;renderModel(Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext;Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildBuffers;)V",
            shift = At.Shift.AFTER
        ),
        remap = false
    )
    private void onAfterRenderModel(
            CallbackInfoReturnable<ChunkBuildOutput> cir,
            @Local BlockRenderCache cache,
            @Local WorldSlice slice,
            @Local BlockRenderContext context,
            @Local ChunkBuildBuffers buffers,
            @Local(ordinal = 0) BlockPos.MutableBlockPos blockPos,
            @Local(ordinal = 1) BlockPos.MutableBlockPos modelOffset
    ) {
        if (!(context.state().getBlock() instanceof SnowLayerBlock)) return;

        BlockAndTintGetter level = slice;
        BlockState snowState = context.state();
        BakedModel snowModel = context.model();
        long snowSeed = context.seed();
        ModelData snowModelData = context.modelData();
        net.minecraft.client.renderer.RenderType snowRenderType = context.renderLayer();

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = blockPos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (!SnowNeighborChecker.shouldExtendInto(level, blockPos, neighborPos, neighborState))
                continue;

            BlockPos.MutableBlockPos shiftedOffset = new BlockPos.MutableBlockPos(
                    modelOffset.getX() + dir.getStepX(),
                    modelOffset.getY(),
                    modelOffset.getZ() + dir.getStepZ()
            );

            BlockState renderState = snowState;
            BakedModel renderModel = snowModel;
            if (snowState.getBlock() instanceof SnowLayerBlock && snowState.hasProperty(SnowLayerBlock.LAYERS)) {
                int averagedLayers = SnowNeighborChecker.averageSnowHeight(level, neighborPos);
                if (averagedLayers != snowState.getValue(SnowLayerBlock.LAYERS)) {
                    renderState = snowState.setValue(SnowLayerBlock.LAYERS, averagedLayers);
                    renderModel = cache.getBlockModels().getBlockModel(renderState);
                }
            }

            if (ImprovedSnowLayersConfig.isReduceZFightingEnabled()) {
                BakedModel widenedModel = PhantomSnowModels.getWidenedModel(renderState.getValue(SnowLayerBlock.LAYERS));
                if (widenedModel != null) {
                    renderModel = widenedModel;
                }
            }

            context.update(neighborPos, shiftedOffset, renderState, renderModel,
                    snowSeed, snowModelData, snowRenderType);
            cache.getBlockRenderer().renderModel(context, buffers);
        }

        context.update(blockPos, modelOffset, snowState, snowModel,
                snowSeed, snowModelData, snowRenderType);
    }
}
