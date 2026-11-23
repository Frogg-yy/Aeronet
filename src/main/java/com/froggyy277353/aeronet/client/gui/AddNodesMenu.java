package com.froggyy277353.aeronet.client.gui;

import com.froggyy277353.aeronet.network.NodeType;
import com.froggyy277353.aeronet.network.NodesData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class AddNodesMenu {
    private boolean isOpened = false;

    private int x = 0;
    private int y = 0;

    private static final String[] nodes = {
            // math
            "math.add", "math.subtract", "math.multiply", "math.divide", "math.map", "math.int",
            "math.distance_brightness", "math.trigonometry.sin", "math.trigonometry.cos",
            "math.trigonometry.tan", "math.trigonometry.asin", "math.trigonometry.acos",
            "math.trigonometry.atan", "math.timer", "math.impulse",
            // strings
            "strings.string", "strings.id_constant", "strings.concat",
            // logical
            "logical.equals", "logical.less", "logical.greater", "logical.equals_less",
            "logical.equals_greater", "logical.strings.string_equals",
            // boolean
            "boolean.boolean_float",
            "boolean.not",
            // physical
            "physical.analog_link", "physical.gearshift", "physical.servo",
            "physical.scanner.scanner", "physical.scanner.scanner_pixel", "physical.scanner.scanner_stats",
            "physical.display.display", "physical.display.canvas_size", "physical.display.draw_pixel",
            "physical.display.draw_line", "physical.display.draw_rect", "physical.display.draw_circle",
            "physical.display.scanner.draw_scan",
            // modules
            "modules.altitude", "modules.gyro", "modules.compass", "modules.gps", "modules.link",
            // arrays
            "arrays.array_pack", "arrays.array_unpack",
            // other
            "other.display_value", "other.view_string"
    };

    private static final String[] nestedMenus = {
            "trigonometry", "display", "scanner"
    };

    private static final String CATEGORY_FIND = "find";
    private static final String NODE_FIND_PLACEHOLDER = "aaa";

    private final List<String> categories = List.of(
            CATEGORY_FIND,
            "math",
            "strings",
            "logical",
            "boolean",
            "physical",
            "modules",
            "arrays",
            "other"
    );

    private final Map<String, List<String>> nestedMenusContent = new LinkedHashMap<>();
    private final Map<String, String> nodeDisplayNames = new HashMap<>();
    private final Map<String, String> menuDisplayNames = new HashMap<>();

    private final Map<String, List<String>> subMenus = new HashMap<>();
    private final Map<String, NodeType> nodeTypeByKey = new HashMap<>();

    private String openedNestedMenu = "";
    private String subMenu = "";
    private Font font;
    private int width = 48;
    private int height;

    private ServerSreen screen = null;
    private boolean isChanged = false;
    private boolean clickedThisFrame = false;

    private static final int LIST_PAD = 3;
    private int screenWidth = 0;
    private int screenHeight = 0;

    public AddNodesMenu(Font font) {
        this.font = font;
        int lh = (font != null) ? font.lineHeight : 9;
        this.height = categories.size() * lh + 3;

        Set<String> categorySet = new HashSet<>(categories);
        Set<String> nestedMenuSet = new HashSet<>(Arrays.asList(nestedMenus));

        for (String category : categories) {
            subMenus.put(category, new ArrayList<>());
            menuDisplayNames.put(category, Component.translatable("aeronet.nodemenu." + category).getString());
        }
        subMenus.get(CATEGORY_FIND).add(NODE_FIND_PLACEHOLDER);
        nodeDisplayNames.put(NODE_FIND_PLACEHOLDER, NODE_FIND_PLACEHOLDER);

        for (String nested : nestedMenus) {
            nestedMenusContent.put(nested, new ArrayList<>());
            menuDisplayNames.put(nested, Component.translatable("aeronet.nodemenu." + nested).getString());
        }

        for (String path : nodes) {
            String[] parts = path.split("\\.");
            if (parts.length < 2) continue;

            String key = parts[parts.length - 1];
            nodeTypeByKey.put(key, NodeType.valueOf(key.toUpperCase(Locale.ROOT)));
            nodeDisplayNames.put(key, Component.translatable("aeronet.node." + key).getString());


            String lastCategory = null;
            for (int i = 0; i < parts.length - 1; i++) {
                String part = parts[i];
                if (categorySet.contains(part)) {
                    lastCategory = part;
                    String nextPart = parts[i + 1];
                    if (!nestedMenuSet.contains(nextPart)) {
                        addUnique(subMenus.get(part), key);
                    }
                } else if (nestedMenuSet.contains(part)) {
                    if (lastCategory != null) {
                        addUnique(subMenus.get(lastCategory), part);
                    }
                    addUnique(nestedMenusContent.get(part), key);
                }
            }
        }
    }

    private static void addUnique(List<String> list, String value) {
        if (!list.contains(value)) {
            list.add(value);
        }
    }

    private int subMenuHeight() {
        List<String> items = subMenu.isEmpty() ? null : subMenus.get(subMenu);
        if (items == null) return 0;
        return items.size() * lineHeight() + LIST_PAD;
    }

    private int nestedMenuHeight() {
        if (openedNestedMenu.isEmpty()) return 0;
        List<String> items = nestedMenusContent.get(openedNestedMenu);
        if (items == null) return 0;
        return items.size() * lineHeight() + LIST_PAD;
    }

    private int panelLeft() {
        return getScreenWidth() / 2 - 188;
    }

    private int panelRight() {
        return getScreenWidth() / 2 + 188;
    }

    private int panelTop() {
        return getScreenHeight() / 2 - 138;
    }

    private int panelBottom() {
        return getScreenHeight() / 2 + 138;
    }

    public void setFont(Font font) {
        this.font = font;
        int lh = (font != null) ? font.lineHeight : 9;
        this.height = categories.size() * lh + 3;
    }

    private int lineHeight() {
        return font.lineHeight;
    }

    private List<String> displayList(List<String> keys) {
        return keys.stream().map(this::displayName).toList();
    }

    private String displayName(String key) {
        return nodeDisplayNames.getOrDefault(key, key);
    }

    private boolean isNestedMenu(String key) {
        return nestedMenusContent.containsKey(key);
    }

    private List<String> getOpenedNestedItems() {
        return nestedMenusContent.get(openedNestedMenu);
    }

    private int getCategoryIndex() {
        return categories.indexOf(subMenu);
    }

    private int getNestedMenuIndex(List<String> items) {
        return items.indexOf(openedNestedMenu);
    }

    private int getScreenWidth() {
        return screenWidth > 0 ? screenWidth : (screen != null ? screen.width : 854);
    }

    private int getScreenHeight() {
        return screenHeight > 0 ? screenHeight : (screen != null ? screen.height : 480);
    }

    private int subMenuWidth() {
        List<String> items = subMenu.isEmpty() ? null : subMenus.get(subMenu);
        if (items == null) return 0;
        return font.width(GuiUtils.getPixelLongestString(displaySubMenuList(items), font));
    }

    private int nestedMenuWidth() {
        if (openedNestedMenu.isEmpty()) return 0;
        List<String> items = nestedMenusContent.get(openedNestedMenu);
        if (items == null) return 0;
        return font.width(GuiUtils.getPixelLongestString(displayNodeList(items), font));
    }

    private void updateMainWidth() {
        width = font.width(GuiUtils.getPixelLongestString(displayMenuList(categories), font));
    }


    private int clampX(int value) {
        int sw = getScreenWidth();
        return Math.max(0, Math.clamp(sw - width - LIST_PAD, 0, value));
    }

    private int clampY(int value) {
        int sh = getScreenHeight();
        return Math.max(0, Math.clamp(sh - height - LIST_PAD, 0, value));
    }

    private int getSubX() {
        int subW = subMenuWidth();
        int rightX = x + width + 2;
        int leftX = x + 1 - (subW + LIST_PAD);

        if (rightX + subW + LIST_PAD <= panelRight()) return rightX;
        if (leftX >= panelLeft()) return leftX;
        if (rightX + subW + LIST_PAD <= getScreenWidth()) return rightX;
        return Math.max(0, leftX);
    }

    private int getSubY() {
        int lh = lineHeight();
        int baseY = y + getCategoryIndex() * lh;
        int subH = subMenuHeight();
        int sh = getScreenHeight();

        if (baseY + subH > sh) {
            baseY = Math.max(0, sh - subH);
        }
        return baseY;
    }

    private boolean isSubOnLeft() {
        return getSubX() < x;
    }

    private int getNestedX() {
        int subW = subMenuWidth();
        int nestedW = nestedMenuWidth();
        int subX = getSubX();

        int rightX = subX + subW + 2;
        int leftX = subX + 1 - (nestedW + LIST_PAD);

        if (isSubOnLeft() && leftX >= panelLeft()) {
            return leftX;
        }

        if (rightX + nestedW + LIST_PAD <= panelRight()) {
            return rightX;
        }
        if (leftX >= panelLeft()) {
            return leftX;
        }
        if (rightX + nestedW + LIST_PAD <= getScreenWidth()) {
            return rightX;
        }
        if (leftX >= 0) {
            return leftX;
        }
        return Math.max(0, getScreenWidth() - nestedW - LIST_PAD);
    }

    private int getNestedY(List<String> items) {
        int lh = lineHeight();
        int baseY = y + getCategoryIndex() * lh + getNestedMenuIndex(items) * lh;
        int nestedH = nestedMenuHeight();
        int sh = getScreenHeight();
        if (baseY + nestedH > sh) {
            baseY = Math.max(0, sh - nestedH);
        }
        return baseY;
    }
    private String menuName(String key) {
        return menuDisplayNames.getOrDefault(key, key);
    }

    private String nodeName(String key) {
        return nodeDisplayNames.getOrDefault(key, key);
    }

    private List<String> displayMenuList(List<String> keys) {
        return keys.stream().map(this::menuName).toList();
    }

    private List<String> displaySubMenuList(List<String> keys) {
        return keys.stream()
                .map(k -> isNestedMenu(k) ? menuName(k) : nodeName(k))
                .toList();
    }

    private List<String> displayNodeList(List<String> keys) {
        return keys.stream().map(this::nodeName).toList();
    }

    private static final int SEARCH_CHARS = 32;
    private static final int SEARCH_MAX_ROWS = 8;
    private static final int SEARCH_MAX_RECENT = 8;
    private static final int SEARCH_FIELD_HEIGHT = 12;
    private static final String SEARCH_SEPARATOR = " -> ";
    private static final int SEARCH_MAX_DEPTH = 8;
    private static final List<String> RECENT = new ArrayList<>();

    private record SearchEntry(String key, String name, String prefix,
                               String lowerName, String lowerKey, String haystack) {
        String path() {
            return prefix + name;
        }
    }

    private record ScoredEntry(SearchEntry entry, int score) {
    }

    private final TextField searchField = new TextField(0, 0, SEARCH_CHARS, SEARCH_FIELD_HEIGHT,
            0xFF1E1E1E, 0xFFFFFFFF, 0xFF699684);
    private final List<SearchEntry> searchIndex = new ArrayList<>();
    private List<SearchEntry> searchResults = new ArrayList<>();
    private String cachedQuery = null;
    private boolean searchOpened = false;
    private int searchAnchorX;
    private int searchAnchorY;
    private int searchX;
    private int searchY;
    private int searchWidth;
    private int searchHeight;
    private int searchHeaderHeight;
    private int selectedIndex;
    private int searchScroll;
    private int menuMouseX;
    private int menuMouseY;
    private int lastMouseX = -1;
    private int lastMouseY = -1;

    public boolean isSearchOpen() {
        return searchOpened;
    }

    private static String tr(String key, String fallback) {
        return Language.getInstance().has(key) ? Component.translatable(key).getString() : fallback;
    }

    private void buildSearchIndex() {
        searchIndex.clear();
        Set<Object> indexedKeys = new HashSet<>();
        for (String category : categories) {
            if (category.equals(CATEGORY_FIND)) {
                continue;
            }
            List<String> items = subMenus.get(category);
            if (items == null) {
                continue;
            }
            List path = new ArrayList<>();
            path.add(category);
            for (String item : items) {
                collectSearch(path, item, 0, indexedKeys);
            }
        }
    }

    private void collectSearch(List<String> parents, String key, int depth, Set indexedKeys) {
        if (depth > SEARCH_MAX_DEPTH) {
            return;
        }
        List<String> children = nestedMenusContent.get(key);
        if (children != null) {
            List<String> next = new ArrayList<>(parents);
            next.add(key);
            for (String child : children) {
                collectSearch(next, child, depth + 1, indexedKeys);
            }
            return;
        }
        if (!nodeTypeByKey.containsKey(key) || !indexedKeys.add(key)) {
            return;
        }
        StringBuilder prefix = new StringBuilder();
        for (String parent : parents) {
            prefix.append(menuName(parent)).append(SEARCH_SEPARATOR);
        }
        String name = nodeName(key);
        String lowerName = name.toLowerCase(Locale.ROOT);
        String lowerKey = key.replace('_', ' ').toLowerCase(Locale.ROOT);
        String haystack = lowerName + " " + lowerKey + " " + prefix.toString().toLowerCase(Locale.ROOT);
        searchIndex.add(new SearchEntry(key, name, prefix.toString(), lowerName, lowerKey, haystack));
    }

    private List<SearchEntry> computeResults(String rawQuery) {
        String query = rawQuery.trim().toLowerCase(Locale.ROOT);
        List<SearchEntry> result = new ArrayList<>();
        if (query.isEmpty()) {
            for (String key : RECENT) {
                for (SearchEntry entry : searchIndex) {
                    if (entry.key().equals(key)) {
                        result.add(entry);
                        break;
                    }
                }
            }
            return result;
        }
        String[] tokens = query.split("\\s+");
        List<ScoredEntry> found = new ArrayList<>();
        for (SearchEntry entry : searchIndex) {
            boolean matches = true;
            for (String token : tokens) {
                if (!entry.haystack().contains(token)) {
                    matches = false;
                    break;
                }
            }
            if (!matches) {
                continue;
            }
            int score;
            if (entry.lowerName().startsWith(query)) {
                score = 0;
            } else if (entry.lowerName().contains(" " + query)) {
                score = 1;
            } else if (entry.lowerName().contains(query)) {
                score = 2;
            } else if (entry.lowerKey().contains(query)) {
                score = 3;
            } else {
                score = 4;
            }
            found.add(new ScoredEntry(entry, score));
        }
        Comparator<ScoredEntry> order = Comparator.comparingInt(ScoredEntry::score);
        order = order.thenComparing(scored -> scored.entry().path());
        found.sort(order);
        for (ScoredEntry scored : found) {
            result.add(scored.entry());
        }
        return result;
    }

    private void refreshResults() {
        String query = searchField.getText();
        if (query.equals(cachedQuery)) {
            return;
        }
        cachedQuery = query;
        searchResults = computeResults(query);
        selectedIndex = 0;
        searchScroll = 0;
    }

    private void openSearch() {
        isOpened = false;
        subMenu = "";
        openedNestedMenu = "";
        buildSearchIndex();
        searchAnchorX = x;
        searchAnchorY = y;
        searchField.setText("");
        searchField.setFocused(true);
        cachedQuery = null;
        searchOpened = true;
        refreshResults();
    }

    private int rowHeight() {
        return lineHeight() + 1;
    }

    private String searchHeaderText() {
        if (!searchField.getText().trim().isEmpty()) {
            return tr("aeronet.nodemenu.no_results", "No results");
        }
        return searchResults.isEmpty()
                ? tr("aeronet.nodemenu.search_hint", "Type to search")
                : tr("aeronet.nodemenu.recent", "Recent");
    }

    private void updateSearchLayout() {
        refreshResults();
        int rh = rowHeight();
        boolean showHeader = searchField.getText().trim().isEmpty() || searchResults.isEmpty();
        searchHeaderHeight = showHeader ? rh : 0;
        int rows = Math.min(searchResults.size(), SEARCH_MAX_ROWS);
        searchWidth = searchField.getPreferredWidth(font) + 6;
        searchHeight = 3 + SEARCH_FIELD_HEIGHT + 3 + searchHeaderHeight + rows * rh + 2;
        int minX = panelLeft();
        int maxX = panelRight() - searchWidth;
        if (maxX < minX) {
            minX = 0;
            maxX = Math.max(0, getScreenWidth() - searchWidth);
        }
        int minY = panelTop();
        int maxY = panelBottom() - searchHeight;
        if (maxY < minY) {
            minY = 0;
            maxY = Math.max(0, getScreenHeight() - searchHeight);
        }
        searchX = Math.max(minX, Math.min(searchAnchorX, maxX));
        searchY = Math.max(minY, Math.min(searchAnchorY, maxY));
    }

    private boolean insideSearch(double mouseX, double mouseY) {
        return mouseX > searchX && mouseX < searchX + searchWidth
                && mouseY > searchY && mouseY < searchY + searchHeight;
    }

    private int searchRowsTop() {
        return searchY + 3 + SEARCH_FIELD_HEIGHT + 3 + searchHeaderHeight;
    }

    private int searchRowAt(double mouseX, double mouseY) {
        if (mouseX <= searchX || mouseX >= searchX + searchWidth) {
            return -1;
        }
        int relative = (int) mouseY - searchRowsTop();
        if (relative < 0) {
            return -1;
        }
        int row = relative / rowHeight();
        int visible = Math.min(searchResults.size() - searchScroll, SEARCH_MAX_ROWS);
        return row < visible ? searchScroll + row : -1;
    }

    private void ensureSelectedVisible() {
        if (selectedIndex < searchScroll) {
            searchScroll = selectedIndex;
        } else if (selectedIndex >= searchScroll + SEARCH_MAX_ROWS) {
            searchScroll = selectedIndex - SEARCH_MAX_ROWS + 1;
        }
    }

    private void pickSearchResult(int index) {
        if (index < 0 || index >= searchResults.size()) {
            return;
        }
        createNodeAt(searchResults.get(index).key(), menuMouseX, menuMouseY);
        isChanged = true;
    }

    private void rememberRecent(String key) {
        RECENT.remove(key);
        RECENT.add(0, key);
        while (RECENT.size() > SEARCH_MAX_RECENT) {
            RECENT.remove(RECENT.size() - 1);
        }
    }

    private void renderSearch(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateSearchLayout();
        searchField.setFocused(true);
        boolean mouseMoved = mouseX != lastMouseX || mouseY != lastMouseY;
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        GuiUtils.drawOutlinedRect(graphics, searchX, searchY, searchWidth, searchHeight,
                0xFF282828, 0xFF699684);
        searchField.setX(searchX + 3);
        searchField.setY(searchY + 3);
        searchField.render(graphics, mouseX, mouseY, partialTick, font);
        int rh = rowHeight();
        if (searchHeaderHeight > 0) {
            graphics.drawString(font, searchHeaderText(), searchX + 4,
                    searchY + 3 + SEARCH_FIELD_HEIGHT + 4, 0xFF9AA5A0);
        }
        int rowsTop = searchRowsTop();
        int visible = Math.min(searchResults.size() - searchScroll, SEARCH_MAX_ROWS);
        for (int i = 0; i < visible; i++) {
            int index = searchScroll + i;
            int rowY = rowsTop + i * rh;
            boolean hovered = mouseX > searchX && mouseX < searchX + searchWidth
                    && mouseY >= rowY && mouseY < rowY + rh;
            if (hovered && mouseMoved) {
                selectedIndex = index;
            }
            if (index == selectedIndex) {
                graphics.fill(searchX + 1, rowY, searchX + searchWidth - 1, rowY + rh, 0xAA000000);
            }
            drawPathRow(graphics, searchResults.get(index), searchX + 4, rowY + 1, searchWidth - 8);
        }
    }

    private void drawPathRow(GuiGraphics graphics, SearchEntry entry, int drawX, int drawY, int maxWidth) {
        String full = entry.path();
        String shown = full;
        boolean cut = false;
        if (font.width(full) > maxWidth) {
            shown = font.plainSubstrByWidth(full, Math.max(0, maxWidth - font.width("...")));
            cut = true;
        }
        int split = Math.min(entry.prefix().length(), shown.length());
        String gray = shown.substring(0, split);
        String white = shown.substring(split) + (cut ? "..." : "");
        graphics.drawString(font, gray, drawX, drawY, 0xFF9AA5A0);
        graphics.drawString(font, white, drawX + font.width(gray), drawY, 0xFFFFFFFF);
    }

    private boolean handleSearchClick(double mouseX, double mouseY, int button) {
        updateSearchLayout();
        if (!insideSearch(mouseX, mouseY)) {
            searchOpened = false;
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            int index = searchRowAt(mouseX, mouseY);
            if (index >= 0) {
                pickSearchResult(index);
            }
        }
        searchField.setFocused(true);
        return true;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!searchOpened) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            searchOpened = false;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            refreshResults();
            pickSearchResult(selectedIndex);
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

    public boolean charTyped(char codePoint, int modifiers) {
        if (!searchOpened) {
            return false;
        }
        searchField.setFocused(true);
        searchField.charTyped(codePoint, modifiers);
        return true;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
                       int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        if (isOpened) {
            // 1. Главный список категорий
            List<String> categoryLabels = displayMenuList(categories);
            updateMainWidth();
            x = clampX(x);
            y = clampY(y);
            int lh = lineHeight();
            GuiUtils.drawOutlinedList(graphics, font, x, y, categoryLabels,
                    0xFF282828, 0xFF699684, 0xFFFFFF);
            for (int i = 0; i < categories.size(); i++) {
                if (mouseY > y + i * lh + 1
                        && mouseY < y + i * lh + lh + 1
                        && mouseX > x
                        && mouseX < x + width + LIST_PAD) {
                    graphics.fill(x + 1, y + i * lh + 1,
                            x + width + 2, y + i * lh + lh + 2,
                            0xAA000000);
                }
            }
            List<String> items = subMenu.isEmpty() ? null : subMenus.get(subMenu);
            if (items != null && !subMenu.equals(CATEGORY_FIND)) {
                List<String> itemLabels = displaySubMenuList(items);
                int subX = getSubX();
                int subY = getSubY();
                GuiUtils.drawOutlinedList(graphics, font, subX, subY, itemLabels,
                        0xFF282828, 0xFF699684, 0xFFFFFF);
                for (int i = 0; i < items.size(); i++) {
                    if (mouseY > subY + i * lh + 1
                            && mouseY < subY + i * lh + lh + 1
                            && mouseX > subX
                            && mouseX < subX + subMenuWidth() + LIST_PAD) {
                        graphics.fill(subX + 1,
                                subY + i * lh + 1,
                                subX + subMenuWidth() + 2,
                                subY + i * lh + lh + 2,
                                0xAA000000);
                    }
                }
                if (!openedNestedMenu.isEmpty() && items.contains(openedNestedMenu)) {
                    List<String> nestedItems = getOpenedNestedItems();
                    if (nestedItems != null) {
                        List<String> nestedLabels = displayNodeList(nestedItems);
                        int nestedX = getNestedX();
                        int nestedY = getNestedY(items);
                        GuiUtils.drawOutlinedList(graphics, font, nestedX, nestedY,
                                nestedLabels, 0xFF282828, 0xFF699684, 0xFFFFFF);
                        for (int i = 0; i < nestedItems.size(); i++) {
                            if (mouseY > nestedY + i * lh + 1
                                    && mouseY < nestedY + i * lh + lh + 1
                                    && mouseX > nestedX
                                    && mouseX < nestedX + nestedMenuWidth() + LIST_PAD) {
                                graphics.fill(nestedX + 1, nestedY + i * lh + 1,
                                        nestedX + nestedMenuWidth() + 2,
                                        nestedY + i * lh + lh + 2,
                                        0xAA000000);
                            }
                        }
                    }
                }
            }
        }
        if (searchOpened) {
            renderSearch(graphics, mouseX, mouseY, partialTick);
        }
        clickedThisFrame = false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (searchOpened) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                searchOpened = false;
            } else {
                return handleSearchClick(mouseX, mouseY, button);
            }
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (!isOpened) {
                isOpened = true;
                openedNestedMenu = "";
                updateMainWidth();
                menuMouseX = (int) mouseX;
                menuMouseY = (int) mouseY;
                x = clampX((int) mouseX);
                y = clampY((int) mouseY);
            }
            return true;
        }
        if (!isOverMenu((int) mouseX, (int) mouseY)) {
            isOpened = false;
            openedNestedMenu = "";
            subMenu = "";
            return true;
        }
        clickedThisFrame = true;
        if (isOpened) {
            int lh = lineHeight();
            for (int i = 0; i < categories.size(); i++) {
                if (mouseY > y + i * lh + 1
                        && mouseY < y + i * lh + lh + 1
                        && mouseX > x
                        && mouseX < x + width + LIST_PAD) {
                    String selectedCategory = categories.get(i);
                    if (selectedCategory.equals(CATEGORY_FIND)) {
                        openSearch();
                        return true;
                    }
                    if (!selectedCategory.equals(subMenu)) {
                        openedNestedMenu = "";
                    }
                    subMenu = selectedCategory;
                    break;
                }
            }
            List<String> items = subMenu.isEmpty() ? null : subMenus.get(subMenu);
            if (items != null && !subMenu.equals(CATEGORY_FIND)) {
                int subX = getSubX();
                int subY = getSubY();
                for (int i = 0; i < items.size(); i++) {
                    if (mouseY > subY + i * lh + 1
                            && mouseY < subY + i * lh + lh + 1
                            && mouseX > subX
                            && mouseX < subX + subMenuWidth() + LIST_PAD) {
                        String key = items.get(i);
                        if (isNestedMenu(key)) {
                            openedNestedMenu = openedNestedMenu.equals(key) ? "" : key;
                        } else {
                            createNodeAt(key, (int) mouseX, (int) mouseY);
                            isChanged = true;
                        }
                        return true;
                    }
                }
                if (!openedNestedMenu.isEmpty() && items.contains(openedNestedMenu)) {
                    List<String> nestedItems = getOpenedNestedItems();
                    if (nestedItems != null) {
                        int nestedX = getNestedX();
                        int nestedY = getNestedY(items);
                        for (int i = 0; i < nestedItems.size(); i++) {
                            if (mouseY > nestedY + i * lh + 1
                                    && mouseY < nestedY + i * lh + lh + 1
                                    && mouseX > nestedX
                                    && mouseX < nestedX + nestedMenuWidth() + LIST_PAD) {
                                createNodeAt(nestedItems.get(i), (int) mouseX, (int) mouseY);
                                isChanged = true;
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean isOverMenu(int mouseX, int mouseY) {
        if (searchOpened) {
            updateSearchLayout();
            return insideSearch(mouseX, mouseY);
        }
        if (!isOpened) return false;
        int lh = lineHeight();
        if (mouseX > x && mouseX < x + width + LIST_PAD
                && mouseY > y && mouseY < y + height) {
            return true;
        }
        if (subMenu.isEmpty()) return false;
        List items = subMenus.get(subMenu);
        if (items == null || subMenu.equals(CATEGORY_FIND)) return false;
        int subX = getSubX();
        int subY = getSubY();
        for (int i = 0; i < items.size(); i++) {
            if (mouseY > subY + i * lh + 1
                    && mouseY < subY + i * lh + lh + 1
                    && mouseX > subX
                    && mouseX < subX + subMenuWidth() + LIST_PAD) {
                return true;
            }
        }
        if (!openedNestedMenu.isEmpty() && items.contains(openedNestedMenu)) {
            List nestedItems = getOpenedNestedItems();
            if (nestedItems != null) {
                int nestedX = getNestedX();
                int nestedY = getNestedY(items);
                if (mouseX > nestedX
                        && mouseX < nestedX + nestedMenuWidth() + LIST_PAD
                        && mouseY > nestedY
                        && mouseY < nestedY + nestedItems.size() * lh + 3) {
                    return true;
                }
            }
        }
        return false;
    }

    private void createNodeAt(String itemKey, int mouseX, int mouseY) {
        NodeType type = nodeTypeByKey.get(itemKey);
        if (type == null || screen == null) return;
        int offsetX = (int) Math.floor(screen.canvasX(mouseX)) - getScreenWidth() / 2;
        int offsetY = (int) Math.floor(screen.canvasY(mouseY)) - getScreenHeight() / 2;
        NodesData data = new NodesData(
                UUID.randomUUID(), offsetX, offsetY, 27, 27, 0, 0,
                new ArrayList<>(), new ArrayList<>(),
                new CompoundTag(), type
        );
        screen.createNode(data);
        rememberRecent(itemKey);
        searchOpened = false;
        isOpened = false;
        subMenu = "";
        openedNestedMenu = "";
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean isLeft = false;
        boolean isRight = false;
        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (searchOpened) {
            updateSearchLayout();
            if (insideSearch(mouseX, mouseY)) {
                int max = Math.max(0, searchResults.size() - SEARCH_MAX_ROWS);
                searchScroll = Math.max(0, Math.min(max, searchScroll - (int) Math.signum(scrollY)));
            }
        }
        return true;
    }

    public void setScreen(ServerSreen screen) {
        this.screen = screen;
    }

    public boolean getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(boolean isChanged) {
        this.isChanged = isChanged;
    }
}