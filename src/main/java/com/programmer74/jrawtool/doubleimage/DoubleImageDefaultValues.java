package com.programmer74.jrawtool.doubleimage;

public class DoubleImageDefaultValues {
  private double rK = 1.0;
  private double gK = 1.0;
  private double bK = 1.0;
  private double brigthness = 0.0;
  private double contrast = 1.0;
  private double gamma = 1.0;
  private double exposure = 0.0;
  private boolean shouldAutoAdjust = false;

  public boolean shouldAutoAdjust() {
    return shouldAutoAdjust;
  }

  public void setShouldAutoAdjust(final boolean shouldAutoAdjust) {
    this.shouldAutoAdjust = shouldAutoAdjust;
  }

  public double getrK() {
    return rK;
  }

  public void setrK(double rK) {
    this.rK = rK;
  }

  public double getgK() {
    return gK;
  }

  public void setgK(double gK) {
    this.gK = gK;
  }

  public double getbK() {
    return bK;
  }

  public void setbK(double bK) {
    this.bK = bK;
  }

  public double getBrigthness() {
    return brigthness;
  }

  public void setBrigthness(double brigthness) {
    this.brigthness = brigthness;
  }

  public double getContrast() {
    return contrast;
  }

  public void setContrast(double contrast) {
    this.contrast = contrast;
  }

  public double getGamma() {
    return gamma;
  }

  public void setGamma(double gamma) {
    this.gamma = gamma;
  }

  public double getExposure() {
    return exposure;
  }

  public void setExposure(double exposure) {
    this.exposure = exposure;
  }
}
