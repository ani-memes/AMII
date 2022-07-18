package io.unthrottled.amii.config.ui;

import com.intellij.uiDesigner.core.GridConstraints;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class CustomMemeList {
  private JButton button1;
  private JPanel rootPane;
  private JScrollPane memesScroller;
  private JPanel ayyLmao;

  public CustomMemeList() {
    CustomMemePanel customMemePanel = new CustomMemePanel();
    ayyLmao.add(customMemePanel.getComponent(), new GridConstraints());
  }

  public JPanel getComponent() {
    return rootPane;
  }

  private void createUIComponents() {
    // TODO: place custom component creation code here
  }
}
