package com.xiaoyu2009.uncraftingtable;

import com.xiaoyu2009.uncraftingtable.config.UncraftingConfig;
import com.xiaoyu2009.uncraftingtable.init.ModBlocks;
import com.xiaoyu2009.uncraftingtable.init.ModMenuTypes;
import com.xiaoyu2009.uncraftingtable.init.ModRecipes;
import com.xiaoyu2009.uncraftingtable.init.ModSounds;
import com.xiaoyu2009.uncraftingtable.network.NetworkHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(UncraftingTableMod.MODID)
public class UncraftingTableMod {
    public static final String MODID = "uncrafting_table";

    public UncraftingTableMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModRecipes.RECIPE_TYPES.register(modEventBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, UncraftingConfig.SPEC);

        NetworkHandler.register();
    }

    public static ResourceLocation prefix(String name) {
        return new ResourceLocation(MODID, name);
    }

    public static ResourceLocation getGuiTexture(String name) {
        return prefix("textures/gui/" + name);
    }
}