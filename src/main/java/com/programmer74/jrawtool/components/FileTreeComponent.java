package com.programmer74.jrawtool.components;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class FileTreeComponent extends JPanel {

  private List<String> extensionsList;
  private File selectedFile = null;
  private Consumer<File> selectedFileChanged = null;
  private boolean displayDirectoriesOnly = false;

  public FileTreeComponent(final File fileRoot, final List<String> extensionsList,
      final boolean displayDirectoriesOnly) {
    DefaultMutableTreeNode nodeRoot = new DefaultMutableTreeNode(new FileTreeNode(fileRoot));
    DefaultTreeModel treeModel = new DefaultTreeModel(nodeRoot);
    this.extensionsList = extensionsList;
    this.selectedFileChanged = ((e) -> {/*nothing*/});
    this.displayDirectoriesOnly = displayDirectoriesOnly;

    int w = 300;
    int h = 600;

    JTree tree = new JTree(treeModel);
    tree.setShowsRootHandles(true);
    JScrollPane scrollPane = new JScrollPane(tree);
    scrollPane.setMinimumSize(new Dimension(w, h));

    fillTree(fileRoot, nodeRoot, 2);
    ((FileTreeNode)nodeRoot.getUserObject()).childrensLoaded();

    tree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
            tree.getLastSelectedPathComponent();
        if (node == null) {
          selectedFile = null;
          return;
        }
        Object nodeInfo = node.getUserObject();
        FileTreeNode clickedNode = (FileTreeNode)nodeInfo;
        if (clickedNode.childrensNotLoaded()) {
          fillTree(clickedNode.getFile(), node, 2);
          clickedNode.childrensLoaded();
        }
        selectedFileChanged.accept(clickedNode.getFile());
      }
    });

    setLayout(new BoxLayout(this, BoxLayout. PAGE_AXIS));
    add(scrollPane);
    setPreferredSize(new Dimension(w, h));
  }

  public void setSelectedFileChanged(final Consumer<File> selectedFileChanged) {
    this.selectedFileChanged = selectedFileChanged;
  }

  public File getSelectedFile() {
    return selectedFile;
  }

  private Comparator<File> fileComparator = new Comparator<File>() {
    @Override public int compare(final File file, final File t1) {
      return file.getName().compareTo(t1.getName());
    }
  };

  private void fillTree(final File fileRoot,
      final DefaultMutableTreeNode node,
      final int depth) {

    if (depth < 0) {
      return;
    }
    node.removeAllChildren();

    File[] allFiles = fileRoot.listFiles();
    if (allFiles == null) {
      return;
    }

    java.util.List<File> directories = new ArrayList<>();
    List<File> files = new ArrayList<>();

    for (File file : allFiles) {
      if (file.isDirectory()) {
        directories.add(file);
      } else {
        if (!displayDirectoriesOnly && extensionsList.contains(getFileExtension(file))) {
          files.add(file);
        }
      }
    }
    directories.sort(fileComparator);
    files.sort(fileComparator);

    for (File dir : directories) {
      DefaultMutableTreeNode childNode =
          new DefaultMutableTreeNode(new FileTreeNode(dir));
      node.add(childNode);
      fillTree(dir, childNode, depth - 1);
    }

    if (!displayDirectoriesOnly) {
      for (File file : files) {
        DefaultMutableTreeNode childNode =
            new DefaultMutableTreeNode(new FileTreeNode(file));
        node.add(childNode);
      }
    }
  }

  private String getFileExtension(File file) {
    int i = file.getName().lastIndexOf('.');
    if (i > 0) {
      String extension = file.getName().substring(i + 1).toLowerCase();
      return extension;
    }
    return file.getName();
  }
}


class FileTreeNode {

  private File file;
  private boolean childrenLoaded = false;

  public FileTreeNode(File file) {
    this.file = file;
  }

  public File getFile() {
    return file;
  }

  public boolean childrensNotLoaded() {
    return !childrenLoaded;
  }

  public void childrensLoaded() {
    childrenLoaded = true;
  }

  @Override
  public String toString() {
    String name = file.getName();
    if (name.equals("")) {
      return file.getAbsolutePath();
    } else {
      return name;
    }
  }
}
