package com.xiaoyu2009.uncraftingtable.inventory.slot;

import com.xiaoyu2009.uncraftingtable.config.UncraftingConfig;
import com.xiaoyu2009.uncraftingtable.inventory.UncraftingMenu;
import com.xiaoyu2009.uncraftingtable.inventory.UncraftingContainer;
import com.xiaoyu2009.uncraftingtable.item.recipe.UncraftingRecipe;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class UncraftingSlot extends Slot {

    protected final Player player;
    protected final Container inputSlot;
    protected final UncraftingContainer uncraftingMatrix;
    protected final Container assemblyMatrix;

    public UncraftingSlot(Player player, Container inputSlot, UncraftingContainer uncraftingMatrix, Container assemblyMatrix, int slotNum, int x, int y) {
        super(uncraftingMatrix, slotNum, x, y);
        this.player = player;
        this.inputSlot = inputSlot;
        this.uncraftingMatrix = uncraftingMatrix;
        this.assemblyMatrix = assemblyMatrix;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        if (!this.assemblyMatrix.isEmpty()) {
            return false;
        }

        if (UncraftingMenu.isMarked(this.getItem())) {
            return false;
        }

        if (UncraftingConfig.disableUncraftingOnly.get() && !(this.uncraftingMatrix.menu.storedGhostRecipe instanceof UncraftingRecipe)) {
            return false;
        }

        return this.uncraftingMatrix.uncraftingCost <= player.experienceLevel || player.getAbilities().instabuild;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        if (this.uncraftingMatrix.uncraftingCost > 0) {
            this.player.giveExperienceLevels(-this.uncraftingMatrix.uncraftingCost);
        }

        for (int i = 0; i < 9; i++) {
            ItemStack transferStack = this.uncraftingMatrix.getItem(i);
            if (!transferStack.isEmpty() && !UncraftingMenu.isMarked(transferStack)) {
                this.assemblyMatrix.setItem(i, transferStack.copy());
            }
        }

        ItemStack inputStack = this.inputSlot.getItem(0);
        if (!inputStack.isEmpty()) {
            this.inputSlot.removeItem(0, this.uncraftingMatrix.numberOfInputItems);
        }

        super.onTake(player, stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isActive() {
        return false;
    }
}