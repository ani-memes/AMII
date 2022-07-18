package io.unthrottled.amii.config.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import io.unthrottled.amii.assets.MemeAsset;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.util.function.Consumer;

public class CustomMemeList {
  private JButton button1;
  private JPanel rootPane;
  private JScrollPane memesScroller;
  private JPanel ayyLmao;

  public CustomMemeList(
    Consumer<MemeAsset> onTest
  ) {
    CustomMemePanel customMemePanel = new CustomMemePanel(
      onTest
    );
    ayyLmao.add(customMemePanel.getComponent(), new GridConstraints());
  }

  public JPanel getComponent() {
    return rootPane;
  }

  private void createUIComponents() {
    // TODO: place custom component creation code here
  }
}
