package com.froggyy277353.aeronet.widgets.strings;

import com.froggyy277353.aeronet.client.gui.GuiUtils;
import com.froggyy277353.aeronet.client.gui.TextField;
import com.froggyy277353.aeronet.network.NodeType;
import com.froggyy277353.aeronet.widgets.Node;
import com.froggyy277353.aeronet.widgets.valueType.NodeValue;
import com.froggyy277353.aeronet.widgets.valueType.ValueType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class ItemConstantNode extends Node {
    private static final int SEARCH_CHARS = 24;
    private static final int SEARCH_MAX_ROWS = 8;
    private static final int SEARCH_FIELD_HEIGHT = 12;
    private static final int ROW_HEIGHT = 14;
    private static final int POPUP_WIDTH = 165;

    private record CatalogEntry(String id, String name, String tag, ItemStack icon, String haystack) {}
    private record ScoredEntry(CatalogEntry entry, int score) {}

    private static List<CatalogEntry> cachedCatalog = null;

    private String selectedId = "minecraft:stone";
    private CatalogEntry selectedEntry = null;

    private boolean searchOpened = false;
    private final TextField searchField = new TextField(0, 0, 0, SEARCH_CHARS, SEARCH_FIELD_HEIGHT,
            0xFF1E1E1E, 0xFFFFFFFF, 0xFF699684);
    private List<CatalogEntry> searchResults = List.of();
    private String cachedQuery = null;
    private int selectedIndex = 0;
    private int searchScroll = 0;
    private int lastMouseX = -1;
    private int lastMouseY = -1;

    private int btnX, btnY, btnW, btnH;
    private int popupX, popupY, popupW, popupH;

    public ItemConstantNode(UUID id, int x, int y, int inputs, int outputs, int width, int height,
                             List<NodeValue> inputArray, List<NodeValue> outputArray) {
        super(id, x, y, 0, 1, 110, 34, inputArray, outputArray);
        red = 0.388F;
        green = 0.400F;
        blue = 0.945F;
        resolveSelectedEntry();
        setOutputValue(0, NodeValue.of(selectedId));
    }

    private static List<CatalogEntry> getCatalog() {
        if (cachedCatalog != null) {
            return cachedCatalog;
        }
        List<CatalogEntry> list = new ArrayList<>();

        for (Block block : BuiltInRegistries.BLOCK) {
            if (block == Blocks.AIR) continue;
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
            String id = key.toString();
            String name = block.getName().getString();
            Item item = block.asItem();
            ItemStack icon = (item != Items.AIR) ? new ItemStack(item) : ItemStack.EMPTY;
            String haystack = (name + " " + id + " block блок").toLowerCase(Locale.ROOT);
            list.add(new CatalogEntry(id, name, "Block", icon, haystack));
        }
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR || Block.byItem(item) != Blocks.AIR) continue;
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
            String id = key.toString();
            String name = item.getDescription().getString();
            ItemStack icon = new ItemStack(item);
            String haystack = (name + " " + id + " item предмет").toLowerCase(Locale.ROOT);
            list.add(new CatalogEntry(id, name, "Item", icon, haystack));
        }

        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            String id = key.toString();
            String name = entityType.getDescription().getString();
            SpawnEggItem egg = SpawnEggItem.byId(entityType);
            ItemStack icon = (egg != null) ? new ItemStack(egg) : ItemStack.EMPTY;
            String haystack = (name + " " + id + " mob entity моб").toLowerCase(Locale.ROOT);
            list.add(new CatalogEntry(id, name, "Mob", icon, haystack));
        }

        cachedCatalog = list;
        return cachedCatalog;
    }

    private void resolveSelectedEntry() {
        for (CatalogEntry entry : getCatalog()) {
            if (entry.id().equals(selectedId)) {
                selectedEntry = entry;
                return;
            }
        }
        selectedEntry = null;
    }

    @Override
    public ValueType getOutputType(int port) {
        return port == 0 ? ValueType.STRING : ValueType.NULL;
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick, int screenWidth, int screenHeight) {
        Component title = Component.translatable("aeronet.node.id_constant");
        this.width = Math.max(font.width(title) + 8, 108);
        this.height = 34;

        super.render(graphics, font, mouseX, mouseY, partialTick, screenWidth, screenHeight);
        graphics.drawString(font, title, screenX + 3, screenY + 2, 0xFFFFFF);

        btnX = screenX + 4;
        btnY = screenY + 14;
        btnW = this.width - 8;
        btnH = 16;

        boolean btnHovered = !searchOpened && mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        int bgColor = btnHovered ? 0xFF3A3A3A : 0xFF282828;
        int borderColor = (btnHovered || searchOpened) ? 0xFF8CC4AD : 0xFF699684;
        GuiUtils.drawOutlinedRect(graphics, btnX, btnY, btnW, btnH, bgColor, borderColor);

        int textOffsetX = btnX + 3;
        if (selectedEntry != null && !selectedEntry.icon().isEmpty()) {
            renderScaledIcon(graphics, selectedEntry.icon(), btnX + 2, btnY + 2, 0.75f);
            textOffsetX = btnX + 16;
        }

        String displayLabel = selectedEntry != null ? selectedEntry.name() : selectedId;
        int maxLabelW = (btnX + btnW - 3) - textOffsetX;
        if (font.width(displayLabel) > maxLabelW) {
            displayLabel = font.plainSubstrByWidth(displayLabel, Math.max(0, maxLabelW - font.width("..."))) + "...";
        }
        graphics.drawString(font, displayLabel, textOffsetX, btnY + 4, 0xFFFFFFFF);

        if (searchOpened) {
            graphics.flush();
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 150);
            renderSearchPopup(graphics, font, mouseX, mouseY, partialTick);
            graphics.flush();
            graphics.pose().popPose();
        }

        setOutputValue(0, NodeValue.of(selectedId));
    }

    private void renderScaledIcon(GuiGraphics graphics, ItemStack stack, int x, int y, float scale) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, -142);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
    }


    private void openSearch() {
        searchOpened = true;
        searchField.setText("");
        searchField.setFocused(true);
        cachedQuery = null;
        selectedIndex = 0;
        searchScroll = 0;
        refreshResults();
    }

    private void refreshResults() {
        String query = searchField.getText();
        if (query.equals(cachedQuery)) return;
        cachedQuery = query;

        String trimmed = query.trim().toLowerCase(Locale.ROOT);
        List<CatalogEntry> catalog = getCatalog();
        if (trimmed.isEmpty()) {
            searchResults = catalog;
            selectedIndex = 0;
            searchScroll = 0;
            return;
        }

        String[] tokens = trimmed.split("\\s+");
        List<ScoredEntry> found = new ArrayList<>();
        for (CatalogEntry entry : catalog) {
            boolean matches = true;
            for (String token : tokens) {
                if (!entry.haystack().contains(token)) {
                    matches = false;
                    break;
                }
            }
            if (!matches) continue;

            String lowerName = entry.name().toLowerCase(Locale.ROOT);
            int score;
            if (lowerName.startsWith(trimmed)) score = 0;
            else if (lowerName.contains(" " + trimmed)) score = 1;
            else if (lowerName.contains(trimmed)) score = 2;
            else if (entry.id().contains(trimmed)) score = 3;
            else score = 4;

            found.add(new ScoredEntry(entry, score));
        }

        found.sort(Comparator.comparingInt(ScoredEntry::score)
                .thenComparing(s -> s.entry().name()));
        List<CatalogEntry> sorted = new ArrayList<>(found.size());
        for (ScoredEntry s : found) {
            sorted.add(s.entry());
        }
        searchResults = sorted;
        selectedIndex = 0;
        searchScroll = 0;
    }

    private void updatePopupBounds() {
        refreshResults();
        int visibleRows = Math.max(1, Math.min(searchResults.size(), SEARCH_MAX_ROWS));
        popupW = POPUP_WIDTH;
        popupH = 3 + SEARCH_FIELD_HEIGHT + 3 + visibleRows * ROW_HEIGHT + 2;
        popupX = btnX;
        popupY = btnY + btnH + 2;
    }

    private void renderSearchPopup(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick) {
        updatePopupBounds();
        searchField.setFocused(true);

        boolean mouseMoved = mouseX != lastMouseX || mouseY != lastMouseY;
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        GuiUtils.drawOutlinedRect(graphics, popupX, popupY, popupW, popupH, 0xFF282828, 0xFF699684);
        searchField.setX(popupX + 3);
        searchField.setY(popupY + 3);
        searchField.render(graphics, mouseX, mouseY, partialTick, font);
        int rowsTop = popupY + 3 + SEARCH_FIELD_HEIGHT + 3;
        if (searchResults.isEmpty()) {
            Component noRes = Component.translatable("aeronet.nodemenu.no_results");
            graphics.drawString(font, noRes, popupX + 4, rowsTop + 3, 0xFF9AA5A0);
            return;
        }
        int visible = Math.min(searchResults.size() - searchScroll, SEARCH_MAX_ROWS);
        for (int i = 0; i < visible; i++) {
            int index = searchScroll + i;
            int rowY = rowsTop + i * ROW_HEIGHT;
            boolean hovered = mouseX >= popupX && mouseX <= popupX + popupW
                    && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            if (hovered && mouseMoved) {
                selectedIndex = index;
            }
            if (index == selectedIndex) {
                graphics.fill(popupX + 1, rowY, popupX + popupW - 1, rowY + ROW_HEIGHT, 0xAA000000);
            }
            CatalogEntry entry = searchResults.get(index);
            int textX = popupX + 4;
            if (!entry.icon().isEmpty()) {
                renderScaledIcon(graphics, entry.icon(), popupX + 3, rowY + 1, 0.75f);
                textX = popupX + 17;
            }
            String suffix = " [" + entry.tag() + "]";
            int maxW = (popupX + popupW - 4) - textX;
            int suffixW = font.width(suffix);
            String name = entry.name();
            if (font.width(name) + suffixW > maxW) {
                name = font.plainSubstrByWidth(name, Math.max(0, maxW - suffixW - font.width("..."))) + "...";
            }
            graphics.drawString(font, name, textX, rowY + 3, 0xFFFFFFFF);
            graphics.drawString(font, suffix, textX + font.width(name), rowY + 3, 0xFF9AA5A0);
        }
    }
    private void pickEntry(int index) {
        if (index < 0 || index >= searchResults.size()) return;
        selectedEntry = searchResults.get(index);
        selectedId = selectedEntry.id();
        setOutputValue(0, NodeValue.of(selectedId));
        searchOpened = false;
        changed = true;
    }
    private void ensureSelectedVisible() {
        if (selectedIndex < searchScroll) {
            searchScroll = selectedIndex;
        } else if (selectedIndex >= searchScroll + SEARCH_MAX_ROWS) {
            searchScroll = selectedIndex - SEARCH_MAX_ROWS + 1;
        }
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int screenWidth, int screenHeight) {
        if (searchOpened) {
            updatePopupBounds();
            boolean insidePopup = mouseX >= popupX && mouseX <= popupX + popupW
                    && mouseY >= popupY && mouseY <= popupY + popupH;
            if (!insidePopup) {
                searchOpened = false;
                return super.mouseClicked(mouseX, mouseY, button, screenWidth, screenHeight);
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                int rowsTop = popupY + 3 + SEARCH_FIELD_HEIGHT + 3;
                if (mouseY >= rowsTop) {
                    int row = ((int) mouseY - rowsTop) / ROW_HEIGHT;
                    int visible = Math.min(searchResults.size() - searchScroll, SEARCH_MAX_ROWS);
                    if (row >= 0 && row < visible) {
                        pickEntry(searchScroll + row);
                        return true;
                    }
                }
                searchField.mouseClicked(mouseX, mouseY, button);
            }
            searchField.setFocused(true);
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && mouseX >= btnX && mouseX <= btnX + btnW
                && mouseY >= btnY && mouseY <= btnY + btnH) {
            openSearch();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button, screenWidth, screenHeight);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (searchOpened) {
            updatePopupBounds();
            if (mouseX >= popupX && mouseX <= popupX + popupW
                    && mouseY >= popupY && mouseY <= popupY + popupH) {
                int max = Math.max(0, searchResults.size() - SEARCH_MAX_ROWS);
                searchScroll = Math.max(0, Math.min(max, searchScroll - (int) Math.signum(scrollY)));
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!searchOpened) return false;
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            searchOpened = false;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            refreshResults();
            pickEntry(selectedIndex);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_UP) {
            refreshResults();
            int count = searchResults.size();
            if (count > 0) {
                selectedIndex = (selectedIndex + (keyCode == GLFW.GLFW_KEY_DOWN ? 1 : count - 1)) % count;
                ensureSelectedVisible();
            }
            return true;
        }
        searchField.setFocused(true);
        searchField.keyPressed(keyCode, scanCode, modifiers);
        return true;
    }
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!searchOpened) return false;
        searchField.setFocused(true);
        searchField.charTyped(codePoint, modifiers);
        return true;
    }
    @Override
    public boolean hasFocusedInput() {
        return searchOpened;
    }
    @Override
    public void clearInputFocus() {
    }
    @Override
    public CompoundTag getExtraData() {
        CompoundTag tag = new CompoundTag();
        tag.putString("selectedId", selectedId);
        return tag;
    }
    @Override
    public void setExtraData(CompoundTag tag) {
        if (tag.contains("selectedId")) {
            selectedId = tag.getString("selectedId");
            resolveSelectedEntry();
            setOutputValue(0, NodeValue.of(selectedId));
        }
    }
    @Override
    public NodeType getType() {
        return NodeType.ID_CONSTANT;
    }
}