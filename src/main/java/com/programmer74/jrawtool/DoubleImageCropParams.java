package com.programmer74.jrawtool;

import java.util.Objects;

public class DoubleImageCropParams {

  public int lx, ly, rx, ry;

  @Override public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final DoubleImageCropParams that = (DoubleImageCropParams) o;
    return lx == that.lx &&
        ly == that.ly &&
        rx == that.rx &&
        ry == that.ry;
  }

  public boolean shouldRepaintImage(final DoubleImageCropParams oldCrop) {
    return !(
        ((oldCrop.lx == lx) && (oldCrop.rx == rx))
        || ((oldCrop.ly == ly) && (oldCrop.ry == ry))
    );
  }

  @Override public int hashCode() {

    return Objects.hash(lx, ly, rx, ry);
  }

  public DoubleImageCropParams(final int lx, final int ly, final int rx, final int ry) {
    this.lx = lx;
    this.ly = ly;
    this.rx = rx;
    this.ry = ry;
  }

  public DoubleImageCropParams(final Double lx, final Double ly, final Double rx, final Double ry) {
    this.lx = lx.intValue();
    this.ly = ly.intValue();
    this.rx = rx.intValue();
    this.ry = ry.intValue();
  }
}
