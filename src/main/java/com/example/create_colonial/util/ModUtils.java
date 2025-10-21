package com.example.create_colonial.util;

import com.example.create_colonial.CreateColonial;
import com.example.create_colonial.config.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ModUtils {
    
    public enum MessageType {
        INFO("§b"),      // Aqua
        SUCCESS("§a"),   // Green
        WARNING("§e"),   // Yellow
        ERROR("§c");     // Red
        
        private final String color;
        
        MessageType(String color) {
            this.color = color;
        }
        
        public String getColor() {
            return color;
        }
    }
    
    public static void sendMessageToPlayer(Player player, String message, MessageType type) {
        player.sendSystemMessage(Component.literal(type.getColor() + message));
    }
    
    public static void debugLog(String message, Object... args) {
        if (Config.isDebugLoggingEnabled()) {
            CreateColonial.LOGGER.info("[DEBUG] " + message, args);
        }
    }
    
    public static boolean isClipboardEnabled() {
        return Config.isClipboardEnabled();
    }
    
    public static boolean isBlueprintSupportEnabled() {
        return Config.isBlueprintSupportEnabled();
    }
    
    public static boolean isBuilderAIEnabled() {
        return Config.isBuilderAIEnabled();
    }
}
