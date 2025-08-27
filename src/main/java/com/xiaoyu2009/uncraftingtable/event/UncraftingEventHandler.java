package com.xiaoyu2009.uncraftingtable.event;

import com.xiaoyu2009.uncraftingtable.inventory.RecipeCacheManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.event.TagsUpdatedEvent;

@Mod.EventBusSubscriber(modid = "uncrafting_table")
public class UncraftingEventHandler {
    
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            RecipeCacheManager.getInstance(level).loadRecipes();
        }
    }
    
    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        RecipeCacheManager.clearAll();
    }
    
    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        if ("uncrafting_table".equals(event.getConfig().getModId())) {
            RecipeCacheManager.clearAll();
        }
    }
}