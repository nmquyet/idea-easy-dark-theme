package com.qyengyn.easydarktheme.javascript;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

public class MyJavascriptExtendedAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        annotateFunctionCall(psiElement, annotationHolder);
    }

    private void annotateFunctionCall(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        var type = PsiUtil.getElementType(psiElement);
        if (type == null || !"JS:IDENTIFIER".equals(type.toString())) return;
        var parent = psiElement.getParent();
        if (parent instanceof JSProperty) return;
        if (!(parent instanceof JSReferenceExpression)) return;
        var grand = parent.getParent();
        if (!(grand instanceof JSCallExpression)) return;

        var textAttributes = DefaultLanguageHighlighterColors.FUNCTION_CALL;
        annotationHolder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
            .textAttributes(textAttributes)
            .create();
    }
}
