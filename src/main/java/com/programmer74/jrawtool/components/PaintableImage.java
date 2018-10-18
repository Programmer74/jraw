package com.programmer74.jrawtool.components;

import java.awt.*;
import java.util.function.Consumer;

public interface PaintableImage {
  int getWidth();

  int getHeight();

  void paintPreviewOnGraphics(Graphics g,
                              int paintX, int paintY, int paintW, int paintH,
                              int windowWidth, int windowHeight);

  void setParent(final Component parent);

  void markSlowPreviewDirty();

  void markPreviewDirty();

  void markDirty();

  void setAfterChunkPaintedCallback(
      final Consumer<Integer> afterChunkPaintedCallback);

  void setAfterSlowPreviewRenderingBeginCallback(
      final Consumer<Integer> afterSlowPreviewRenderingBeginCallback);

  void setAfterSlowPreviewRenderingEndCallback(
      final Consumer<Integer> afterSlowPreviewRenderingEndCallback);
}
