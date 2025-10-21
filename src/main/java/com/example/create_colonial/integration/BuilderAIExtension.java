package com.example.create_colonial.integration;

import com.example.create_colonial.CreateColonial;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Provides helper methods for MineColonies builders to work with Create blocks
 */
public class BuilderAIExtension {

    /**
     * Checks if a block requires special handling due to Create mechanics
     */
    public static boolean requiresSpecialHandling(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof KineticBlockEntity;
    }

    /**
     * Validates that a Create block is placed correctly (rotation, connections, etc.)
     */
    public static boolean validatePlacement(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        
        if (blockEntity instanceof KineticBlockEntity kineticBE) {
            // Check if the kinetic block is properly connected
            // This is a simplified check - actual implementation would verify:
            // - Rotation is correct
            // - Connected to power source if needed
            // - Not overstressed
            
            CreateColonial.LOGGER.debug("Validating Create block placement at {}", pos);
            return true;
        }
        
        return true;
    }

    /**
     * Gets the priority for building this block (Create blocks may need to be built in order)
     */
    public static int getBuildPriority(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        
        if (blockEntity instanceof KineticBlockEntity) {
            // Create blocks that generate power should be built first
            // Blocks that consume power should be built after
            // For now, return a default medium priority
            return 50;
        }
        
        return 0;
    }

    /**
     * Called after a Create block is placed to finalize its setup
     */
    public static void finalizeBlockPlacement(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        
        if (blockEntity instanceof KineticBlockEntity kineticBE) {
            // Ensure the block entity is properly initialized
            blockEntity.setChanged();
            CreateColonial.LOGGER.debug("Finalized Create block at {}", pos);
        }
    }
}
