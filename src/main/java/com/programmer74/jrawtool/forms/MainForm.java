package com.programmer74.jrawtool.forms;

import com.programmer74.jrawtool.Application;
import com.programmer74.jrawtool.components.HistogramComponent;
import com.programmer74.jrawtool.components.ImagePreviewAccessoire;
import com.programmer74.jrawtool.doubleimage.DoubleImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class MainForm extends JFrame {

  private Application parentApp;
  private DoubleImage doubleImage;
  private JDesktopPane mdiPane;

  public MainForm(final DoubleImage doubleImage, final Application application) {
    super("JRaw");

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    this.doubleImage = doubleImage;
    this.parentApp = application;
    this.mdiPane = new JDesktopPane();

    add(mdiPane);
    setupMenu();

    setSize(new Dimension(1024, 768));
  }

  public JDesktopPane getMdiPane() {
    return mdiPane;
  }

  private void setupMenu() {
    JMenuBar menubar = new JMenuBar();

    JMenu file = new JMenu("File");
    file.setMnemonic(KeyEvent.VK_F);

    JMenuItem eOpenItem = new JMenuItem("Open");
    eOpenItem.setMnemonic(KeyEvent.VK_O);
    eOpenItem.setToolTipText("Open image");
    eOpenItem.addActionListener((ActionEvent event) -> {
      handleOpen();
    });

    JMenuItem eOpenViaBrowserItem = new JMenuItem("Open via browser");
    eOpenViaBrowserItem.setMnemonic(KeyEvent.VK_B);
    eOpenViaBrowserItem.setToolTipText("Open image via picture browser");
    eOpenViaBrowserItem.addActionListener((ActionEvent event) -> {
      handleOpenViaPictureBrowser();
    });

    JMenuItem eSaveItem = new JMenuItem("Save");
    eSaveItem.setMnemonic(KeyEvent.VK_S);
    eSaveItem.setToolTipText("Open image");
    eSaveItem.addActionListener((ActionEvent event) -> {
      handleSave();
    });

    JMenuItem eExitItem = new JMenuItem("Exit");
    eExitItem.setMnemonic(KeyEvent.VK_Q);
    eExitItem.setToolTipText("Exit application");
    eExitItem.addActionListener((ActionEvent event) -> {
      System.exit(0);
    });

    file.add(eOpenItem);
    file.add(eOpenViaBrowserItem);
    file.add(eSaveItem);
    file.add(eExitItem);

    JMenu edit = new JMenu("Edit");

    file.setMnemonic(KeyEvent.VK_E);
    JMenuItem eCropItem = new JMenuItem("Crop/Rotate");
    eCropItem.setToolTipText("Crop or rotate image");
    eCropItem.addActionListener((ActionEvent event) -> {
      handleCrop();
    });
    edit.add(eCropItem);

    menubar.add(file);
    menubar.add(edit);

    setJMenuBar(menubar);
  }

  public void showForm() {
    setVisible(true);
  }

  private void handleOpen() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
    FileFilter imageFilter = new FileNameExtensionFilter(
        "Image files", "pgm", "jpg", "bmp", "png", "nef", "cr2");
    fileChooser.setFileFilter(imageFilter);
    fileChooser.setAccessory(new ImagePreviewAccessoire(fileChooser));
    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fileChooser.getSelectedFile();
      parentApp.loadApplication(selectedFile.getAbsolutePath());
    }
  }

  private void handleOpenViaPictureBrowser() {
    parentApp.openImageBrowser();
  }

  private void handleSave() {
    if (doubleImage != null) {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
      FileFilter imageFilter = new FileNameExtensionFilter(
          "Image files", "jpg");
      fileChooser.setFileFilter(imageFilter);
      int result = fileChooser.showSaveDialog(this);
      if (result == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();

        try {
          ImageIO.write(doubleImage.getBufferedImage(), "jpg", selectedFile);
          System.out.println("Save OK");
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  private void handleCrop() {
    parentApp.openCropForm();
  }

  public void setDoubleImage(final DoubleImage doubleImage) {
    this.doubleImage = doubleImage;
  }
}
