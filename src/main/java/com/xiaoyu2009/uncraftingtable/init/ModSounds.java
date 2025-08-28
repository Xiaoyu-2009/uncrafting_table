package com.xiaoyu2009.uncraftingtable.init;

import com.xiaoyu2009.uncraftingtable.UncraftingTableMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, UncraftingTableMod.MODID);

    public static final RegistryObject<SoundEvent> UNCRAFTING_TABLE_ACTIVATE = SOUNDS.register("uncrafting_table_activate",
        () -> new SoundEvent(UncraftingTableMod.prefix("uncrafting_table_activate")));
}