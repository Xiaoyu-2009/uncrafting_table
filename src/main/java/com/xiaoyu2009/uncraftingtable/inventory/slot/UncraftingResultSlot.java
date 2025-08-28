package com.xiaoyu2009.uncraftingtable.inventory.slot;

import com.xiaoyu2009.uncraftingtable.inventory.UncraftingContainer;
import com.xiaoyu2009.uncraftingtable.inventory.UncraftingMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.HashMap;
import java.util.Map;

public class UncraftingResultSlot extends ResultSlot {

    private final Player player;
    private final Container inputSlot;
    private final UncraftingContainer uncraftingMatrix;
    private final CraftingContainer assemblyMatrix;
    private final Map<Integer, ItemStack> tempRemainderMap;

    public UncraftingResultSlot(Player player, Container input, Container uncraftingMatrix, Container assemblyMatrix, Container result, int slotIndex, int x, int y) {
        super(player, (CraftingContainer) assemblyMatrix, result, slotIndex, x, y);
        this.player = player;
        this.inputSlot = input;
        this.uncraftingMatrix = (UncraftingContainer) uncraftingMatrix;
        this.assemblyMatrix = (CraftingContainer) assemblyMatrix;
        this.tempRemainderMap = new HashMap<>();
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        boolean combined = true;

        this.tempRemainderMap.clear();

        for (Recipe<CraftingContainer> recipe : player.level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, this.assemblyMatrix, this.player.level)) {
            if (ItemStack.isSameItemSameTags(recipe.getResultItem(), stack)) {
                combined = false;
                break;
            }
        }

        if (combined) {
            if (this.uncraftingMatrix.recraftingCost > 0) {
                this.player.giveExperienceLevels(-this.uncraftingMatrix.recraftingCost);
            }

            for (int i = 0; i < this.uncraftingMatrix.getContainerSize(); i++) {
                if (this.assemblyMatrix.getItem(i).isEmpty()) {
                    if (!UncraftingMenu.isMarked(this.uncraftingMatrix.getItem(i))) {
                        this.uncraftingMatrix.setItem(i, ItemStack.EMPTY);
                    } else {
                        this.tempRemainderMap.put(i, this.uncraftingMatrix.getItem(i));
                    }
                }
            }
            this.inputSlot.removeItem(0, this.uncraftingMatrix.numberOfInputItems);
        }

        this.checkTakeAchievements(stack);

        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(player);
        NonNullList<ItemStack> remainingItems = player.level.getRecipeManager().getRemainingItemsFor(RecipeType.CRAFTING, this.assemblyMatrix, player.level);
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);

        for(int i = 0; i < remainingItems.size(); ++i) {
            ItemStack currentStack = this.assemblyMatrix.getItem(i);
            ItemStack remainingStack = remainingItems.get(i);
            if (!currentStack.isEmpty()) {
                this.assemblyMatrix.removeItem(i, 1);
                currentStack = this.assemblyMatrix.getItem(i);
            }

            if (!remainingStack.isEmpty()) {
                if (currentStack.isEmpty()) {
                    this.assemblyMatrix.setItem(i, remainingStack);
                } else if (!ItemStack.isSameItemSameTags(currentStack, remainingStack) && !this.player.getInventory().add(remainingStack)) {
                    this.player.drop(remainingStack, false);
                }
            }
        }
        
        if (!this.tempRemainderMap.isEmpty()) {
            this.tempRemainderMap.forEach(this.assemblyMatrix::setItem);
        }
    }
}