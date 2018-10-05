package com.programmer74.jrawtool.doubleimage;

public class DoubleImageKernelMatrixGenerator {
  final double[][] defaultConvMatrix = {
      {0, 0, 0, 0, 0},
      {0, 0, 0, 0, 0},
      {0, 0, 1, 0, 0},
      {0, 0, 0, 0, 0},
      {0, 0, 0, 0, 0}
  };
  final double defaultConvMatrixDivider = 1.0;
  final double[][] unsharpnessMaskingConvMatrix = {
      {1, 4, 6, 4, 1},
      {4, 16, 24, 16, 4},
      {6, 24, -476, 24, 6},
      {4, 16, 24, 16, 4},
      {1, 4, 6, 4, 1}
  };
  //1) calculate gaussian matrix
  //2) divider = -divider
  //3) mid_value = (divider * 2) + mid_value
  final double unsharpnessMaskingMatrixDivider = -256.0;
  final double[][] gaussianConvMatrix = {
      {1, 4, 6, 4, 1},
      {4, 16, 24, 16, 4},
      {6, 24, 36, 24, 6},
      {4, 16, 24, 16, 4},
      {1, 4, 6, 4, 1}
  };
  final double gaussianMatrixDivider = 256.0;
  final double[][] sharpnessConvMatrix = {{0, -1, 0}, {-1, 5, -1}, {0, -1, 0}};
  final double sharpnessMatrixDivider = 1.0;
  //applyConvolution(pixel, x, y, unsharpnessMaskingConvMatrix, unsharpnessMaskingMatrixDivider,  1.0);
  //applyConvolution(pixel, x, y, defaultConvMatrix, defaultConvMatrixDivider,  1.0);
  //    applyConvolution(pixel, x, y, gaussianConvMatrix, gaussianMatrixDivider,  1.0);
  public static double[][] getMatrix() {
    final double[][] unsharpnessMaskingConvMatrix = {
        {1, 4, 6, 4, 1},
        {4, 16, 24, 16, 4},
        {6, 24, -476, 24, 6},
        {4, 16, 24, 16, 4},
        {1, 4, 6, 4, 1}
    };
    final double unsharpnessMaskingMatrixDivider = -256.0;
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        unsharpnessMaskingConvMatrix[i][j] /= unsharpnessMaskingMatrixDivider;
      }
    }
    return unsharpnessMaskingConvMatrix;
  }
}
