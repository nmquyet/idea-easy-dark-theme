package com.qyengyn.easydarktheme.java;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.impl.source.PsiJavaCodeReferenceElementImpl;
import com.intellij.psi.impl.source.tree.JavaElementType;
import com.qyengyn.easydarktheme.MyHighlightingColors;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class MyJavaExtendedAnnotator implements Annotator {
    private static final HashMap<String, TextAttributesKey> KEYWORDS = new HashMap<>(){{
        put("null", MyHighlightingColors.JAVA_CONSTANT_KEYWORD);
        put("true", MyHighlightingColors.JAVA_CONSTANT_KEYWORD);
        put("false", MyHighlightingColors.JAVA_CONSTANT_KEYWORD);

        put("boolean", MyHighlightingColors.JAVA_PRIMITIVE_KEYWORD);
        put("char", MyHighlightingColors.JAVA_PRIMITIVE_KEYWORD);
        put("byte", MyHighlightingColors.JAVA_PRIMITIVE_KEYWORD);
        put("short", MyHighlightingColors.JAVA_PRIMITIVE_KEYWORD);
        put("int", MyHighlightingColors.JAVA_PRIMITIVE_KEYWORD);
        put("long", MyHighlightingColors.JAVA_PRIMITIVE_KEYWORD);
        put("double", MyHighlightingColors.JAVA_PRIMITIVE_KEYWORD);
        put("float", MyHighlightingColors.JAVA_PRIMITIVE_KEYWORD);

        put("return", MyHighlightingColors.JAVA_TERMINATE_KEYWORD);
        put("throw", MyHighlightingColors.JAVA_TERMINATE_KEYWORD);
        put("break", MyHighlightingColors.JAVA_TERMINATE_KEYWORD);
        put("continue", MyHighlightingColors.JAVA_TERMINATE_KEYWORD);
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

        String keyword = javaToken.getText();
        if (keyword == null || !KEYWORDS.containsKey(keyword)) {
            return;
        }
        annotationHolder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
            .textAttributes(KEYWORDS.get(keyword))
            .create();
    }

    private void annotateImportPackageName(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if (!(psiElement instanceof PsiJavaCodeReferenceElementImpl codeReferenceElement))
            return;
        if (codeReferenceElement.getElementType() != JavaElementType.JAVA_CODE_REFERENCE)
            return;
        if (codeReferenceElement.getParent() == null || !(codeReferenceElement.getParent() instanceof PsiJavaCodeReferenceElementImpl))
            return;
        if (codeReferenceElement.getParent().getParent() == null || !(codeReferenceElement.getParent().getParent() instanceof PsiImportStatement))
            return;

        annotationHolder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
            .textAttributes(MyHighlightingColors.JAVA_IMPORT_PACKAGE_PREFIX)
            .create();
    }
}
