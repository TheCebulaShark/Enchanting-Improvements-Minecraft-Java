package org.blahajenjoyer.enchanting_improvements.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.blahajenjoyer.enchanting_improvements.menu.EnchantMenu;
import org.blahajenjoyer.enchanting_improvements.EnchantingImprovements;

public class EnchantingScreen extends AbstractContainerScreen<EnchantMenu> {
    private static final ResourceLocation BOOK_TEXTURE = new ResourceLocation("textures/entity/enchanting_table_book.png");
    private BookModel bookModel;
    private final RandomSource random = RandomSource.create();
    private float open, oOpen;
    private float flip, oFlip, flipT, flipA;


    private static final ResourceLocation TEX = new ResourceLocation(EnchantingImprovements.MOD_ID, "textures/gui/enchanting_table.png");

    private static final int ENCHANTING_VIEW_START_X = 56;
    private static final int ENCHANTING_VIEW_START_Y = 14;
    private static final int ENCHANTING_VIEW_END_X = 174;
    private static final int ENCHANTING_VIEW_END_Y = 117;
    private static final int ENCHANTING_VIEW_WIDTH = ENCHANTING_VIEW_END_X - ENCHANTING_VIEW_START_X;
    private static final int ENCHANTING_VIEW_HEIGHT = ENCHANTING_VIEW_END_Y - ENCHANTING_VIEW_START_Y;


    public EnchantingScreen(EnchantMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 184;
        this.imageHeight = 212;
    }

