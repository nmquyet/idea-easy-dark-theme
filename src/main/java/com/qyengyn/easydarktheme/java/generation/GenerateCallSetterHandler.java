package com.qyengyn.easydarktheme.java.generation;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.qyengyn.easydarktheme.java.JavaPsiUtils;
import org.jetbrains.annotations.NotNull;


public class GenerateCallSetterHandler implements CodeInsightActionHandler {

    public GenerateCallSetterHandler() {
    }

    @Override
    public final void invoke(final @NotNull Project project, final @NotNull Editor editor, @NotNull PsiFile file) {
        if (!EditorModificationUtil.checkModificationAllowed(editor)) return;
        if (!FileDocumentManager.getInstance().requestWriting(editor.getDocument(), project)) {
            return;
        }

        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        if (!(element instanceof PsiIdentifier psiIdentifier)) {
            showHint(editor);
            return;
        }
        if (!(psiIdentifier.getParent() instanceof PsiLocalVariable psiLocalVariable)) {
            showHint(editor);
            return;
        }

        PsiClass aClass = resolvePsiClass(psiLocalVariable);
        if (aClass == null) {
            showHint(editor, "Cannot detect type of variable");
            return;
        }

        final PsiMethodMember[] members = ReadAction.compute(
                () -> JavaPsiUtils.findAllPublicSetters(aClass));

        var dialog = new MemberChooser<>(members, false, true, project, null, null);
        dialog.show();
        var selectedMembers = dialog.getSelectedElements()
            .toArray(new PsiMethodMember[0]);

        WriteAction.run(() -> generateCode(project, selectedMembers, psiLocalVariable));
    }

    private void generateCode(Project project, PsiMethodMember[] members, PsiLocalVariable element) {
        PsiStatement anchor = PsiTreeUtil.getParentOfType(element, PsiStatement.class);
        if (anchor == null)
            return;

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        for (PsiMethodMember methodMember : members) {
            PsiMethod method = methodMember.getElement();
            PsiStatement statement = factory.createStatementFromText(
                    element.getName() + "." + method.getName() + "();",
                    null);
            anchor.getParent().addAfter(statement, anchor);
        }
    }

    protected PsiClass resolvePsiClass(PsiLocalVariable localVariable) {
        // Attempt to resolve the inferred type from the initializer
        PsiExpression initializer = localVariable.getInitializer();
        if (initializer != null) {
            PsiType inferredType = initializer.getType();
            if (inferredType instanceof PsiClassType classType) {
                return classType.resolve();
            }
        }

        // If no initializer or can't resolve the type, use PsiUtil to infer it
        PsiType type = PsiUtil.getTypeByPsiElement(localVariable);
        if (type instanceof PsiClassType) {
            return ((PsiClassType) type).resolve();
        }

        // If type inference fails, return null
        return null;
    }

    private void showHint(Editor editor) {
        showHint(editor,
                 "Place caret at an variable with initializer\n"
                    + "var variable[caret] = new SomeClass();");
    }

    private void showHint(Editor editor, String message) {
        HintManager.getInstance().showInformationHint(editor, message);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
