package com.elytradev.wings.client.gui;

import com.elytradev.wings.inventory.ContainerConverter;
import com.elytradev.wings.tile.TileEntityConverter;
import com.google.common.collect.Lists;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidTank;

public class GuiConverter extends GuiContainer {

	private static final ResourceLocation BG = new ResourceLocation("wings", "textures/gui/converter.png");
	
	private ContainerConverter container;
	
	public GuiConverter(ContainerConverter container) {
		super(container);
		this.container = container;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1, 1, 1);
		mc.getTextureManager().bindTexture(BG);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String s = container.te.getDisplayName().getUnformattedText();
		fontRenderer.drawString(s, xSize / 2 - fontRenderer.getStringWidth(s) / 2, 6, 0x404040);
		fontRenderer.drawString(container.playerInventory.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 0x404040);
		
		GlStateManager.color(1, 1, 1);
		
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		drawFluid(container.te.inputTank, 8, 9, 60);
		drawFluid(container.te.outputTank, 152, 9, 60);
		
		mc.getTextureManager().bindTexture(BG);
		drawTexturedModalRect(8, 9, 176, 18, 16, 60);
		drawTexturedModalRect(152, 9, 176, 18, 16, 60);
		
		drawTexturedModalRect(68, 32, 176, 0, (int)((container.progress/(double)TileEntityConverter.OPERATION_TIME)*63), 17);
		
		GlStateManager.pushMatrix();
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		GlStateManager.translate(-x, -y, 0);
		
		if (mouseX-x >= 8 && mouseX-x <= 8+16
				&& mouseY-y >= 9 && mouseY-y <= 9+60) {
			drawHoveringText(Lists.newArrayList(
					container.te.inputTank.getFluid() == null ? I18n.format("fluid.wings.empty") : container.te.inputTank.getFluid().getLocalizedName(),
					"\u00A77"+container.te.inputTank.getFluidAmount()+"/"+container.te.inputTank.getCapacity()+" mB"
					), mouseX, mouseY);
		}
		if (mouseX-x >= 152 && mouseX-x <= 152+16
				&& mouseY-y >= 9 && mouseY-y <= 9+60) {
			drawHoveringText(Lists.newArrayList(
					container.te.outputTank.getFluid() == null ? I18n.format("fluid.wings.empty") : container.te.outputTank.getFluid().getLocalizedName(),
					"\u00A77"+container.te.outputTank.getFluidAmount()+"/"+container.te.outputTank.getCapacity()+" mB"
					), mouseX, mouseY);
		}
		GlStateManager.popMatrix();
	}

	private void drawFluid(FluidTank fluid, int x, int y, int h) {
		if (fluid.getFluidAmount() <= 0) return;
		double amt = fluid.getFluidAmount()/(double)fluid.getCapacity();
		TextureAtlasSprite tex = mc.getTextureMapBlocks().getAtlasSprite(fluid.getFluid().getFluid().getStill(fluid.getFluid()).toString());
		
		int fh = (int)Math.floor(amt*h);
		
		int fullSquares = fh/16;
		if (fluid.getFluid().getFluid().isGaseous(fluid.getFluid())) {
			
		} else {
			int yc = y+h;
			for (int i = 0; i < fullSquares; i++) {
				drawRect(x, yc-16, tex, 16, 16, false);
				yc -= 16;
			}
			int lastHeight = fh%16;
			drawRect(x, yc-lastHeight, tex, 16, lastHeight, true);
		}
	}

	private void drawRect(int x, int y, TextureAtlasSprite tex, int w, int h, boolean flipped) {
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder bb = tess.getBuffer();
		
		float minU = tex.getInterpolatedU(0);
		float maxU = tex.getInterpolatedU(w);
		
		float minV = tex.getInterpolatedV(flipped ? h : 0);
		float maxV = tex.getInterpolatedV(flipped ? 0 : h);
		
		bb.begin(7, DefaultVertexFormats.POSITION_TEX);
		bb.pos(x + 0, y + h, zLevel).tex(minU, maxV).endVertex();
		bb.pos(x + w, y + h, zLevel).tex(maxU, maxV).endVertex();
		bb.pos(x + w, y + 0, zLevel).tex(maxU, minV).endVertex();
		bb.pos(x + 0, y + 0, zLevel).tex(minU, minV).endVertex();
		tess.draw();
	}

}
