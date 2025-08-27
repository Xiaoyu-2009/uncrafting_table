package com.xiaoyu2009.uncraftingtable.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoyu2009.uncraftingtable.UncraftingTableMod;
import com.xiaoyu2009.uncraftingtable.config.UncraftingConfig;
import com.xiaoyu2009.uncraftingtable.inventory.UncraftingMenu;
import com.xiaoyu2009.uncraftingtable.network.NetworkHandler;
import com.xiaoyu2009.uncraftingtable.network.UncraftingGuiPacket;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class UncraftingScreen extends AbstractContainerScreen<UncraftingMenu> {

    private static final ResourceLocation TEXTURE = UncraftingTableMod.getGuiTexture("guigoblintinkering.png");

    public UncraftingScreen(UncraftingMenu container, Inventory player, Component name) {
        super(container, player, name);
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(new CycleButton(this, this.leftPos + 40, this.topPos + 22, true, button -> {
            NetworkHandler.CHANNEL.sendToServer(new UncraftingGuiPacket(0));
            this.menu.unrecipeInCycle++;
            this.menu.slotsChanged(this.menu.tinkerInput);
        }));
        this.addRenderableWidget(new CycleButton(this, this.leftPos + 40, this.topPos + 55, false, button -> {
            NetworkHandler.CHANNEL.sendToServer(new UncraftingGuiPacket(1));
            this.menu.unrecipeInCycle--;
            this.menu.slotsChanged(this.menu.tinkerInput);
        }));

        this.addRenderableWidget(new CycleButtonMini(this, this.leftPos + 27, this.topPos + 56, true, button -> {
            NetworkHandler.CHANNEL.sendToServer(new UncraftingGuiPacket(2));
            this.menu.ingredientsInCycle++;
            this.menu.slotsChanged(this.menu.tinkerInput);
        }));
        this.addRenderableWidget(new CycleButtonMini(this, this.leftPos + 27, this.topPos + 63, false, button -> {
            NetworkHandler.CHANNEL.sendToServer(new UncraftingGuiPacket(3));
            this.menu.ingredientsInCycle--;
            this.menu.slotsChanged(this.menu.tinkerInput);
        }));

        this.addRenderableWidget(new CycleButton(this, this.leftPos + 121, this.topPos + 22, true, button -> {
            NetworkHandler.CHANNEL.sendToServer(new UncraftingGuiPacket(4));
            this.menu.recipeInCycle++;
            this.menu.slotsChanged(this.menu.assemblyMatrix);
        }));
        this.addRenderableWidget(new CycleButton(this, this.leftPos + 121, this.topPos + 55, false, button -> {
            NetworkHandler.CHANNEL.sendToServer(new UncraftingGuiPacket(5));
            this.menu.recipeInCycle--;
            this.menu.slotsChanged(this.menu.assemblyMatrix);
        }));
    }

    @Override
    public boolean mouseScrolled(double x, double y, double direction) {
        boolean scrolled = super.mouseScrolled(x, y, direction);

        if (x > this.leftPos + 27 && x < this.leftPos + 33 && y > this.topPos + 56 && y < this.topPos + 69) {
            if (direction > 0) {
                NetworkHandler.CHANNEL.sendToServer(new UncraftingGuiPacket(2));
                this.menu.ingredientsInCycle++;
            } else {
                NetworkHandler.CHANNEL.sendToServer(new UncraftingGuiPacket(3));
                this.menu.ingredientsInCycle--;
            }
            this.menu.slotsChanged(this.menu.tinkerInput);
        }

        if (x > this.leftPos + 40 && x < this.leftPos + 54 && y > this.topPos + 22 && y < this.topPos + 64) {
            if (direction > 0) {
                NetworkHandler.CHANNEL.sendToServer(new UncraftingGuiPacket(0));
                this.menu.unrecipeInCycle++;
            } else {
                NetworkHandler.CHANNEL.sendToServer(new UncraftingGuiPacket(1));
                this.menu.unrecipeInCycle--;
            }
            this.menu.slotsChanged(this.menu.tinkerInput);
        }

        if (x > this.leftPos + 121 && x < this.leftPos + 135 && y > this.topPos + 22 && y < this.topPos + 64) {
            if (direction > 0) {
                NetworkHandler.CHANNEL.sendToServer(new UncraftingGuiPacket(4));
                this.menu.recipeInCycle++;
            } else {
                NetworkHandler.CHANNEL.sendToServer(new UncraftingGuiPacket(5));
                this.menu.recipeInCycle--;
            }
            this.menu.slotsChanged(this.menu.assemblyMatrix);
        }

        return scrolled;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        this.font.draw(poseStack, this.title, 6, 6, 4210752);
        if (UncraftingConfig.disableUncraftingOnly.get()) {
            this.font.draw(poseStack, Component.translatable("container.uncrafting_table.uncrafting_table.uncrafting_disabled").withStyle(ChatFormatting.DARK_RED), 6, this.imageHeight - 96 + 2, 4210752);
        } else {
            this.font.draw(poseStack, I18n.get("container.inventory"), 7, this.imageHeight - 96 + 2, 4210752);
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int frameX = (this.width - this.imageWidth) / 2;
        int frameY = (this.height - this.imageHeight) / 2;
        this.blit(poseStack, frameX, frameY, 0, 0, this.imageWidth, this.imageHeight);

        UncraftingMenu tfContainer = this.menu;

        poseStack.pushPose();
        poseStack.translate(this.leftPos, this.topPos, 0);

        for (int i = 0; i < 9; i++) {
            Slot uncrafting = tfContainer.getSlot(2 + i);
            Slot assembly = tfContainer.getSlot(11 + i);

            ItemStack uncraftingItem = uncrafting.container.getItem(uncrafting.getSlotIndex());
            if (!uncraftingItem.isEmpty()) {
                this.drawSlotAsBackground(poseStack, uncraftingItem, assembly);
            }
        }
        poseStack.popPose();

        int costVal = tfContainer.getUncraftingCost();
        if (costVal > 0) {
            int color;
            String cost = "" + costVal;
            if (this.minecraft.player.experienceLevel < costVal && !this.minecraft.player.getAbilities().instabuild) {
                color = 0xA00000;
            } else {
                color = 0x80FF20;
            }
            this.font.draw(poseStack, cost, frameX + 48 - this.font.width(cost), frameY + 38, color);
        }

        costVal = tfContainer.getRecraftingCost();
        if (costVal > 0) {
            int color;
            String cost = "" + costVal;
            if (this.minecraft.player.experienceLevel < costVal && !this.minecraft.player.getAbilities().instabuild) {
                color = 0xA00000;
            } else {
                color = 0x80FF20;
            }
            this.font.draw(poseStack, cost, frameX + 130 - this.font.width(cost), frameY + 38, color);
        }
    }

    private void drawSlotAsBackground(PoseStack poseStack, ItemStack itemStackToRender, Slot appearSlot) {
        int screenX = appearSlot.x + leftPos;
        int screenY = appearSlot.y + topPos;

        this.itemRenderer.renderGuiItem(itemStackToRender, screenX, screenY);

        boolean itemBroken = UncraftingMenu.isMarked(itemStackToRender);

        RenderSystem.disableDepthTest();
        this.fillGradient(poseStack, appearSlot.x, appearSlot.y, appearSlot.x + 16, appearSlot.y + 16, itemBroken ? 0x80FF8b8b : 0x9f8b8b8b, itemBroken ? 0x80FF8b8b : 0x9f8b8b8b);
        RenderSystem.enableDepthTest();
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, int pX, int pY) {
        UncraftingMenu container = this.menu;

        for (int i = 0; i < 9; i++) {
            if (container.getCarried().isEmpty() && container.slots.get(2 + i).hasItem() && this.hoveredSlot == container.slots.get(11 + i) && !container.slots.get(11 + i).hasItem()) {
                this.renderTooltip(poseStack, container.slots.get(2 + i).getItem(), pX, pY);
            }
        }

        super.renderTooltip(poseStack, pX, pY);
    }

    private static class CycleButton extends Button {
        private final boolean up;
        private final UncraftingScreen screen;

        CycleButton(UncraftingScreen screen, int x, int y, boolean up, OnPress onClick) {
            super(x, y, 14, 9, Component.empty(), onClick);
            this.up = up;
            this.screen = screen;
        }

        @Override
        public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            if (this.visible) {
                this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

                int textureX = 176;
                int textureY = 0;

                if (this.isHovered) textureX += this.width;

                if (!this.up) textureY += this.height;

                RenderSystem.setShaderTexture(0, TEXTURE);
                screen.blit(poseStack, this.x, this.y, textureX, textureY, this.width, this.height);
            }
        }
    }

    private static class CycleButtonMini extends Button {
        private final boolean up;
        private final UncraftingScreen screen;

        CycleButtonMini(UncraftingScreen screen, int x, int y, boolean up, OnPress onClick) {
            super(x, y, 7, 7, Component.empty(), onClick);
            this.up = up;
            this.screen = screen;
        }

        @Override
        public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            if (this.visible) {
                this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

                int textureX = 176;
                int textureY = 41;

                if (this.isHovered) textureX += this.width;

                if (!this.up) textureY += this.height;

                RenderSystem.setShaderTexture(0, TEXTURE);
                screen.blit(poseStack, this.x, this.y, textureX, textureY, this.width, this.height);
            }
        }
    }
}