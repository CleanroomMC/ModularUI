package io.github.cleanroommc.modularui.texture;

import net.minecraft.util.ResourceLocation;

public class UITexture {

	public static UITexture resolveFully(ResourceLocation location) {
		return new UITexture(location, 0.0, 0.0, 1.0, 1.0);
	}

	public static UITexture resolveFully(String location) {
		return resolveFully(new ResourceLocation(location));
	}

	public static UITexture resolveFully(String domain, String path) {
		return resolveFully(new ResourceLocation(domain, path));
	}

	public static UITexture resolvePartially(ResourceLocation location, int imageX, int imageY, int u, int v, int width, int height) {
		return new UITexture(location, u / (imageX * 1.0), v / (imageY * 1.0), (u + width) / (imageX * 1.0), (v + height) / (imageY * 1.0));
	}

	public static UITexture resolvePartially(String location, int imageX, int imageY, int u, int v, int width, int height) {
		return resolvePartially(new ResourceLocation(location), imageX, imageY, u, v, width, height);
	}

	public static UITexture resolvePartially(String domain, String path, int imageX, int imageY, int u, int v, int width, int height) {
		return resolvePartially(new ResourceLocation(domain, path), imageX, imageY, u, v, width, height);
	}

	private final ResourceLocation location;

	private final double offsetX;
	private final double offsetY;

	private final double imageWidth;
	private final double imageHeight;

	protected UITexture(ResourceLocation location, double offsetX, double offsetY, double width, double height) {
		this.location = location;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.imageWidth = width;
		this.imageHeight = height;
	}

	public ResourceLocation getLocation() {
		return location;
	}

	public double getOffsetX() {
		return offsetX;
	}

	public double getOffsetY() {
		return offsetY;
	}

	public double getImageWidth() {
		return imageWidth;
	}

	public double getImageHeight() {
		return imageHeight;
	}

	public UITexture getSection(double offsetX, double offsetY, double width, double height) {
		return new UITexture(location, this.offsetX + (imageWidth * offsetX), this.offsetY + (imageHeight * offsetY), this.imageWidth * width, this.imageHeight * height);
	}

}
