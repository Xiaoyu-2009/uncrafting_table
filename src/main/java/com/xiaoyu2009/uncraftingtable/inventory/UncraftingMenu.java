package com.xiaoyu2009.uncraftingtable.inventory;

import java.util.Arrays;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.xiaoyu2009.uncraftingtable.config.UncraftingConfig;
import com.xiaoyu2009.uncraftingtable.init.ModBlocks;
import com.xiaoyu2009.uncraftingtable.init.ModMenuTypes;
import com.xiaoyu2009.uncraftingtable.inventory.slot.AssemblySlot;
import com.xiaoyu2009.uncraftingtable.inventory.slot.UncraftingResultSlot;
import com.xiaoyu2009.uncraftingtable.inventory.slot.UncraftingSlot;
import com.xiaoyu2009.uncraftingtable.item.recipe.UncraftingRecipe;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.IShapedRecipe;

public class UncraftingMenu extends AbstractContainerMenu {

    private static final String TAG_MARKER = "UncraftingTableMarker";

    private final UncraftingContainer uncraftingMatrix = new UncraftingContainer(this);
    public final CraftingContainer assemblyMatrix = new CraftingContainer(this, 3, 3) {
        @Override
        public void setChanged() {
            UncraftingMenu.this.slotsChanged(this);
        }
    };
    private final CraftingContainer combineMatrix = new CraftingContainer(this, 3, 3) {
        @Override
        public void setChanged() {
            UncraftingMenu.this.slotsChanged(this);
        }
    };

    public final Container tinkerInput = new UncraftingInputContainer(this);
    private final ResultContainer tinkerResult = new ResultContainer();

    private final ContainerLevelAccess positionData;
    private final Level level;
    private final Player player;

    public int unrecipeInCycle = 0;
    public int ingredientsInCycle = 0;
    public int recipeInCycle = 0;

    @Nullable
    public Recipe<?> storedGhostRecipe = null;

    public static UncraftingMenu fromNetwork(int id, Inventory inventory, FriendlyByteBuf buffer) {
        return new UncraftingMenu(id, inventory, inventory.player.level, ContainerLevelAccess.NULL);
    }

    public UncraftingMenu(int id, Inventory inventory, Level level, ContainerLevelAccess positionData) {
        super(ModMenuTypes.UNCRAFTING.get(), id);

        this.positionData = positionData;
        this.level = level;
        this.player = inventory.player;

        this.addSlot(new Slot(this.tinkerInput, 0, 13, 35));
        this.addSlot(new UncraftingResultSlot(inventory.player, this.tinkerInput, this.uncraftingMatrix, this.assemblyMatrix, this.tinkerResult, 0, 147, 35));

        int invX;
        int invY;

        for (invX = 0; invX < 3; ++invX) {
            for (invY = 0; invY < 3; ++invY) {
                this.addSlot(new UncraftingSlot(inventory.player, this.tinkerInput, this.uncraftingMatrix, this.assemblyMatrix, invY + invX * 3, 62 + invY * 18, 17 + invX * 18));
            }
        }
        for (invX = 0; invX < 3; ++invX) {
            for (invY = 0; invY < 3; ++invY) {
                this.addSlot(new AssemblySlot(this.assemblyMatrix, invY + invX * 3, 62 + invY * 18, 17 + invX * 18));
            }
        }

        for (invX = 0; invX < 3; ++invX) {
            for (invY = 0; invY < 9; ++invY) {
                this.addSlot(new Slot(inventory, invY + invX * 9 + 9, 8 + invY * 18, 84 + invX * 18));
            }
        }

        for (invX = 0; invX < 9; ++invX) {
            this.addSlot(new Slot(inventory, invX, 8 + invX * 18, 142));
        }

        this.slotsChanged(this.assemblyMatrix);
    }

