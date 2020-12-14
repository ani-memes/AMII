// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package io.unthrottled.amii.config.ui;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.impl.config.IntentionActionMetaData;
import com.intellij.codeInsight.intention.impl.config.IntentionDescriptionPanel;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.options.MasterDetails;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DetailsComponent;
import com.intellij.psi.PsiFile;
import com.intellij.ui.GuiUtils;
import com.intellij.util.Alarm;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.util.Collections;
import java.util.List;

public final class PreferredCharacterPanel implements MasterDetails {
  private final PreferredCharacterTree myPreferredCharacterTree;
  private final IntentionDescriptionPanel myIntentionDescriptionPanel = new IntentionDescriptionPanel();
  private final Alarm myResetAlarm = new Alarm();
  private JPanel myPanel;
  private JPanel myTreePanel;
  private DetailsComponent myDetailsComponent;

  public PreferredCharacterPanel() {

    myPreferredCharacterTree = new PreferredCharacterTree() {
      @Override
      protected void selectionChanged(Object selected) {
        if (selected instanceof IntentionActionMetaData) {
          final IntentionActionMetaData actionMetaData = (IntentionActionMetaData) selected;
          final Runnable runnable = () -> {
            intentionSelected(actionMetaData);
            if (myDetailsComponent != null) {
              String[] text = ArrayUtil.append(actionMetaData.myCategory, actionMetaData.getFamily());
              myDetailsComponent.setText(text);
            }
          };
          myResetAlarm.cancelAllRequests();
          myResetAlarm.addRequest(runnable, 100);
        } else {
          categorySelected((String) selected);
          if (myDetailsComponent != null) {
            myDetailsComponent.setText((String) selected);
          }
        }
      }
    };
    myTreePanel.setLayout(new BorderLayout());
    myTreePanel.add(myPreferredCharacterTree.getComponent(), BorderLayout.CENTER);

    GuiUtils.replaceJSplitPaneWithIDEASplitter(myPanel);

  }

  private void intentionSelected(IntentionActionMetaData actionMetaData) {
    myIntentionDescriptionPanel.reset(actionMetaData, myPreferredCharacterTree.getFilter());
  }

  private void categorySelected(String intentionCategory) {
    myIntentionDescriptionPanel.reset(intentionCategory);
  }

  public void reset() {
    myPreferredCharacterTree.reset();
    SwingUtilities.invokeLater(() -> myIntentionDescriptionPanel.init(myPanel.getWidth() / 2));
  }

  @Override
  public void initUi() {
    myDetailsComponent = new DetailsComponent();
  }

  @Override
  public JComponent getToolbar() {
    return myPreferredCharacterTree.getToolbarPanel();
  }

  @Override
  public JComponent getMaster() {
    return myTreePanel;
  }

  @Override
  public DetailsComponent getDetails() {
    return myDetailsComponent;
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
    myIntentionDescriptionPanel.dispose();
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
