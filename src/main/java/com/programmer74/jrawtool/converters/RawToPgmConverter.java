package com.programmer74.jrawtool.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RawToPgmConverter {

  private static final String dcraw_executable = "/opt/dcraw/dcraw";

  private static final boolean debayer_by_dcraw = true;

  public static InputStream openDCRawAsConverterToPGM(String rawFilename) {
    try {
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
      Process p = builder.start();

      return p.getInputStream();
    } catch (Exception ex) {
      return null;
    }
  }

  public static InputStream openDCRawAsJpegPreviewExtractor(String rawFilename) {
    try {
      final List<String> dcrawCmd = new ArrayList<>();
      dcrawCmd.add(dcraw_executable);
      dcrawCmd.add("-e");

      dcrawCmd.add("-c");
      dcrawCmd.add(rawFilename);

      ProcessBuilder builder = new ProcessBuilder(dcrawCmd);
      builder.redirectError(ProcessBuilder.Redirect.INHERIT);
      Process p = builder.start();
      return p.getInputStream();
    } catch (Exception ex) {
      return null;
    }
  }

  public static String extractRawInformationFromFile(String rawFilename) {
    try {

      final List<String> dcrawCmd = new ArrayList<>();
      dcrawCmd.add(dcraw_executable);
      dcrawCmd.add("-v");
      dcrawCmd.add("-i");
      dcrawCmd.add(rawFilename);

      ProcessBuilder builder = new ProcessBuilder(dcrawCmd);
      builder.redirectError(ProcessBuilder.Redirect.INHERIT);
      Process p = builder.start();

      BufferedReader reader =
          new BufferedReader(new InputStreamReader(p.getInputStream()));
      StringBuilder inputs = new StringBuilder();
      String line = null;
      while ( (line = reader.readLine()) != null) {
        inputs.append(line);
        inputs.append(System.getProperty("line.separator"));
      }
      String result = inputs.toString();

      p.waitFor();

      return result;
    } catch (Exception ex) {
      return "";
    }
  }
}
