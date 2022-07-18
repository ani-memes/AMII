package io.unthrottled.amii.config.ui;

import com.intellij.ui.components.JBLabel;
import io.unthrottled.amii.assets.MemeAsset;
import io.unthrottled.amii.assets.VisualMemeContent;
import org.intellij.lang.annotations.Language;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;

public class CustomMemePanel {
  private JPanel memeSettings;
  private JComboBox memeAssetCategoryComboBox;
  private JButton chooseAudibleAsset;
  private JButton testMeme;
  private JPanel visualAssetDisplay;
  private JLabel assetDescription;
  private JPanel rootPane;
  private JBLabel memeDisplay;

  public CustomMemePanel(Consumer<MemeAsset> onTest) {
    @Language("HTML") String meme = "<html><img src=\"file:///Users/alexsimons/Downloads/zero-two-and-hiro-anime.gif\" /></html>";
    memeDisplay.setText(meme);

    testMeme.addActionListener(a ->
      onTest.accept(
        new MemeAsset(
          new VisualMemeContent(
            "aoeu",
            URI.create("file:///Users/alexsimons/Downloads/zero-two-and-hiro-anime.gif"),
            "",
            null
          ),
          null
        )
      ));
  }

  public Optional<MemeAsset> getMemeAsset() {
    return Optional.empty();
  }

  public JPanel getComponent() {
    return rootPane;
  }
}
