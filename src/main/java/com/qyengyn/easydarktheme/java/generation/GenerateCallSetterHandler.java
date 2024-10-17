package com.qyengyn.easydarktheme.java.generation;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class GenerateCallSetterHandler implements CodeInsightActionHandler {

    public GenerateCallSetterHandler() {
    }

    @Override
    public final void invoke(final @NotNull Project project, final @NotNull Editor editor, @NotNull PsiFile file) {
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

        final PsiMethodMember[] members = findAllPublicSetters(aClass);
        if (members == null) {
            showHint(editor, "No public setters found");
            return;
        }

        generateCode(project, members, psiLocalVariable);
    }

    @Override
    public String toString() {
        return "GenerateSetObjectPropsHandler{}";
    }

    private void generateCode(final Project project, PsiMethodMember[] members, PsiLocalVariable element) {
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

    protected PsiMethodMember @Nullable [] findAllPublicSetters(final PsiClass aClass) {
        PsiMethod[] allMethods = aClass.getAllMethods();
        List<PsiMethodMember> result = new ArrayList<>();
        for (PsiMethod method : allMethods) {
            if (!method.getName().startsWith("set")) continue;

            PsiParameterList parameterList = method.getParameterList();
            if (parameterList.getParametersCount() != 1) continue;

            PsiModifierList modifierList = method.getModifierList();
            if (!modifierList.hasModifierProperty(PsiModifier.PUBLIC)) continue;
            if (modifierList.hasModifierProperty(PsiModifier.STATIC)) continue;

            result.add(new PsiMethodMember(method));
        }
        return result.toArray(new PsiMethodMember[0]);
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

}
