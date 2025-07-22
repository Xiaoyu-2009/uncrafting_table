package com.xiaoyu2009.uncraftingtable.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class UncraftingConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue uncraftingXpCostMultiplier;
    public static final ForgeConfigSpec.DoubleValue repairingXpCostMultiplier;
    public static final ForgeConfigSpec.BooleanValue allowShapelessUncrafting;
    public static final ForgeConfigSpec.BooleanValue disableUncraftingOnly;
    public static final ForgeConfigSpec.BooleanValue disableEntireTable;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> disableUncraftingRecipes;
    public static final ForgeConfigSpec.BooleanValue reverseRecipeBlacklist;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> blacklistedUncraftingModIds;
    public static final ForgeConfigSpec.BooleanValue flipUncraftingModIdList;

    static {
        BUILDER.push("拆解台配置");

        uncraftingXpCostMultiplier = BUILDER
                .comment("拆解物品的经验消耗倍数")
                .defineInRange("uncraftingXpCostMultiplier", 1.0, 0.0, Double.MAX_VALUE);

        repairingXpCostMultiplier = BUILDER
                .comment("修复物品的经验消耗倍数")
                .defineInRange("repairingXpCostMultiplier", 0.5, 0.0, Double.MAX_VALUE);

        allowShapelessUncrafting = BUILDER
                .comment("是否允许拆解无形状配方")
                .define("allowShapelessUncrafting", true);

        disableUncraftingOnly = BUILDER
                .comment("禁用拆解功能，只保留合成功能")
                .define("disableUncraftingOnly", false);

        disableEntireTable = BUILDER
                .comment("完全禁用拆解台")
                .define("disableEntireTable", false);

        disableUncraftingRecipes = BUILDER
                .comment("禁用的拆解配方列表")
                .defineList("disableUncraftingRecipes", List.of(), obj -> obj instanceof String);

        reverseRecipeBlacklist = BUILDER
                .comment("反转配方黑名单")
                .define("reverseRecipeBlacklist", false);

        blacklistedUncraftingModIds = BUILDER
                .comment("禁用拆解的模组ID列表")
                .defineList("blacklistedUncraftingModIds", List.of(), obj -> obj instanceof String);

        flipUncraftingModIdList = BUILDER
                .comment("反转模组ID列表")
                .define("flipUncraftingModIdList", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}