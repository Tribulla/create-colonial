package com.example.create_colonial;

import com.example.create_colonial.config.Config;
import com.example.create_colonial.network.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod("create_colonial")
public class CreateColonial {
    public static final String MOD_ID = "create_colonial";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateColonial() {
        IEventBus modEventBus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        
        // Register config
        Config.register();
        
        // Register network handler
        NetworkHandler.register();
        
        // Register mod for setup event
        modEventBus.addListener(this::setup);
        
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Create Colonial Setup");
        LOGGER.info("Blueprint Support: {}", Config.isBlueprintSupportEnabled());
        LOGGER.info("Clipboard Integration: {}", Config.isClipboardEnabled());
        LOGGER.info("Builder AI Extensions: {}", Config.isBuilderAIEnabled());
    }
}
