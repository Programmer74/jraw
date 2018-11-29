package com.programmer74.jrawtool.byteimage;

import com.programmer74.jrawtool.components.PaintableImage;
import com.programmer74.jrawtool.doubleimage.BufferedImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class ByteImage implements PaintableImage {

  private BufferedImage bufferedImage;

  public ByteImage(final BufferedImage bufferedImage) {
    this.bufferedImage = bufferedImage;
  }

  public BufferedImage getBufferedImage() {
    return bufferedImage;
  }

  @Override public int getWidth() {
    return bufferedImage.getWidth();
  }

  @Override public int getHeight() {
    return bufferedImage.getHeight();
  }

  @Override public void paintPreviewOnGraphics(final Graphics g, final int paintX, final int paintY,
      final int paintW,
      final int paintH, final int windowWidth, final int windowHeight) {

    g.drawImage(bufferedImage, paintX, paintY, paintW, paintH, null);
  }

  @Override
  public BufferedImage burnPreview(int maxWidth) {
    return BufferedImageUtils.getShrinkedImage(bufferedImage, maxWidth);
  }

  @Override public void setParent(final Component parent) {

  }

  @Override public void markSlowPreviewDirty() {

  }

  @Override public void markPreviewDirty() {

  }

  @Override public void markDirty() {

  }

  @Override public void setAfterChunkPaintedCallback(
      final Consumer<Integer> afterChunkPaintedCallback) {

  }

  @Override public void setAfterSlowPreviewRenderingBeginCallback(
      final Consumer<Integer> afterSlowPreviewRenderingBeginCallback) {

  }

  @Override public void setAfterSlowPreviewRenderingEndCallback(
      final Consumer<Integer> afterSlowPreviewRenderingEndCallback) {

  }
}
