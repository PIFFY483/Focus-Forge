package com.lockon.client;

import com.lockon.config.LockOnConfig;
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
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LockOnEntityListScreen extends Screen {

    private final Screen parent;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> configValue;
    private final Component screenTitle;

    private EntityList listWidget;
    private EditBox searchBox;
    private List<String> currentList;
    private final List<String> availableEntities;

    public LockOnEntityListScreen(Screen parent, ForgeConfigSpec.ConfigValue<List<? extends String>> configValue, Component title) {
        super(title);
        this.parent = parent;
        this.configValue = configValue;
        this.screenTitle = title;

        this.currentList = new ArrayList<>(configValue.get());

        this.availableEntities = BuiltInRegistries.ENTITY_TYPE.keySet().stream()
                .map(ResourceLocation::toString)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    protected void init() {
        this.searchBox = new EditBox(this.font, this.width / 2 - 100, 22, 200, 20, Component.translatable("gui.search"));
        this.searchBox.setResponder(this::updateList);
        this.addWidget(this.searchBox);

        this.listWidget = new EntityList(this.minecraft, this);
        this.addWidget(this.listWidget);
        this.updateList(this.searchBox.getValue());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            this.onClose();
        }).bounds(this.width / 2 - 100, this.height - 29, 200, 20).build());
    }

    public void updateList(String filter) {
        this.listWidget.publicClearEntries();
        String lowerFilter = filter.toLowerCase();

        this.availableEntities.stream()
                .filter(id -> id.toLowerCase().contains(lowerFilter) || getEntityName(id).getString().toLowerCase().contains(lowerFilter))
                .forEach(id -> {
                    this.listWidget.publicAddEntry(new EntityListEntry(this, id));
                });
    }

    private Component getEntityName(String entityId) {
        ResourceLocation rl = ResourceLocation.tryParse(entityId);
        EntityType<?> type = rl != null ? BuiltInRegistries.ENTITY_TYPE.get(rl) : null;
        return type != null ? type.getDescription() : Component.literal(entityId);
    }

    public void toggleEntityInList(String entityId) {
        if (this.currentList.contains(entityId)) {
            this.currentList.remove(entityId);
        } else {
            this.currentList.add(entityId);
            this.currentList.sort(String::compareTo);
        }
        this.configValue.set(this.currentList);
    }

    @Override
    public void onClose() {
        LockOnConfig.CLIENT_SPEC.save();
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        this.listWidget.render(graphics, mouseX, mouseY, partialTicks);
        this.searchBox.render(graphics, mouseX, mouseY, partialTicks);
        graphics.drawCenteredString(this.font, this.screenTitle, this.width / 2, 8, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    // --- EntityList İç Sınıfı ---
    public static class EntityList extends ObjectSelectionList<EntityListEntry> {
        private final LockOnEntityListScreen parentScreen;

        public EntityList(Minecraft mc, LockOnEntityListScreen parent) {
            super(mc, parent.width, parent.height, 45, parent.height - 35, 36);
            this.parentScreen = parent;
        }

        // Public Wrapper Metotlar
        public void publicClearEntries() {
            this.clearEntries();
        }

        public void publicAddEntry(EntityListEntry entry) {
            this.addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return 200;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 105;
        }
    }

    // --- EntityListEntry İç Sınıfı ---
    public static class EntityListEntry extends ObjectSelectionList.Entry<EntityListEntry> {
        private final LockOnEntityListScreen parentScreen;
        private final String entityId;
        private final Component entityName;
        private final Button toggleButton; // final olarak tanımlı

        public EntityListEntry(LockOnEntityListScreen parentScreen, String entityId) {
            this.parentScreen = parentScreen;
            this.entityId = entityId;
            this.entityName = parentScreen.getEntityName(entityId);

            // ✅ Düzeltme: Final değişkenin başlatılması garanti altına alındı
            this.toggleButton = Button.builder(Component.literal(""), (button) -> {
                parentScreen.toggleEntityInList(entityId);
                button.setMessage(getButtonText());
                parentScreen.updateList(parentScreen.searchBox.getValue());
            }).bounds(0, 0, 50, 20).build();

            // Constructor bittiğinde doğru mesajı ayarla
            this.toggleButton.setMessage(getButtonText());
        }

        private Component getButtonText() {
            return parentScreen.currentList.contains(this.entityId) ? Component.translatable("lockon.list.remove") : Component.translatable("lockon.list.add");
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            int textX = parentScreen.width / 2 - 100;
            int buttonX = parentScreen.width / 2 + 50;

            graphics.drawString(parentScreen.font, this.entityName, textX, top + 3, 0xFFFFFF, false);
            graphics.drawString(parentScreen.font, Component.literal(this.entityId), textX, top + 3 + 10, 0xAAAAAA, false);

            this.toggleButton.setMessage(getButtonText());
            this.toggleButton.setX(buttonX);
            this.toggleButton.setY(top + 8);
            this.toggleButton.render(graphics, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.toggleButton.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", this.entityName);
        }
    }
}