    @Override
    public void slotsChanged(Container inventory) {
        if (inventory == this.tinkerInput) {
            this.uncraftingMatrix.clearContent();

            ItemStack inputStack = tinkerInput.getItem(0);
            Recipe<?>[] recipes = getRecipesFor(inputStack, this.level);

            int size = recipes.length;

            if (size > 0) {
                Recipe<?> recipe = recipes[Math.floorMod(this.unrecipeInCycle, size)];
                this.storedGhostRecipe = recipe;
                ItemStack[] recipeItems = this.getIngredients(recipe);

                if (recipe instanceof IShapedRecipe<?> rec) {
                    int recipeWidth = rec.getRecipeWidth();
                    int recipeHeight = rec.getRecipeHeight();

                    for (int invY = 0; invY < recipeHeight; invY++) {
                        for (int invX = 0; invX < recipeWidth; invX++) {
                            int index = invX + invY * recipeWidth;
                            if (index >= recipeItems.length) continue;

                            ItemStack ingredient = normalizeIngredient(recipeItems[index].copy());
                            this.uncraftingMatrix.setItem(invX + invY * 3, ingredient);
                        }
                    }
                } else {
                    for (int i = 0; i < this.uncraftingMatrix.getContainerSize(); i++) {
                        if (i < recipeItems.length) {
                            ItemStack ingredient = normalizeIngredient(recipeItems[i].copy());
                            this.uncraftingMatrix.setItem(i, ingredient);
                        }
                    }
                }

                if (inputStack.isDamaged()) {
                    int damagedParts = this.countDamagedParts(inputStack);

                    for (int i = 0; i < 9 && damagedParts > 0; i++) {
                        ItemStack stack = this.uncraftingMatrix.getItem(i);
                        if (isDamageableComponent(stack)) {
                            markStack(stack);
                            damagedParts--;
                        }
                    }
                }

                for (int i = 0; i < 9; i++) {
                    ItemStack ingredient = this.uncraftingMatrix.getItem(i);
                    if (isIngredientProblematic(ingredient)) {
                        markStack(ingredient);
                    }
                }

                this.uncraftingMatrix.numberOfInputItems = recipe instanceof UncraftingRecipe uncraftingRecipe ? uncraftingRecipe.count() : recipe.getResultItem().getCount();
                this.uncraftingMatrix.uncraftingCost = this.calculateUncraftingCost();
                this.uncraftingMatrix.recraftingCost = 0;

            } else {
                this.storedGhostRecipe = null;
                this.uncraftingMatrix.numberOfInputItems = 0;
                this.uncraftingMatrix.uncraftingCost = 0;
            }
        }
        
        if (inventory == this.assemblyMatrix || inventory == this.tinkerInput) {
            if (this.tinkerInput.isEmpty()) {
                this.chooseRecipe(this.assemblyMatrix);
            } else {
                this.tinkerResult.setItem(0, ItemStack.EMPTY);
                this.uncraftingMatrix.uncraftingCost = this.calculateUncraftingCost();
            }
            this.uncraftingMatrix.recraftingCost = 0;
        }

        if (inventory != this.combineMatrix && !this.uncraftingMatrix.isEmpty() && !this.assemblyMatrix.isEmpty()) {
            for (int i = 0; i < 9; i++) {
                ItemStack assembly = this.assemblyMatrix.getItem(i);
                ItemStack uncrafting = this.uncraftingMatrix.getItem(i);

                if (!assembly.isEmpty()) {
                    this.combineMatrix.setItem(i, assembly);
                } else if (!uncrafting.isEmpty() && !isMarked(uncrafting)) {
                    this.combineMatrix.setItem(i, uncrafting);
                } else {
                    this.combineMatrix.setItem(i, ItemStack.EMPTY);
                }
            }
            
            this.chooseRecipe(this.combineMatrix);

            ItemStack input = this.tinkerInput.getItem(0);
            ItemStack result = this.tinkerResult.getItem(0);

            if (!result.isEmpty() && isValidMatchForInput(input, result)) {
                CompoundTag inputTags = null;
                if (input.getTag() != null) {
                    inputTags = input.getTag().copy();
                }

                Map<Enchantment, Integer> resultInnateEnchantments = EnchantmentHelper.getEnchantments(result);
                Map<Enchantment, Integer> inputEnchantments = EnchantmentHelper.getEnchantments(input);
                inputEnchantments.keySet().removeIf(enchantment -> enchantment == null || !enchantment.canEnchant(result));

                if (inputTags != null) {
                    inputTags.remove("ench");
                    inputTags.remove("Damage");
                    result.setTag(inputTags);
                    EnchantmentHelper.setEnchantments(inputEnchantments, result);
                }

                for (Map.Entry<Enchantment, Integer> entry : resultInnateEnchantments.entrySet()) {
                    Enchantment ench = entry.getKey();
                    int level = entry.getValue();

                    if (EnchantmentHelper.isEnchantmentCompatible(EnchantmentHelper.getEnchantments(result).keySet(), ench) && EnchantmentHelper.getTagEnchantmentLevel(ench, result) < level) {
                        result.enchant(ench, level);
                    }
                }

                this.tinkerResult.setItem(0, result);
                this.uncraftingMatrix.uncraftingCost = 0;
                this.uncraftingMatrix.recraftingCost = this.calculateRecraftingCost();
            }
        }
    }

