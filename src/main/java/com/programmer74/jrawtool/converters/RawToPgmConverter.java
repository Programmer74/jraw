package com.programmer74.jrawtool.converters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RawToPgmConverter {

  private static final String dcraw_executable = "/opt/dcraw/dcraw";

  private static final boolean debayer_by_dcraw = true;

  public static String convertRawToPgmAndGetFilename(String rawFilename) {
    try {
      final File temp = File.createTempFile("temp-image", ".pgm");

      //for 16-bit raw bayer array:
      //./dcraw -4 -D -v -c DSC_1801.NEF > file
      //for 16-bit colored image, but not gamma corrected
      //./dcraw -4 -v -c DSC_1801.NEF > file2

      final List<String> dcrawCmd = new ArrayList<>();
      dcrawCmd.add(dcraw_executable);
      dcrawCmd.add("-4");

      if (!debayer_by_dcraw) {
        dcrawCmd.add("-D");
      }

      dcrawCmd.add("-v");
      dcrawCmd.add("-c");
      dcrawCmd.add(rawFilename);

      ProcessBuilder builder = new ProcessBuilder(dcrawCmd);
      builder.redirectError(ProcessBuilder.Redirect.INHERIT);
      builder.redirectOutput(ProcessBuilder.Redirect.to(temp));
      Process p = builder.start();
      p.waitFor();

      return temp.getAbsolutePath();
    } catch (Exception ex) {
      return "";
    }
  }

  public static String extractJpegPreviewAndGetFilename(String rawFilename) {
    try {
      final File temp = File.createTempFile("temp-image", ".jpg");

      final List<String> dcrawCmd = new ArrayList<>();
      dcrawCmd.add(dcraw_executable);
      dcrawCmd.add("-e");

      dcrawCmd.add("-c");
      dcrawCmd.add(rawFilename);

      ProcessBuilder builder = new ProcessBuilder(dcrawCmd);
      builder.redirectError(ProcessBuilder.Redirect.INHERIT);
      builder.redirectOutput(ProcessBuilder.Redirect.to(temp));
      Process p = builder.start();
      p.waitFor();

      return temp.getAbsolutePath();
    } catch (Exception ex) {
      return "";
    }
  }
}
