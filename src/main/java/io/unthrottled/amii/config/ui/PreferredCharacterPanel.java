// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package io.unthrottled.amii.config.ui;

import com.intellij.ui.GuiUtils;

import javax.swing.JPanel;
import javax.swing.JTree;
import java.awt.BorderLayout;

public final class PreferredCharacterPanel {
  private final PreferredCharacterTree myPreferredCharacterTree = new PreferredCharacterTree();
  private JPanel myPanel;
  private JPanel myTreePanel;

  public PreferredCharacterPanel() {
    myTreePanel.setLayout(new BorderLayout());
    myTreePanel.add(myPreferredCharacterTree.getComponent(), BorderLayout.CENTER);

    GuiUtils.replaceJSplitPaneWithIDEASplitter(myPanel);
  }

  public void reset() {
    myPreferredCharacterTree.reset();
  }

  public void apply() {
    myPreferredCharacterTree.apply();
  }

  public JPanel getComponent() {
    return myPanel;
  }

  public JTree getIntentionTree() {
    return myPreferredCharacterTree.getTree();
  }

  public boolean isModified() {
    return myPreferredCharacterTree.isModified();
  }

  public void dispose() {
    myPreferredCharacterTree.dispose();
  }

  public void selectIntention(String familyName) {
    myPreferredCharacterTree.selectIntention(familyName);
  }

  public Runnable showOption(final String option) {
    return () -> {
      myPreferredCharacterTree.filter(myPreferredCharacterTree.filterModel(option, true));
      myPreferredCharacterTree.setFilter(option);
    };
  }
}
