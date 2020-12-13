// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package io.unthrottled.amii.config.ui;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.impl.config.IntentionActionMetaData;
import com.intellij.codeInsight.intention.impl.config.IntentionDescriptionPanel;
import com.intellij.codeInsight.intention.impl.config.IntentionManagerSettings;
import com.intellij.codeInsight.intention.impl.config.TextDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.ide.ui.search.SearchUtil;
import com.intellij.ide.ui.search.SearchableOptionsRegistrar;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.options.MasterDetails;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DetailsComponent;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.ui.GuiUtils;
import com.intellij.util.Alarm;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class IntentionSettingsPanel implements MasterDetails {
  private final IntentionSettingsTree myIntentionSettingsTree;
  private final IntentionDescriptionPanel myIntentionDescriptionPanel = new IntentionDescriptionPanel();
  private final Alarm myResetAlarm = new Alarm();
  private JPanel myPanel;
  private JPanel myTreePanel;
  private DetailsComponent myDetailsComponent;

  public IntentionSettingsPanel() {

    myIntentionSettingsTree = new IntentionSettingsTree() {
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

      @Override
      protected List<IntentionActionMetaData> filterModel(String filter, final boolean force) {
        List<IntentionActionMetaData> list = getMetaData();
        if (filter == null || filter.length() == 0) {
          return list;
        }

        Set<String> quoted = new HashSet<>();
        List<Set<String>> keySetList = SearchUtil.findKeys(filter, quoted);
        List<IntentionActionMetaData> result = new ArrayList<>();
        for (IntentionActionMetaData metaData : list) {
          if (isIntentionAccepted(metaData, filter, force, keySetList, quoted)) {
            result.add(metaData);
          }
        }
        final Set<String> filters = SearchableOptionsRegistrar.getInstance().getProcessedWords(filter);
        if (force && result.isEmpty()) {
          if (filters.size() > 1) {
            result = filterModel(filter, false);
          }
        }
        return result;
      }
    };
    myTreePanel.setLayout(new BorderLayout());
    myTreePanel.add(myIntentionSettingsTree.getComponent(), BorderLayout.CENTER);

    GuiUtils.replaceJSplitPaneWithIDEASplitter(myPanel);

  }

  @NotNull
  private List<IntentionActionMetaData> getMetaData() {
    IntentionAction intentionAction = new IntentionAction() {
      @Override
      public @IntentionName @NotNull String getText() {
        return "Ryuko";
      }

      @Override
      public @NotNull @IntentionFamilyName String getFamilyName() {
        return "KillLaKill";
      }

      @Override
      public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
      }

      @Override
      public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {

      }

      @Override
      public boolean startInWriteAction() {
        return false;
      }
    };
    IntentionActionMetaData intentionActionMetaData = new IntentionActionMetaData(
      intentionAction, this.getClass().getClassLoader(), new String[]{}, "Ayy lmao"
    );
    return Collections.singletonList(intentionActionMetaData);
  }

  private static boolean isIntentionAccepted(IntentionActionMetaData metaData, @NonNls String filter, boolean forceInclude,
                                             List<? extends Set<String>> keySetList, @NotNull Set<String> quoted) {
    if (StringUtil.containsIgnoreCase(metaData.getFamily(), filter)) {
      return true;
    }

    for (String category : metaData.myCategory) {
      if (category != null && StringUtil.containsIgnoreCase(category, filter)) {
        return true;
      }
    }
    for (String stripped : quoted) {
      if (StringUtil.containsIgnoreCase(metaData.getFamily(), stripped)) {
        return true;
      }
      for (String category : metaData.myCategory) {
        if (category != null && StringUtil.containsIgnoreCase(category, stripped)) {
          return true;
        }
      }
      try {
        final TextDescriptor description = metaData.getDescription();
        if (StringUtil.containsIgnoreCase(description.getText(), stripped)) {
          if (!forceInclude) return true;
        } else if (forceInclude) return false;
      } catch (IOException e) {
        //skip then
      }
    }
    for (Set<String> keySet : keySetList) {
      if (keySet.contains(metaData.getFamily())) {
        if (!forceInclude) {
          return true;
        }
      } else {
        if (forceInclude) {
          return false;
        }
      }
    }
    return forceInclude;
  }

  private void intentionSelected(IntentionActionMetaData actionMetaData) {
    myIntentionDescriptionPanel.reset(actionMetaData, myIntentionSettingsTree.getFilter());
  }

  private void categorySelected(String intentionCategory) {
    myIntentionDescriptionPanel.reset(intentionCategory);
  }

  public void reset() {
    myIntentionSettingsTree.reset();
    SwingUtilities.invokeLater(() -> myIntentionDescriptionPanel.init(myPanel.getWidth() / 2));
  }

  @Override
  public void initUi() {
    myDetailsComponent = new DetailsComponent();
  }

  @Override
  public JComponent getToolbar() {
    return myIntentionSettingsTree.getToolbarPanel();
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
    myIntentionSettingsTree.apply();
  }

  public JPanel getComponent() {
    return myPanel;
  }

  public JTree getIntentionTree() {
    return myIntentionSettingsTree.getTree();
  }

  public boolean isModified() {
    return myIntentionSettingsTree.isModified();
  }

  public void dispose() {
    myIntentionSettingsTree.dispose();
    myIntentionDescriptionPanel.dispose();
  }

  public void selectIntention(String familyName) {
    myIntentionSettingsTree.selectIntention(familyName);
  }

  public Runnable showOption(final String option) {
    return () -> {
      myIntentionSettingsTree.filter(myIntentionSettingsTree.filterModel(option, true));
      myIntentionSettingsTree.setFilter(option);
    };
  }
}
