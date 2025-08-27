package com.xiaoyu2009.uncraftingtable.init;

import com.xiaoyu2009.uncraftingtable.UncraftingTableMod;
import com.xiaoyu2009.uncraftingtable.block.UncraftingTableBlock;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = UncraftingTableMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, UncraftingTableMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, UncraftingTableMod.MODID);

    public static final RegistryObject<Block> UNCRAFTING_TABLE = BLOCKS.register("uncrafting_table", 
        () -> new UncraftingTableBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD)
            .strength(2.5F)
            .sound(SoundType.WOOD)));

    public static final RegistryObject<Item> UNCRAFTING_TABLE_ITEM = ITEMS.register("uncrafting_table",
        () -> new BlockItem(UNCRAFTING_TABLE.get(), new Item.Properties()));

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(UNCRAFTING_TABLE_ITEM);
        }
    }
}