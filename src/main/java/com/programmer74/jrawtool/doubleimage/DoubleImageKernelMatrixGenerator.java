package com.programmer74.jrawtool.doubleimage;

public class DoubleImageKernelMatrixGenerator {

  //unsharpness masking:
  //1) calculate gaussian matrix
  //2) divider = -divider
  //3) mid_value = (divider * 2) + mid_value


  private static double[][] buildMatrix(int size) {
    return new double[size][size];
  }

  private static double gaussFunction(int x, int y, double sigma) {
    return 1 / (2.f * Math.PI * sigma * sigma) * Math.exp((x * x + y * y) / (-2.f * sigma * sigma));
  }

  private static double[][] buildSharpeningMatrix(int matrixSize) {
    double[][] tmp = new double[][]{
        {0, -1, 0},
        {-1, 5, -1},
        {0, -1, 0}
    };
    double[][] ansMatrix = buildMatrix(matrixSize);
    int offset = (matrixSize - 3) / 2;
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        ansMatrix[i + offset][j + offset] = tmp[i][j];
      }
    }
    return ansMatrix;
  }

  private static double[][] buildGaussianMatrix(double radius, int matrixSize) {
    double[][] matrix = buildMatrix(matrixSize);
    double sum = 0;
    int offset = matrixSize / 2;
    for (int x = -offset; x <= offset; x++) {
      for (int y = -offset; y <= offset; y++) {
        matrix[x + offset][y + offset] = gaussFunction(x, y, radius);
        sum += matrix[x + offset][y + offset];
      }
    }
    for (int i = 0; i < matrixSize; i++) {
      for (int j = 0; j < matrixSize; j++) {
        matrix[i][j] /= sum;
      }
    }
    return matrix;
  }

  private static double[][] buildUnsharpMaskingMatrix(double radius, int matrixSize) {
    double[][] matrix = buildMatrix(matrixSize);
    double sum = 0;
    int offset = matrixSize / 2;
    for (int x = -offset; x <= offset; x++) {
      for (int y = -offset; y <= offset; y++) {
        matrix[x + offset][y + offset] = -gaussFunction(x, y, radius);
        sum += matrix[x + offset][y + offset];
      }
    }
    for (int i = 0; i < matrixSize; i++) {
      for (int j = 0; j < matrixSize; j++) {
        matrix[i][j] /= -sum;
      }
    }
    matrix[offset][offset] = 1 - 2 * sum;
    return matrix;
  }

  public static double[][] buildConvolutionMatrix(
      double plainSharpeningStrength,
      double gaussianBlurStrength,
      int gaussianBlurRadius,
      double unsharpMaskingStrength,
      int unsharpMaskingRadius
  ) {

    int totalRadius = Math.max(1, Math.max(gaussianBlurRadius, unsharpMaskingRadius));
    int matrixSize = totalRadius * 2 + 1;

    double[][] matrix = buildMatrix(matrixSize);
    matrix[totalRadius][totalRadius] = 1;

    double[][] plainSharpeningMatrix = buildSharpeningMatrix(matrixSize);
    double[][] gaussianBlurMatrix = buildGaussianMatrix(gaussianBlurRadius, matrixSize);
    double[][] unsharpMaskingMatrix = buildUnsharpMaskingMatrix(unsharpMaskingRadius, matrixSize);

    double sum = 0;
    for (int i = 0; i < matrixSize; i++) {
      for (int j = 0; j < matrixSize; j++) {
        matrix[i][j] *= (1 - plainSharpeningStrength - gaussianBlurStrength - unsharpMaskingStrength);
        matrix[i][j] += plainSharpeningStrength * plainSharpeningMatrix[i][j];
        matrix[i][j] += gaussianBlurStrength * gaussianBlurMatrix[i][j];
        matrix[i][j] += unsharpMaskingStrength * unsharpMaskingMatrix[i][j];

        sum += matrix[i][j];
      }
    }

    System.out.println(sum);
    for (int i = 0; i < matrixSize; i++) {
      for (int j = 0; j < matrixSize; j++) {
        System.out.print(matrix[i][j] + " ");
      }
      System.out.println();
    }

    return matrix;
  }
}
