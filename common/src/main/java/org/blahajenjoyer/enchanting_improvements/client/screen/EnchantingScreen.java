package org.blahajenjoyer.enchanting_improvements.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import org.blahajenjoyer.enchanting_improvements.menu.EnchantMenu;
import org.blahajenjoyer.enchanting_improvements.EnchantingImprovements;

public class EnchantingScreen extends AbstractContainerScreen<EnchantMenu> {
    private static final ResourceLocation BOOK_TEXTURE = new ResourceLocation("textures/entity/enchanting_table_book.png");
    private BookModel bookModel;
    private final RandomSource random = RandomSource.create();
    private float time;
    private float open, oOpen;
    private float flip, oFlip, flipT, flipA;


    private static final ResourceLocation TEX = new ResourceLocation(EnchantingImprovements.MOD_ID, "textures/gui/enchanting_table.png");

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
        this.time += 1.0F;

        boolean hasItem = this.menu.getSlot(0).hasItem();

        this.oOpen = this.open;
        this.oFlip = this.flip;

        float target = hasItem ? 1.0F : 0.0F;
        this.open = Mth.clamp(this.open + (target - this.open) * 0.2F, 0.0F, 1.0F);

        this.flipT += 0.1F;
        if (this.flipT > (float)Math.PI * 2F) this.flipT -= (float)Math.PI * 2F;
        this.flipA += (this.flipT - this.flipA) * 0.9F;
        this.flip += 0.05F + (hasItem ? 0.0F : 0.02F) + (this.random.nextFloat() - 0.5F) * 0.02F;
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

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mouseX, int mouseY) {
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