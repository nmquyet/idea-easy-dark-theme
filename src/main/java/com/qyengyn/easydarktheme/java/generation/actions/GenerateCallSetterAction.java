package com.qyengyn.easydarktheme.java.generation.actions;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.qyengyn.easydarktheme.java.generation.GenerateCallSetterHandler;
import org.jetbrains.annotations.NotNull;

public class GenerateCallSetterAction extends BaseCodeInsightAction implements DumbAware {

    private final GenerateCallSetterHandler myHandler = new GenerateCallSetterHandler();

    public GenerateCallSetterAction() {
        System.out.println("");
    }

    @Override
    protected @NotNull CodeInsightActionHandler getHandler() {
        return this.myHandler;
    }

    @Override
    protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        if (!EditorModificationUtil.checkModificationAllowed(editor))
            return false;
        if (!file.getFileType().getName().equalsIgnoreCase("java"))
            return false;

        return super.isValidForFile(project, editor, file);
    }
}