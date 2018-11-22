package com.programmer74.jrawtool.forms;

import javax.swing.*;
import java.awt.*;

public class InfoDialog extends JDialog {

  private JPanel textPanel;
  private JTextArea textLabel;

  public InfoDialog(final JFrame parentFrame) {
    super(parentFrame, "title", true);
    setModalityType(Dialog.ModalityType.MODELESS);
    setLocationRelativeTo(null);

    textPanel = new JPanel();
    textPanel.setPreferredSize(new Dimension(400, 300));
    textPanel.setMinimumSize(new Dimension(400, 300));

    textLabel = new JTextArea("");
    textLabel.setWrapStyleWord(true);
    textLabel.setLineWrap(true);
    textLabel.setPreferredSize(new Dimension(300, 300));
    textLabel.setMinimumSize(new Dimension(300, 300));
    textLabel.setEditable(false);
    textLabel.setCursor(null);
    textLabel.setOpaque(false);
    textLabel.setFocusable(false);
    textLabel.setFont(UIManager.getFont("Label.font"));

    textPanel.add(textLabel);
    add(textPanel);
    pack();
  }

  public void showDialog(final String title) {
    setTitle(title);
    textLabel.setText("");
    setVisible(true);
  }

  public void appendText(final String text) {
    textLabel.setText(textLabel.getText() + "\n > " + text);
    this.revalidate();
    this.repaint();
  }

  public void hideDialog() {
    this.dispose();
  }
}