    public static void markStack(ItemStack stack) {
        stack.addTagElement(TAG_MARKER, ByteTag.valueOf((byte) 1));
    }

    public static boolean isMarked(ItemStack stack) {
        CompoundTag stackTag = stack.getTag();
        return stackTag != null && stackTag.getBoolean(TAG_MARKER);
    }

    private static boolean isIngredientProblematic(ItemStack ingredient) {
        return (!ingredient.isEmpty() && ingredient.getItem().hasCraftingRemainingItem(ingredient)) || ingredient.is(Items.BARRIER);
    }

    private static ItemStack normalizeIngredient(ItemStack ingredient) {
        if (ingredient.getCount() > 1) {
            ingredient.setCount(1);
        }
        return ingredient;
    }

    private static Recipe<?>[] getRecipesFor(ItemStack inputStack, Level world) {
        RecipeCacheManager cacheManager = RecipeCacheManager.getInstance(world);
        return cacheManager.getRecipesFor(inputStack);
    }

    private static boolean isRecipeSupported(Recipe<?> recipe) {
        return UncraftingConfig.allowShapelessUncrafting.get() ? recipe instanceof CraftingRecipe : recipe instanceof ShapedRecipe;
    }

    private static boolean matches(ItemStack input, ItemStack output) {
        return input.is(output.getItem()) && input.getCount() >= output.getCount();
    }

