package com.xiaoyu2009.uncraftingtable.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.xiaoyu2009.uncraftingtable.config.UncraftingConfig;
import com.xiaoyu2009.uncraftingtable.init.ModRecipes;
import com.xiaoyu2009.uncraftingtable.item.recipe.UncraftingRecipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public class RecipeCacheManager {
    
    private static final Map<Level, RecipeCacheManager> INSTANCES = new ConcurrentHashMap<>();
    
    private final Map<String, List<Recipe<?>>> recipeCache = new ConcurrentHashMap<>();
    private final List<UncraftingRecipe> uncraftingRecipes = new ArrayList<>();
    private final Level level;
    private boolean loaded = false;
    
    private RecipeCacheManager(Level level) {
        this.level = level;
    }
    
    public static RecipeCacheManager getInstance(Level level) {
        return INSTANCES.computeIfAbsent(level, RecipeCacheManager::new);
    }
    
    public void loadRecipes() {
        if (loaded) {
            return;
        }
        
        recipeCache.clear();
        uncraftingRecipes.clear();
        
        for (Recipe<?> recipe : level.getRecipeManager().getRecipes()) {
            if (isRecipeSupported(recipe) &&
                    !recipe.isIncomplete() &&
                    recipe.canCraftInDimensions(3, 3) &&
                    !recipe.getIngredients().isEmpty()) {
                
                ItemStack resultItem = recipe.getResultItem(level.registryAccess());
                if (!resultItem.isEmpty()) {
                    boolean shouldCache = true;

                    if (UncraftingConfig.disableUncraftingRecipes.get().contains(recipe.getId().toString())) {
                        shouldCache = UncraftingConfig.reverseRecipeBlacklist.get();
                    } else {
                        shouldCache = !UncraftingConfig.reverseRecipeBlacklist.get();
                    }

                    if (shouldCache) {
                        if (UncraftingConfig.blacklistedUncraftingModIds.get().contains(recipe.getId().getNamespace())) {
                            shouldCache = UncraftingConfig.flipUncraftingModIdList.get();
                        } else {
                            shouldCache = !UncraftingConfig.flipUncraftingModIdList.get();
                        }
                    }
                    
                    if (shouldCache) {
                        addRecipeToCache(resultItem, recipe);
                    }
                }
            }
        }
        
        uncraftingRecipes.addAll(level.getRecipeManager().getAllRecipesFor(ModRecipes.UNCRAFTING_RECIPE.get()));
        loaded = true;
    }
    
    public void reloadRecipes() {
        loaded = false;
        recipeCache.clear();
        uncraftingRecipes.clear();
        loadRecipes();
    }
    
    public static void clearAll() {
        for (RecipeCacheManager manager : INSTANCES.values()) {
            manager.loaded = false;
            manager.recipeCache.clear();
            manager.uncraftingRecipes.clear();
        }
        INSTANCES.clear();
    }
    
    public Recipe<?>[] getRecipesFor(ItemStack inputStack) {
        if (!loaded) {
            loadRecipes();
        }
        
        if (inputStack.isEmpty()) {
            return new Recipe<?>[0];
        }
        
        List<Recipe<?>> recipes = new ArrayList<>();
        
        String inputKey = inputStack.getItem().toString();
        List<Recipe<?>> cachedRecipes = recipeCache.get(inputKey);
        if (cachedRecipes != null) {
            for (Recipe<?> recipe : cachedRecipes) {
                ItemStack resultItem = recipe.getResultItem(level.registryAccess());
                if (matches(inputStack, resultItem)) {
                    recipes.add(recipe);
                }
            }
        }
        
        for (UncraftingRecipe uncraftingRecipe : uncraftingRecipes) {
            if (uncraftingRecipe.isItemStackAnIngredient(inputStack)) {
                recipes.add(uncraftingRecipe);
            }
        }
        
        return recipes.toArray(new Recipe<?>[0]);
    }
    
    private void addRecipeToCache(ItemStack resultItem, Recipe<?> recipe) {
        String key = resultItem.getItem().toString();
        recipeCache.computeIfAbsent(key, k -> new ArrayList<>()).add(recipe);
    }
    
    private static boolean isRecipeSupported(Recipe<?> recipe) {
        return UncraftingConfig.allowShapelessUncrafting.get() ? recipe instanceof CraftingRecipe : recipe instanceof ShapedRecipe;
    }
    
    private static boolean matches(ItemStack input, ItemStack output) {
        return input.is(output.getItem()) && input.getCount() >= output.getCount();
    }
}