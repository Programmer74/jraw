package com.programmer74.jrawtool.doubleimage;

import java.util.function.Consumer;

public class DoubleImageUtils {

  public static DoubleImage getRotatedImage(DoubleImage original, double angle, Consumer<String> status) {
    int i, j, x, y, new_x, new_y;
    double v_sin, v_cos;
    double[] neutral = new double[]{255, 255, 255};

    angle = angle * Math.PI / 180.0f;
    v_cos = Math.cos(angle);
    v_sin = Math.sin(angle);

    int width = original.getWidth();
    int height = original.getHeight();

    int newWidth = (int)Math.max(width * v_cos - height * v_sin, width * v_cos + height * v_sin);
    int newHeight = (int)Math.max(width * v_sin + height * v_cos, -width * v_sin + height * v_cos);
    DoubleImage result = new DoubleImage(newWidth, newHeight, original.getDefaultValues());

    for (i = 0; i < newWidth; i++) {
      for (j = 0; j < newHeight; j++) {
        x = i - newWidth / 2;
        y = j - newHeight / 2;
        new_x = (int)(((x * v_cos - y * v_sin)) + width / 2);
        new_y = (int)(((x * v_sin + y * v_cos)) + height / 2);
        if ((new_x >= width) || (new_y >= height) || (new_x < 0) || (new_y < 0)) {
          result.setPixel(i, j, neutral);
        } else {
          result.setPixel(i, j, original.getPixel(new_x, new_y));
        }
      }
      if (i % 50 == 0) {
        status.accept("Rotation: " + (i * 100.0 / newWidth) + "%");
      }
    }
    return result;
  }

  public static DoubleImage getCroppedImage(DoubleImage original, int orgOffsetX, int orgOffsetY, int cropWidth, int cropHeight) {
    DoubleImage result = new DoubleImage(cropWidth, cropHeight, original.getDefaultValues());
    for (int i = 0; i < cropWidth; i++) {
      for (int j = 0; j < cropHeight; j++) {
        int fromx = i + orgOffsetX;
        int fromy = j + orgOffsetY;
        if (fromx >= original.getWidth()) fromx = original.getWidth() - 1;
        if (fromy >= original.getHeight()) fromy = original.getHeight() - 1;
        result.setPixel(i, j, original.getPixel(fromx, fromy));
      }
    }
    return result;
  }

}
