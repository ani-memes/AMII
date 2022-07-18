package io.unthrottled.amii.config.ui;

import com.intellij.ui.components.JBLabel;
import org.intellij.lang.annotations.Language;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CustomMemePanel {
  private JPanel memeSettings;
  private JComboBox memeAssetCategoryComboBox;
  private JButton chooseAudibleAsset;
  private JButton testMeme;
  private JPanel visualAssetDisplay;
  private JLabel assetDescription;
  private JPanel rootPane;
  private JBLabel memeDisplay;

  public CustomMemePanel() {
    @Language("HTML") String meme = "<html><img src=\"file:///Users/alexsimons/Downloads/zero-two-and-hiro-anime.gif\" /></html>";
    memeDisplay.setText(meme);
  }

  public JPanel getComponent() {
    return rootPane;
  }
}
