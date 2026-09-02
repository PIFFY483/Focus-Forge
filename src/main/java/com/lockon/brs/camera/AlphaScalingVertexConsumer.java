package com.lockon.brs.camera;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;

public class AlphaScalingVertexConsumer implements VertexConsumer {

    private final VertexConsumer delegate;
    private final float alphaScale;

    public AlphaScalingVertexConsumer(VertexConsumer delegate, float alphaScale) {
        this.delegate = delegate;
        this.alphaScale = Mth.clamp(alphaScale, 0.0f, 1.0f);
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        delegate.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        int scaledA = Mth.clamp(Math.round(a * alphaScale), 0, 255);
        delegate.color(r, g, b, scaledA);
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        delegate.uv(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        delegate.overlayCoords(u, v);
        return this;
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        delegate.uv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        delegate.normal(x, y, z);
        return this;
    }

    @Override
    public void defaultColor(int r, int g, int b, int a) {
        int scaledA = Mth.clamp(Math.round(a * alphaScale), 0, 255);
        delegate.defaultColor(r, g, b, scaledA);
    }

    @Override
    public void unsetDefaultColor() {
        delegate.unsetDefaultColor();
    }

    @Override
    public void endVertex() {
        delegate.endVertex();
    }
}