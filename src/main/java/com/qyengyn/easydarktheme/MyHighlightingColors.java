package com.qyengyn.easydarktheme;

import com.intellij.ide.highlighter.JavaHighlightingColors;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

public final class MyHighlightingColors {
    public static final TextAttributesKey JAVA_CONSTANT_KEYWORD = TextAttributesKey.createTextAttributesKey("JAVA_CONSTANT_KEYWORD", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey JAVA_PRIMITIVE_KEYWORD = TextAttributesKey.createTextAttributesKey("JAVA_PRIMITIVE_KEYWORD", JavaHighlightingColors.CLASS_NAME_ATTRIBUTES);
    public static final TextAttributesKey JAVA_IMPORT_PACKAGE_PREFIX = TextAttributesKey.createTextAttributesKey("JAVA_PACKAGE_REFERENCE", HighlighterColors.TEXT);
}
