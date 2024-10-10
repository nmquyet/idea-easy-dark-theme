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

import java.util.HashMap;

public class JavaExtendedKeywordAnnotator implements Annotator {
    private static final HashMap<String, String> KEYWORDS = new HashMap<>(){{
        put("null", "JAVA_CONSTANT_KEYWORD");
        put("true", "JAVA_CONSTANT_KEYWORD");
        put("false", "JAVA_CONSTANT_KEYWORD");

        put("boolean", "JAVA_PRIMITIVE_KEYWORD");
        put("char", "JAVA_PRIMITIVE_KEYWORD");
        put("byte", "JAVA_PRIMITIVE_KEYWORD");
        put("short", "JAVA_PRIMITIVE_KEYWORD");
        put("int", "JAVA_PRIMITIVE_KEYWORD");
        put("long", "JAVA_PRIMITIVE_KEYWORD");
        put("double", "JAVA_PRIMITIVE_KEYWORD");
        put("float", "JAVA_PRIMITIVE_KEYWORD");
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
            .textAttributes(TextAttributesKey.createTextAttributesKey(
                    KEYWORDS.get(keyword),
                    DefaultLanguageHighlighterColors.KEYWORD)
            )
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
            .textAttributes(TextAttributesKey.createTextAttributesKey(
                "JAVA_PACKAGE_REFERENCE",
                HighlighterColors.TEXT)
            )
            .create();
    }
}
