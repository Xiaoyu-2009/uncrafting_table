package com.xiaoyu2009.uncraftingtable.init;

import com.xiaoyu2009.uncraftingtable.UncraftingTableMod;
import com.xiaoyu2009.uncraftingtable.inventory.UncraftingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, UncraftingTableMod.MODID);

    public static final RegistryObject<MenuType<UncraftingMenu>> UNCRAFTING = MENU_TYPES.register("uncrafting",
        () -> IForgeMenuType.create(UncraftingMenu::fromNetwork));
}