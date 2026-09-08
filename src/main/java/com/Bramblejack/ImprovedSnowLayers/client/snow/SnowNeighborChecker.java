package com.Bramblejack.ImprovedSnowLayers.client.snow;

import com.Bramblejack.ImprovedSnowLayers.config.ImprovedSnowLayersConfig;
import com.Bramblejack.ImprovedSnowLayers.config.SnowCheckerMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumMap;
import java.util.Map;

public class SnowNeighborChecker {

    private static final class ScanResult {
        final int snowCount;
        final int airCount;
        final BlockPos firstSnowSource;

        ScanResult(int snowCount, int airCount, BlockPos firstSnowSource) {
            this.snowCount = snowCount;
            this.airCount = airCount;
            this.firstSnowSource = firstSnowSource;
        }
    }

    public static boolean isEligibleForPhantomLayer(BlockGetter level, BlockPos neighborPos,
                                                     BlockState neighborState) {
        if (!passesBasicFilters(level, neighborPos, neighborState)) return false;
        return decide(scan(level, neighborPos));
    }

    public static boolean shouldExtendInto(BlockGetter level, BlockPos snowPos,
                                           BlockPos neighborPos, BlockState neighborState) {
        if (!passesBasicFilters(level, neighborPos, neighborState)) return false;

        ScanResult result = scan(level, neighborPos);
        if (!decide(result)) return false;

        return snowPos.equals(result.firstSnowSource);
    }

    public static boolean hasPhantomSnowAbove(BlockGetter level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        return isEligibleForPhantomLayer(level, abovePos, aboveState);
    }

    private static boolean passesBasicFilters(BlockGetter level, BlockPos neighborPos, BlockState neighborState) {
        if (neighborState.isAir()) return false;
        if (neighborState.getBlock() instanceof SnowLayerBlock) return false;

        try {
            if (Block.isShapeFullBlock(neighborState.getCollisionShape(level, neighborPos)))
                return false;
        } catch (Exception e) {
            return false;
        }

        return ImprovedSnowLayersConfig.isBlockAllowed(neighborState);
    }

    private static boolean decide(ScanResult scan) {
        return switch (ImprovedSnowLayersConfig.getCheckerMode()) {
            case THREE_OF_FOUR -> scan.snowCount >= 3;
            case PREFER_SNOW -> (scan.snowCount != 0 || scan.airCount != 0) && scan.snowCount >= scan.airCount;
            case PREFER_AIR -> scan.snowCount > scan.airCount;
            case ALL_SIDES -> scan.snowCount == 4;
        };
    }

    private static ScanResult scan(BlockGetter level, BlockPos neighborPos) {
        return ImprovedSnowLayersConfig.isRaySearchEnabled()
                ? raySearchScan(level, neighborPos,
                        ImprovedSnowLayersConfig.getMaxHorizontalDistance(),
                        ImprovedSnowLayersConfig.getMaxVerticalDistance())
                : adjacentScan(level, neighborPos);
    }

    private static ScanResult adjacentScan(BlockGetter level, BlockPos neighborPos) {
        int snowCount = 0;
        int airCount = 0;
        BlockPos firstSnowSource = null;

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos candidate = neighborPos.relative(dir);
            BlockState candidateState = level.getBlockState(candidate);
            if (candidateState.is(BlockTags.SNOW)) {
                snowCount++;
                if (firstSnowSource == null) firstSnowSource = candidate;
            } else if (candidateState.isAir()) {
                airCount++;
            }
        }

        return new ScanResult(snowCount, airCount, firstSnowSource);
    }

    private static ScanResult raySearchScan(BlockGetter level, BlockPos neighborPos,
                                             int maxHorizontalDistance, int maxVerticalDistance) {
        Map<Direction, BlockPos.MutableBlockPos> cursors = new EnumMap<>(Direction.class);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            cursors.put(dir, new BlockPos.MutableBlockPos(
                    neighborPos.getX(), neighborPos.getY() - 1, neighborPos.getZ()));
        }

        int snowCount = 0;
        int airCount = 0;
        BlockPos firstSnowSource = null;

        for (int ring = 0; ring < maxHorizontalDistance && !cursors.isEmpty(); ring++) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos.MutableBlockPos cursor = cursors.get(dir);
                if (cursor == null) continue;

                RayStep step = stepDirection(level, cursor, dir, maxVerticalDistance);
                if (step == RayStep.CONTINUE) continue;
                if (step == RayStep.SNOW) {
                    snowCount++;
                    if (firstSnowSource == null) firstSnowSource = cursor.above().immutable();
                } else if (step == RayStep.AIR) {
                    airCount++;
                }
                cursors.remove(dir);
            }

            ScanResult candidate = new ScanResult(snowCount, airCount, firstSnowSource);
            if (decide(candidate)) return candidate;
        }

        return new ScanResult(snowCount, airCount, firstSnowSource);
    }

    private enum RayStep { SNOW, AIR, CONTINUE, GIVE_UP }

    private static RayStep stepDirection(BlockGetter level, BlockPos.MutableBlockPos cursor,
                                          Direction dir, int maxVerticalDistance) {
        cursor.move(dir);
        int depthBudget = maxVerticalDistance;

        while (!isFullBlock(level, cursor)) {
            if (depthBudget-- <= 0) return RayStep.GIVE_UP;
            cursor.move(Direction.DOWN);
        }
        while (isFullBlock(level, cursor.above())) {
            if (depthBudget-- <= 0) return RayStep.GIVE_UP;
            cursor.move(Direction.UP);
        }

        BlockState above = level.getBlockState(cursor.above());
        if (above.is(BlockTags.SNOW)) return RayStep.SNOW;
        if (above.isAir()) return RayStep.AIR;
        return RayStep.CONTINUE;
    }

    private static boolean isFullBlock(BlockGetter level, BlockPos pos) {
        try {
            BlockState state = level.getBlockState(pos);
            return Block.isShapeFullBlock(state.getCollisionShape(level, pos));
        } catch (Exception e) {
            return false;
        }
    }

    public static int averageSnowHeight(BlockGetter level, BlockPos neighborPos) {
        int totalHeight = 0;
        int count = 0;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockState candidateState = level.getBlockState(neighborPos.relative(dir));
            if (!candidateState.is(BlockTags.SNOW)) continue;
            totalHeight += snowHeightOf(candidateState);
            count++;
        }

        if (count == 0) return 1;
        return Math.max(1, Math.min(8, totalHeight / count));
    }

    private static int snowHeightOf(BlockState state) {
        if (state.getBlock() instanceof SnowLayerBlock && state.hasProperty(SnowLayerBlock.LAYERS)) {
            return state.getValue(SnowLayerBlock.LAYERS);
        }
        return 8;
    }
}
