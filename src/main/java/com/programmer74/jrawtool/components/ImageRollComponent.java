package com.programmer74.jrawtool.components;

import com.programmer74.jrawtool.converters.GenericConverter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ImageRollComponent extends JPanel {
  private List<File> filenames = new ArrayList<>();

  //TODO: guava cache?
  private Map<String, Image> imagesCache = new HashMap<>();
  private Image emptyImage = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);

  //private JPanel paintBar = new JPanel();
  private JScrollBar scrollBar = new JScrollBar(JScrollBar.HORIZONTAL);

  private static final int scrollBarMultiplier = 10;
  private int imageRollEntryWidth = 120;
  private int imageRollEntryHeight = 80;

  private int mouseX = 0;
  private int mouseY = 0;

  private int hoveredIndex = -1;
  private int selectedIndex = -1;

  private Consumer<File> selectedFileChanged = null;
  private Consumer<File> selectedFileAccepted = null;

  public ImageRollComponent() {
    scrollBar.setMinimum(0);
    scrollBar.setMaximum(100);

    setLayout(new BorderLayout());
    scrollBar.addAdjustmentListener((e) -> forceRepaint());

    //add(paintBar, BorderLayout.CENTER);
    add(scrollBar, BorderLayout.SOUTH);

    addMouseMotionListener(new MouseMotionAdapter() {
      @Override public void mouseMoved(final MouseEvent mouseEvent) {
        mouseX = mouseEvent.getX();
        mouseY = mouseEvent.getY();
        repaint();
      }
    });

    this.selectedFileChanged = (e) -> { /*nothing*/};
    this.selectedFileAccepted = (e) -> { /*nothing*/};

    addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(final MouseEvent e) {
        //this is doubleclick handler
        selectedIndex = hoveredIndex;
        if ((selectedIndex != -1) && (selectedIndex < filenames.size())) {
          if (e.getClickCount() == 2 && !e.isConsumed()) {
            stopCacheFillerThread();
            selectedFileAccepted.accept(filenames.get(selectedIndex));
          } else {
            selectedFileChanged.accept(filenames.get(selectedIndex));
          }
        }
        e.consume();
      }
    });
    addMouseWheelListener(new MouseWheelListener() {
      @Override public void mouseWheelMoved(final MouseWheelEvent mouseWheelEvent) {
        int val = mouseWheelEvent.getScrollAmount() * mouseWheelEvent.getWheelRotation();
        val += scrollBar.getValue();
        val = Math.max(0, Math.min(scrollBar.getMaximum(), val));
        scrollBar.setValue(val);
        forceRepaint();
      }
    });
    setupCacheFillerThread();
  }

  private Thread cacheFillerThread = null;
  private AtomicBoolean cacheFillerThreadStarted = new AtomicBoolean(false);
  private AtomicBoolean cacheFillerThreadShouldStop = new AtomicBoolean(false);

  private void setupCacheFillerThread() {
    cacheFillerThread = new Thread(() -> {
      cacheFillerThreadStarted.set(true);
      List<File> filenamesCopy = new ArrayList<>(filenames);
      for (File file : filenamesCopy) {
        Image img = GenericConverter.loadSmallPreview(file.getAbsolutePath(), imageRollEntryWidth, imageRollEntryHeight);
        imagesCache.put(file.getPath(), img);
//        System.out.println("Added image " + file);
        paintImageRoll();
        if (filenamesCopy.size() != filenames.size()) {
          return;
        }
        if (cacheFillerThreadShouldStop.get()) {
          return;
        }
      }
    });
  }

  private void startCacheFillerThread() {
    if (!cacheFillerThreadStarted.get()) {
      cacheFillerThreadShouldStop.set(false);
      setupCacheFillerThread();
      cacheFillerThread.start();
    }
  }

  private void stopCacheFillerThread() {
    if (cacheFillerThreadStarted.get()) {
      try {
        cacheFillerThreadShouldStop.set(true);
        cacheFillerThread.join();
        cacheFillerThreadStarted.set(false);
      } catch (Exception ex) {

      }
    }
  }

  public void forceRepaint() {
    paintImageRoll();
  }

  public int getSelectedIndex() {
    return selectedIndex;
  }

  public File getSelectedFile() {
    if ((selectedIndex != -1) && (selectedIndex < filenames.size())) {
      return filenames.get(selectedIndex);
    }
    return null;
  }

  public void setSelectedFileChanged(final Consumer<File> selectedFileChanged) {
    this.selectedFileChanged = selectedFileChanged;
  }

  public void setSelectedFileAccepted(final Consumer<File> selectedFileAccepted) {
    this.selectedFileAccepted = selectedFileAccepted;
  }

  public void setFiles(List<File> list) {
    stopCacheFillerThread();
    for (Image i: imagesCache.values()) {
      i.flush();
    }
    imagesCache.clear();
    filenames.clear();
    filenames.addAll(list);
    filenames.sort(new Comparator<File>() {
      @Override public int compare(final File file, final File t1) {
        return file.getName().compareTo(t1.getName());
      }
    });
    startCacheFillerThread();
    updateScrollbar();
    forceRepaint();
  }

  private void updateScrollbar() {
    scrollBar.setMinimum(0);
//    scrollBar.setMaximum(filenames.size() * scrollBarMultiplier);
    scrollBar.setMaximum((filenames.size() - this.getWidth() / imageRollEntryWidth)
        * scrollBarMultiplier);
    scrollBar.setValue(0);
  }

  private void paintSingleImage(final Image image, final Graphics2D g,
      final int x, final int y, final int maxWidth, final int maxHeight) {

    int imageWidth = image.getWidth(null);
    int imageHeight = image.getHeight(null);

    double wK = imageWidth * 1.0 / maxWidth;
    double hK = imageHeight * 1.0 / maxHeight;

    double scale = 1 / Math.max(wK, hK);

    int paintW = (int)(imageWidth * 1.0 * scale);
    int paintH = (int)(imageHeight * 1.0 * scale);

    int offsetX = (maxWidth - paintW) / 2;
    int offsetY = (maxHeight - paintH) / 2;

    g.drawImage(image, x + offsetX, y + offsetY, paintW, paintH, null);
  }

  @Override public void paint(final Graphics g) {
    super.paint(g);
    paintImageRoll();
  }

  private void paintImageRoll() {

    Graphics2D g = (Graphics2D)getGraphics();

//    RepaintManager rm = RepaintManager.currentManager(this);
//    boolean b = rm.isDoubleBufferingEnabled();
//    rm.setDoubleBufferingEnabled(false);

    imageRollEntryHeight = this.getHeight() - scrollBar.getHeight() - 10;
    imageRollEntryWidth = imageRollEntryHeight / 2 * 3;

    if (filenames.size() == 0) {
      hoveredIndex = -1;
      selectedIndex = -1;
      g.setColor(Color.BLACK);
      g.drawString("No image files found", 24, 24);
      return;
    }

    int offset = -scrollBar.getValue() * imageRollEntryWidth / scrollBarMultiplier;

    int fromIndex = -offset / imageRollEntryWidth;
    if (fromIndex < 0) {
      fromIndex = 0;
    }

    int toIndex = fromIndex + this.getWidth() / imageRollEntryWidth + 1;
    if (toIndex > filenames.size() - 1) {
      toIndex = filenames.size() - 1;
    }

    offset += fromIndex * imageRollEntryWidth;

//    fillImageCache(fromIndex, toIndex);

//    System.out.println("offset = " + (-offset));
//    System.out.println("fromIndex = " + fromIndex);
//    System.out.println("mouseX = " + mouseX);
    hoveredIndex = fromIndex + (mouseX - offset) / imageRollEntryWidth;

    for (int index = fromIndex; index <= toIndex; index++) {

      final File file = filenames.get(index);
      Image filePreview = imagesCache.getOrDefault(file.getPath(), emptyImage);
//      Image filePreview = emptyImage;

      paintSingleImage(filePreview, g, offset, 0, imageRollEntryWidth,
          imageRollEntryHeight);

      g.setColor(Color.BLACK);
      g.drawString(file.getName(), offset, 15);
      g.setColor(Color.WHITE);
      g.drawString(file.getName(), offset + 1, 16);

      if (index == hoveredIndex) {
        g.setColor(Color.RED);
        g.drawRect(offset + 1, 1, imageRollEntryWidth - 1, imageRollEntryHeight - 1);
      }

      offset += imageRollEntryWidth;
    }

//    rm.setDoubleBufferingEnabled(b);
  }
}
