package com.qyengyn.easydarktheme;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.impl.source.PsiJavaCodeReferenceElementImpl;
import com.intellij.psi.impl.source.tree.JavaElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class JavaExtendedKeywordAnnotator implements Annotator {
    private static final Set<String> constant_keywords = new HashSet<>(){{
        add("null");
        add("true");
        add("false");
        add("void");
    }};
    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        annotateKeyword(psiElement, annotationHolder);
        annotateImportPackageName(psiElement, annotationHolder);
    }

    private void annotateKeyword(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if (!(psiElement instanceof PsiJavaToken javaToken)) {
            return;
        }

        String value = javaToken.getText();
        if (value == null || !constant_keywords.contains(value)) {
            return;
        }

        annotationHolder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
            .textAttributes(TextAttributesKey.createTextAttributesKey("JAVA_ADDTIONAL_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD))
            .create();
    }

    private void annotateImportPackageName(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if (!(psiElement instanceof PsiJavaCodeReferenceElementImpl codeReferenceElement)) {
            return;
        }

        if (codeReferenceElement.getElementType() != JavaElementType.JAVA_CODE_REFERENCE) {
            return;
        }

        if (codeReferenceElement.getParent() == null || !(codeReferenceElement.getParent() instanceof PsiJavaCodeReferenceElementImpl)) {
            return;
        }

        if (codeReferenceElement.getParent().getParent() == null || !(codeReferenceElement.getParent().getParent() instanceof PsiImportStatement)) {
            return;
        }

        annotationHolder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
            .textAttributes(TextAttributesKey.createTextAttributesKey("JAVA_PACKAGE_REFERENCE", HighlighterColors.TEXT))
            .create();
    }
}
