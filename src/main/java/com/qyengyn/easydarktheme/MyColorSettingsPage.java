package com.qyengyn.easydarktheme;

import com.intellij.ide.highlighter.JavaFileHighlighter;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.pom.java.LanguageLevel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class MyColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] ATTRS = {
        new AttributesDescriptor("Java//Keyword//Primitive Type", MyHighlightingColors.JAVA_PRIMITIVE_KEYWORD),
        new AttributesDescriptor("Java//Keyword//Constant", MyHighlightingColors.JAVA_CONSTANT_KEYWORD),
        new AttributesDescriptor("Java//Keyword//Terminate", MyHighlightingColors.JAVA_TERMINATE_KEYWORD),
        new AttributesDescriptor("Java//Import Statement//Package Prefix", MyHighlightingColors.JAVA_IMPORT_PACKAGE_PREFIX)
    };

    private static final Map<String, TextAttributesKey> TAGS = new HashMap<>() {{
        put("importPackagePrefix", MyHighlightingColors.JAVA_IMPORT_PACKAGE_PREFIX);
        put("primitive", MyHighlightingColors.JAVA_PRIMITIVE_KEYWORD);
        put("constant", MyHighlightingColors.JAVA_CONSTANT_KEYWORD);
        put("terminate", MyHighlightingColors.JAVA_TERMINATE_KEYWORD);
    }};

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return ATTRS;
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @Override
    public @NotNull @NlsContexts.ConfigurableName String getDisplayName() {
        return "Easy Dark";
    }

    @Override
    public @Nullable Icon getIcon() {
        return null;
    }

    @Override
    public @NotNull SyntaxHighlighter getHighlighter() {
        return new JavaFileHighlighter(LanguageLevel.HIGHEST);
    }

    @Override
    public @NonNls @NotNull String getDemoText() {
        return """
        import <importPackagePrefix>java.util</importPackagePrefix>.List
        class MyClass {
            private <primitive>int</primitive> number;
            private <primitive>boolean</primitive> isTrue;

            public <primitive>boolean</primitive> isPositive() {
                if (number == 0) {
                    <terminate>return</terminate> <constant>true</constant>;
                }
                return this.number > 0;
            }
        }
        """;
    }

    @Override
    public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return TAGS;
    }
}
