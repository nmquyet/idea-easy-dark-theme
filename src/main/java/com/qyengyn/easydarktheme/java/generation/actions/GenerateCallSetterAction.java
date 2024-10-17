package com.qyengyn.easydarktheme.java.generation.actions;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.project.DumbAware;
import com.qyengyn.easydarktheme.java.generation.GenerateCallSetterHandler;
import org.jetbrains.annotations.NotNull;

public class GenerateCallSetterAction extends BaseCodeInsightAction implements DumbAware {

    @Override
    protected @NotNull CodeInsightActionHandler getHandler() {
        return new GenerateCallSetterHandler();
    }

}