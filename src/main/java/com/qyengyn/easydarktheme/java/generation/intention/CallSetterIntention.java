package com.qyengyn.easydarktheme.java.generation.intention;

import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction;
import com.intellij.codeInsight.intention.LowPriorityAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.util.IncorrectOperationException;
import com.qyengyn.easydarktheme.java.generation.GenerateCallSetterHandler;
import org.jetbrains.annotations.NotNull;

public class CallSetterIntention extends BaseElementAtCaretIntentionAction implements LowPriorityAction {

    private final GenerateCallSetterHandler handler = new GenerateCallSetterHandler();

    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        this.handler.invoke(project, editor, element.getContainingFile());
    }

    @Override
    public boolean isAvailable(@NotNull Project project, @NotNull Editor editor, @NotNull PsiElement element) {
        if (!(element instanceof PsiIdentifier identifier)) {
            return false;
        }
        return identifier.getParent() instanceof PsiLocalVariable;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Set values with setters";
    }

    @Override
    public @NotNull String getText() {
        return getFamilyName();
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