    @Override
    protected void init() {
        super.init();
        this.bookModel = new BookModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.BOOK));
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        boolean hasItem = this.menu.getSlot(0).hasItem();

        this.oOpen = this.open;
        this.oFlip = this.flip;

        float target = hasItem ? 1.0F : 0.0F;
        this.open = Mth.clamp(this.open + (target - this.open) * 0.2F, 0.0F, 1.0F);

        this.flipT += 0.1F;
        if (this.flipT > (float)Math.PI * 2F) this.flipT -= (float)Math.PI * 2F;
        this.flipA += (this.flipT - this.flipA) * 0.9F;
        this.flip += 0.05F + (hasItem ? 0.0F : 0.02F) + (this.random.nextFloat() - 0.5F) * 0.02F;

        ItemStack cur = this.menu.getSlot(0).getItem();
        if (!ItemStack.isSameItemSameTags(cur, lastItem)) {
            lastItem = cur.copy();
            rebuildTreeFor(cur);
        }
    }

    private void drawBook(GuiGraphics gg, int x, int y, float delta) {
        float f = Mth.lerp(delta, this.oOpen, this.open);
        float g = Mth.lerp(delta, this.oFlip, this.flip);

        var pose = gg.pose();
        pose.pushPose();

        pose.translate(x + 32.0F, y + 32.0F, 100.0F);

        pose.scale(-40.0F, 40.0F, 40.0F);
        pose.mulPose(Axis.XP.rotationDegrees(25.0F));

        pose.translate((1.0F - f) * 0.2F, (1.0F - f) * 0.1F, (1.0F - f) * 0.25F);

        float yaw = -(1.0F - f) * 90.0F - 90.0F;
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.mulPose(Axis.XP.rotationDegrees(180.0F));

        float j = Mth.clamp(Mth.frac(g + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
        float k = Mth.clamp(Mth.frac(g + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
        this.bookModel.setupAnim(0.0F, j, k, f);

        var buf = this.minecraft.renderBuffers().bufferSource();
        this.bookModel.renderToBuffer(
                pose,
                buf.getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucent(BOOK_TEXTURE)),
                0xF000F0, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                1F, 1F, 1F, 1F
        );
        buf.endBatch();
        pose.popPose();
    }

    private static final ResourceLocation TEX_VIEWPORT_IDLE = new ResourceLocation(EnchantingImprovements.MOD_ID, "textures/gui/enchanting_bg.png");
    private static final ResourceLocation TEX_VIEWPORT_ACTIVE = new ResourceLocation(EnchantingImprovements.MOD_ID, "textures/gui/enchanting_active_bg.png");

    private static final int TILE = 16;

    private static final int U_HLINE = 184;
    private static final int V_HLINE = 0;
    private static final int HLINE_W  = 16;
    private static final int HLINE_H  = 2;

    private static final int U_VLINE = 184;
    private static final int V_VLINE = 0;
    private static final int VLINE_W  = 2;
    private static final int VLINE_H  = 8;


    private static final int ROW_H = 18;
    private static final int PADDING_X = 8;
    private static final int PADDING_Y = 8;
    private static final int LABEL_GAP = 6;
    private static final int STEM_THICK = 2;
    private static final int BOOK_SIZE = 16;

    private boolean treeActive = false;
    private ItemStack lastItem = ItemStack.EMPTY;

    private static class Row {
        net.minecraft.world.item.enchantment.Enchantment ench;
        int maxLvl;
        Component label;
    }

    private final java.util.List<Row> rows = new java.util.ArrayList<>();
    private int labelWidth = 0;

    private int contentW = 210;
    private int contentH = 180;

    private double panX = 0, panY = 0;
    private boolean dragging = false;
    private double dragLastX = 0, dragLastY = 0;
    private double zoom = 1.0;
    private static final double MIN_ZOOM = 0.65;
    private static final double MAX_ZOOM = 2.25;

    private static final String LKEY_LABEL_WRAP = "screen." + EnchantingImprovements.MOD_ID + ".enchant_label";

    private void clampPan() {
        double scaledW = contentW * zoom;
        double scaledH = contentH * zoom;
        double minX = Math.min(0.0, ENCHANTING_VIEW_WIDTH - scaledW);
        double minY = Math.min(0.0, ENCHANTING_VIEW_HEIGHT - scaledH);
        panX = Mth.clamp(panX, minX, 0.0);
        panY = Mth.clamp(panY, minY, 0.0);
    }

    private boolean insideViewport(double mouseX, double mouseY) {
        int x0 = leftPos + ENCHANTING_VIEW_START_X, y0 = topPos + ENCHANTING_VIEW_START_Y;
        int x1 = leftPos + ENCHANTING_VIEW_END_X, y1 = topPos + ENCHANTING_VIEW_END_Y;
        return mouseX >= x0 && mouseX < x1 && mouseY >= y0 && mouseY < y1;
    }

    private void drawViewportBackground(GuiGraphics g, int vpX0, int vpY0) {
        ResourceLocation tex = treeActive ? TEX_VIEWPORT_ACTIVE : TEX_VIEWPORT_IDLE;
        int offX = Math.floorMod((int)Math.floor(-panX * zoom), TILE);
        int offY = Math.floorMod((int)Math.floor(-panY * zoom), TILE);
        for (int y = -offY; y < ENCHANTING_VIEW_HEIGHT; y += TILE) {
            for (int x = -offX; x < ENCHANTING_VIEW_WIDTH; x += TILE) {
                g.blit(tex, vpX0 + x, vpY0 + y, 0, 0, TILE, TILE);
            }
        }
    }

    private void blitHLineFromTex(GuiGraphics g, int x, int y, int len) {
        int drawn = 0;
        while (drawn < len) {
            int w = Math.min(HLINE_W, len - drawn);
            g.blit(TEX, x + drawn, y, U_HLINE, V_HLINE, w, HLINE_H);
            drawn += w;
        }
    }

    private void blitVLineFromTex(GuiGraphics g, int x, int y, int len) {
        int drawn = 0;
        while (drawn < len) {
            int h = Math.min(VLINE_H, len - drawn);
            g.blit(TEX, x, y + drawn, U_VLINE, V_VLINE, VLINE_W, h);
            drawn += h;
        }
    }


    private void renderTreeViewport(GuiGraphics g) {
        int vpX0 = leftPos + ENCHANTING_VIEW_START_X;
        int vpY0 = topPos  + ENCHANTING_VIEW_START_Y;
        int vpX1 = leftPos + ENCHANTING_VIEW_END_X;
        int vpY1 = topPos  + ENCHANTING_VIEW_END_Y;

        g.enableScissor(vpX0, vpY0, vpX1, vpY1);
        drawViewportBackground(g, vpX0, vpY0);

        if (treeActive && !rows.isEmpty()) {
            var pose = g.pose();
            pose.pushPose();
            pose.translate(vpX0 + panX, vpY0 + panY, 0);
            pose.scale((float) zoom, (float) zoom, 1.0F);

            int left = PADDING_X;
            int boundaryX = left + labelWidth + LABEL_GAP;
            int y = PADDING_Y + ROW_H / 2;

            for (int i = 0; i < rows.size(); i++) {
                Row r = rows.get(i);

                int labelX = boundaryX - LABEL_GAP - this.font.width(r.label);
                g.drawString(this.font, r.label, labelX, y - this.font.lineHeight / 2, 0xFFFFFFFF, false);

                if (i > 0) {
                    int prevY = PADDING_Y + (i - 1) * ROW_H + ROW_H / 2;
                    blitVLineFromTex(g, boundaryX + 1, prevY, y - prevY);
                }
                if (i < rows.size() - 1) {
                    int nextY = PADDING_Y + (i + 1) * ROW_H + ROW_H / 2;
                    blitVLineFromTex(g, boundaryX + 1, y, nextY - y);
                }

                int startX = boundaryX + STEM_THICK;
                for (int lvl = 1; lvl <= r.maxLvl; lvl++) {
                    int seg = (lvl < r.maxLvl) ? 24 : 12;
                    blitHLineFromTex(g, startX, y - HLINE_H / 2, seg);

                    int bookCenterX = (lvl < r.maxLvl) ? (startX + seg / 2) : (startX + seg);
                    g.renderFakeItem(new ItemStack(Items.ENCHANTED_BOOK),
                            bookCenterX - BOOK_SIZE / 2, y - BOOK_SIZE / 2);

                    startX += seg;
                }

                y += ROW_H;
            }

            pose.popPose();
        }

        g.disableScissor();
    }

    private void rebuildTreeFor(ItemStack stack) {
        rows.clear();
        labelWidth = 0;
        treeActive = !stack.isEmpty();

        if (!treeActive) {
            contentW = ENCHANTING_VIEW_WIDTH;
            contentH = ENCHANTING_VIEW_HEIGHT;
            panX = panY = 0;
            return;
        }

        for (var ench : net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT) {
            if (!ench.isDiscoverable()) continue;
            if (!ench.canEnchant(stack)) continue;

            Row r = new Row();
            r.ench = ench;
            r.maxLvl = Math.max(1, ench.getMaxLevel());

            Component base = Component.translatable(ench.getDescriptionId());

            r.label = Component.translatable(LKEY_LABEL_WRAP, base);

            rows.add(r);
            labelWidth = Math.max(labelWidth, this.font.width(r.label));
        }

        rows.sort(java.util.Comparator.comparing(r -> r.label.getString()));

        int maxLevels = rows.stream().mapToInt(r -> r.maxLvl).max().orElse(1);
        int left = PADDING_X;
        int boundaryX = left + labelWidth + LABEL_GAP;
        int sumH = (maxLevels - 1) * 24 + 12;
        int treeRight = boundaryX + STEM_THICK + sumH + BOOK_SIZE / 2;
        int top = PADDING_Y;
        int bottom = top + rows.size() * ROW_H;

        contentW = treeRight + PADDING_X;
        contentH = bottom + PADDING_Y;

        clampPan();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (treeActive  && button == 0 && insideViewport(mouseX, mouseY)) {
            dragging = true;
            dragLastX = mouseX;
            dragLastY = mouseY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!treeActive && !insideViewport(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }

        double old = zoom;
        double next = Mth.clamp(old + delta * 0.1, MIN_ZOOM, MAX_ZOOM);
        if (next == old) return true;

        int vpX0 = leftPos + ENCHANTING_VIEW_START_X;
        int vpY0 = topPos  + ENCHANTING_VIEW_START_Y;

        double worldX = (mouseX - vpX0 - panX) / old;
        double worldY = (mouseY - vpY0 - panY) / old;

        zoom = next;
        panX = mouseX - vpX0 - worldX * zoom;
        panY = mouseY - vpY0 - worldY * zoom;

        clampPan();
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (treeActive && dragging && button == 0) {
            panX += (mouseX - dragLastX);
            panY += (mouseY - dragLastY);
            dragLastX = mouseX;
            dragLastY = mouseY;
            clampPan();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (treeActive && button == 0 && dragging) {
            dragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }


    @Override
    protected void renderBg(GuiGraphics g, float pt, int mouseX, int mouseY) {
        renderTreeViewport(g);

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        g.blit(TEX, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        drawBook(g, leftPos, topPos, pt);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, this.title, 12, 10, 0x404040, false);

        int invLabelX = 11;
        int invLabelY = 118;
        g.drawString(this.font, this.playerInventoryTitle, invLabelX, invLabelY, 0x404040, false);
    }
}