    private static CraftingRecipe[] getRecipesFor(CraftingContainer matrix, Level world) {
        return world.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, matrix, world).toArray(new CraftingRecipe[0]);
    }

    private void chooseRecipe(CraftingContainer inventory) {
        CraftingRecipe[] recipes = getRecipesFor(inventory, this.level);

        if (recipes.length == 0) {
            this.tinkerResult.setItem(0, ItemStack.EMPTY);
            return;
        }

        CraftingRecipe recipe = recipes[Math.floorMod(this.recipeInCycle, recipes.length)];

        if (recipe != null && !recipe.isSpecial() && (!this.level.getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING) || ((ServerPlayer) this.player).getRecipeBook().contains(recipe))) {
            this.tinkerResult.setRecipeUsed(recipe);
            this.tinkerResult.setItem(0, recipe.assemble(inventory));
        } else {
            this.tinkerResult.setItem(0, ItemStack.EMPTY);
        }
    }

    private static boolean isValidMatchForInput(ItemStack inputStack, ItemStack resultStack) {
        if (inputStack.is(Tags.Items.TOOLS_PICKAXES) && resultStack.is(Tags.Items.TOOLS_PICKAXES)) {
            return true;
        }
        if (inputStack.is(Tags.Items.TOOLS_AXES) && resultStack.is(Tags.Items.TOOLS_AXES)) {
            return true;
        }
        if (inputStack.is(Tags.Items.TOOLS_SHOVELS) && resultStack.is(Tags.Items.TOOLS_SHOVELS)) {
            return true;
        }
        if (inputStack.is(Tags.Items.TOOLS_HOES) && resultStack.is(Tags.Items.TOOLS_HOES)) {
            return true;
        }
        if (inputStack.is(Tags.Items.TOOLS_SWORDS) && resultStack.is(Tags.Items.TOOLS_SWORDS)) {
            return true;
        }
        if (inputStack.is(Tags.Items.TOOLS_BOWS) && resultStack.is(Tags.Items.TOOLS_BOWS)) {
            return true;
        }
        if (inputStack.is(Tags.Items.TOOLS_CROSSBOWS) && resultStack.is(Tags.Items.TOOLS_CROSSBOWS)) {
            return true;
        }
        if (inputStack.is(Tags.Items.TOOLS_FISHING_RODS) && resultStack.is(Tags.Items.TOOLS_FISHING_RODS)) {
            return true;
        }

        if (inputStack.is(Tags.Items.ARMORS) && resultStack.is(Tags.Items.ARMORS)) {
            return inputStack.getEquipmentSlot() == resultStack.getEquipmentSlot();
        }

        return false;
    }

    private int calculateUncraftingCost() {
        if ((!UncraftingConfig.disableUncraftingOnly.get() || this.storedGhostRecipe instanceof UncraftingRecipe) && this.assemblyMatrix.isEmpty()) {
            return this.storedGhostRecipe instanceof UncraftingRecipe recipe ? recipe.cost() : (int) Math.round(countDamageableParts(this.uncraftingMatrix) * UncraftingConfig.uncraftingXpCostMultiplier.get());
        }
        return 0;
    }

    private int calculateRecraftingCost() {
        ItemStack input = this.tinkerInput.getItem(0);
        ItemStack output = this.tinkerResult.getItem(0);

        if (input.isEmpty() || output.isEmpty()) {
            return 0;
        }

        int cost = 0;

        if (!input.sameItem(output)) {
            int nonEmptySlots = 0;
            for (int i = 0; i < this.assemblyMatrix.getContainerSize(); i++) {
                if (!this.assemblyMatrix.getItem(i).isEmpty()) {
                    nonEmptySlots++;
                }
            }
            cost += nonEmptySlots;
        }

        int enchantCost = countTotalEnchantmentCost(input);
        cost += enchantCost;

        int damagedCost = (1 + this.countDamagedParts(input)) * EnchantmentHelper.getEnchantments(output).size();
        cost += damagedCost;

        cost = Math.max(1, cost);

        return (int) Math.round(Math.max(1, cost) * UncraftingConfig.repairingXpCostMultiplier.get());
    }

    private static int countTotalEnchantmentCost(ItemStack stack) {
        int count = 0;

        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            Enchantment ench = entry.getKey();
            int level = entry.getValue();

            if (ench != null && level > 0) {
                count += getWeightModifier(ench) * level;
                count += 1;
            }
        }

        return count;
    }

    private static int getWeightModifier(Enchantment ench) {
        return switch (ench.getRarity().getWeight()) {
            case 1 -> 8;
            case 2 -> 4;
            case 3, 4, 5 -> 2;
            default -> 1;
        };
    }

    @Override
    public void clicked(int slotNum, int mouseButton, ClickType clickType, Player player) {
        if (slotNum > 0 && this.slots.get(slotNum).container == this.assemblyMatrix
                && player.containerMenu.getCarried().isEmpty() && !this.slots.get(slotNum).hasItem()) {

            if (this.assemblyMatrix.isEmpty() && (clickType != ClickType.SWAP || player.getInventory().getItem(mouseButton).isEmpty())) {
                slotNum -= 9;
            }
        }

        if (slotNum > 0 && this.slots.get(slotNum).container == this.tinkerResult
                && this.calculateRecraftingCost() > player.experienceLevel && !player.getAbilities().instabuild) {
            return;
        }

        if (slotNum > 0 && this.slots.get(slotNum).container == this.uncraftingMatrix) {
            if (UncraftingConfig.disableUncraftingOnly.get() && !(this.storedGhostRecipe instanceof UncraftingRecipe)) {
                return;
            }

            if (this.calculateUncraftingCost() > player.experienceLevel && !player.getAbilities().instabuild) {
                return;
            }

            ItemStack stackInSlot = this.slots.get(slotNum).getItem();
            if (stackInSlot.isEmpty() || isMarked(stackInSlot)) {
                return;
            }
        }

        super.clicked(slotNum, mouseButton, clickType, player);

        if (slotNum > 0 && this.slots.get(slotNum).container == this.tinkerInput) {
            this.slotsChanged(this.tinkerInput);
        }
    }

    private static boolean isDamageableComponent(ItemStack stack) {
        return !stack.isEmpty();
    }

    private static int countDamageableParts(Container matrix) {
        int count = matrix.getContainerSize();
        for (int i = 0; i < matrix.getContainerSize(); i++) {
            if (isIngredientProblematic(matrix.getItem(i)) || isMarked(matrix.getItem(i)) || !isDamageableComponent(matrix.getItem(i))) {
                count--;
            }
        }
        return count;
    }

    private int countDamagedParts(ItemStack input) {
        int totalMax4 = Math.max(4, countDamageableParts(this.uncraftingMatrix));
        float damage = (float) input.getDamageValue() / (float) input.getMaxDamage();
        return (int) Math.ceil(totalMax4 * damage);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotNum) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotNum);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (slotNum == 0) {
                if (!this.moveItemStackTo(itemstack1, 20, 56, false)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            } else if (slotNum == 1) {
                this.positionData.execute((p_39378_, p_39379_) -> itemstack1.getItem().onCraftedBy(itemstack1, p_39378_, player));
                if (!this.moveItemStackTo(itemstack1, 20, 56, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            } else if (slotNum >= 20 && slotNum < 56) {
                if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slot.container == this.assemblyMatrix) {
                if (!this.moveItemStackTo(itemstack1, 20, 56, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (this.moveItemStackTo(itemstack1, 20, 56, false)) {
                    slot.onTake(player, itemstack1);
                    return ItemStack.EMPTY;
                }
            }
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemstack1);
            if (slotNum == 1) {
                player.drop(itemstack1, false);
            }
        }
        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.positionData.execute((world, pos) -> {
            this.clearContainer(player, this.assemblyMatrix);
            this.clearContainer(player, this.tinkerInput);
        });
    }

    private ItemStack[] getIngredients(Recipe<?> recipe) {
        ItemStack[] stacks = new ItemStack[recipe.getIngredients().size()];

        for (int i = 0; i < recipe.getIngredients().size(); i++) {
            ItemStack[] matchingStacks = Arrays.stream(recipe.getIngredients().get(i).getItems()).toArray(ItemStack[]::new);
            stacks[i] = matchingStacks.length > 0 ? matchingStacks[Math.floorMod(this.ingredientsInCycle, matchingStacks.length)] : ItemStack.EMPTY;
        }

        return stacks;
    }

    @Override
    public boolean stillValid(Player player) {
        return !UncraftingConfig.disableEntireTable.get() && stillValid(this.positionData, player, ModBlocks.UNCRAFTING_TABLE.get());
    }

    public int getUncraftingCost() {
        return this.uncraftingMatrix.uncraftingCost;
    }

    public int getRecraftingCost() {
        return this.uncraftingMatrix.recraftingCost;
    }
}