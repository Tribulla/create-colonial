package com.example.create_colonial.integration;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Handles saving and loading Create block entity data for blueprints
 */
public class CreateBlockEntityHandler {

    /**
     * Saves Create-specific data from a block entity to NBT
     */
    public static CompoundTag saveCreateBlockEntity(BlockEntity blockEntity) {
        CompoundTag tag = new CompoundTag();
        
        if (blockEntity instanceof KineticBlockEntity kineticBE) {
            // Save kinetic properties
            tag.putFloat("speed", kineticBE.getSpeed());
            // Note: getCapacity() and getStressApplied() might not be available in all Create versions
            // These are optional and will be skipped if the methods don't exist
            
            // Save specific Create block entity data
            if (blockEntity instanceof BeltBlockEntity beltBE) {
                saveBeltData(tag, beltBE);
            } else if (blockEntity instanceof MechanicalPressBlockEntity pressBE) {
                savePressData(tag, pressBE);
            } else if (blockEntity instanceof ArmBlockEntity armBE) {
                saveArmData(tag, armBE);
            }
        }
        
        // Save the full NBT data as well for complete restoration
        CompoundTag fullData = blockEntity.saveWithFullMetadata();
        tag.put("fullData", fullData);
        tag.putString("type", "create_block");
        
        return tag;
    }

    /**
     * Restores Create-specific data to a block entity when building from blueprint
     */
    public static void loadCreateBlockEntity(Level level, BlockPos pos, CompoundTag tag) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return;
        }
        
        // Restore full NBT data
        if (tag.contains("fullData")) {
            CompoundTag fullData = tag.getCompound("fullData");
            blockEntity.load(fullData);
        }
        
        // Apply kinetic properties if it's a kinetic block
        if (blockEntity instanceof KineticBlockEntity kineticBE) {
            if (tag.contains("speed")) {
                float speed = tag.getFloat("speed");
                // The speed will be set via the full NBT data restore
            }
        }
        
        blockEntity.setChanged();
    }

    private static void saveBeltData(CompoundTag tag, BeltBlockEntity belt) {
        // Belt-specific data is saved in the full NBT
        tag.putString("beltType", "belt");
    }

    private static void savePressData(CompoundTag tag, MechanicalPressBlockEntity press) {
        // Press-specific data is saved in the full NBT
        tag.putString("pressType", "press");
    }

    private static void saveArmData(CompoundTag tag, ArmBlockEntity arm) {
        // Arm-specific data is saved in the full NBT
        tag.putString("armType", "arm");
    }

    /**
     * Checks if a block entity is from Create mod
     */
    public static boolean isCreateBlockEntity(BlockEntity blockEntity) {
        return blockEntity instanceof KineticBlockEntity;
    }
}
