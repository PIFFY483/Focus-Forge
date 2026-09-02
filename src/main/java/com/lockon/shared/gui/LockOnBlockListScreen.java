package com.lockon.shared.gui;

import com.lockon.shared.config.SharedListConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Blok listesi (acquisition/preclusion) ekranı. OLD ve NEW kamera modlarının
 * ikisi de bu tek ekranı ve {@link SharedListConfig}'teki ortak listeleri
 * kullanır (bkz. SharedListConfig sınıf açıklaması).
 */
public class LockOnBlockListScreen extends Screen {

    private final Screen parent;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> configValue;
    private final Component screenTitle;
    private final boolean isPreclusionList;

    private BlockList listWidget;
    private EditBox searchBox;
    private List<String> currentList;
    private final List<String> allBlockIds;

    public LockOnBlockListScreen(Screen parent, Component title, ForgeConfigSpec.ConfigValue<List<? extends String>> configValue, boolean isPreclusionList) {
        super(title);
        this.parent = parent;
        this.configValue = configValue;
        this.screenTitle = title;
        this.isPreclusionList = isPreclusionList;

        this.currentList = new ArrayList<>(configValue.get());

        this.allBlockIds = BuiltInRegistries.BLOCK.keySet().stream()
                .map(ResourceLocation::toString)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    @Override
    protected void init() {
        this.clearWidgets();

        this.searchBox = new EditBox(this.font, this.width / 2 - 100, 22, 200, 20, Component.translatable("gui.search"));
        this.searchBox.setHint(Component.translatable("lockon.list.search_block_hint"));
        this.searchBox.setResponder(this::updateList);
        this.addWidget(this.searchBox);

        this.listWidget = new BlockList(this.minecraft, this.width, this.height, 48, this.height - 32, 24);
        this.addWidget(this.listWidget);

        this.updateList(this.searchBox.getValue());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            this.configValue.set(this.currentList);
            SharedListConfig.SPEC.save();
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    private void updateList(String filterText) {
        this.listWidget.clearList();
        String lowerFilter = filterText.toLowerCase();

        this.allBlockIds.stream()
                .filter(id -> id.toLowerCase().contains(lowerFilter))
                .forEach(id -> {
                    boolean isAdded = this.currentList.contains(id);
                    this.listWidget.addEntryPublic(new BlockListEntry(this, id, isAdded));
                });
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        this.listWidget.render(graphics, mouseX, mouseY, partialTicks);
        graphics.drawCenteredString(this.font, this.screenTitle, this.width / 2, 8, 0xFFFFFF);
        this.searchBox.render(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    // ----------- İÇ SINIFLAR -----------

    private class BlockList extends ObjectSelectionList<BlockListEntry> {
        public BlockList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
            super(minecraft, width, height, y0, y1, itemHeight);
            this.setRenderSelection(false);
        }

        public void clearList() {
            this.clearEntries();
        }

        public void addEntryPublic(BlockListEntry entry) {
            this.addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return 250;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 120;
        }

        @Override
        public void setSelected(BlockListEntry entry) { /* Engellendi */ }
    }

    private class BlockListEntry extends ObjectSelectionList.Entry<BlockListEntry> {
        private final LockOnBlockListScreen parentScreen;
        private final String blockId;
        private boolean isAdded;
        private final Component blockName;
        private final Button toggleButton;

        public BlockListEntry(LockOnBlockListScreen parentScreen, String blockId, boolean isAdded) {
            this.parentScreen = parentScreen;
            this.blockId = blockId;
            this.isAdded = isAdded;

            Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse(blockId));
            this.blockName = block != null ? block.getName() : Component.translatable("lockon.list.unknown_block", blockId);

            this.toggleButton = Button.builder(getButtonText(), (button) -> {
                this.isAdded = !this.isAdded;
                if (this.isAdded) {
                    parentScreen.currentList.add(blockId);
                } else {
                    parentScreen.currentList.remove(blockId);
                }
                button.setMessage(getButtonText());
                parentScreen.updateList(parentScreen.searchBox.getValue());
            }).bounds(0, 0, 50, 20).build();
        }

        private Component getButtonText() {
            return this.isAdded ? Component.translatable("lockon.list.remove") : Component.translatable("lockon.list.add");
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            int textX = parentScreen.width / 2 - 100;
            int buttonX = parentScreen.width / 2 + 50;

            graphics.drawString(parentScreen.font, this.blockName, textX, top + 3, 0xFFFFFF, false);
            graphics.drawString(parentScreen.font, Component.literal(this.blockId), textX, top + 3 + 10, 0xAAAAAA, false);

            this.toggleButton.setX(buttonX);
            this.toggleButton.setY(top);
            this.toggleButton.render(graphics, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.toggleButton.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", this.blockName);
        }
    }
}
