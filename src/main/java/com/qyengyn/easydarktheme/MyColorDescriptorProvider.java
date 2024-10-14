package com.qyengyn.easydarktheme;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorAndFontDescriptorsProvider;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NotNull;

public class MyColorDescriptorProvider implements ColorAndFontDescriptorsProvider {
    private static AttributesDescriptor[] ATTRS = {
        new AttributesDescriptor("Java//Additional Keyword//Primitive Keyword", TextAttributesKey.createTextAttributesKey("JAVA_PRIMITIVE_KEYWORD")),
        new AttributesDescriptor("Java//Additional Keyword//Import Package Prefix", TextAttributesKey.createTextAttributesKey("JAVA_PACKAGE_REFERENCE")),
        new AttributesDescriptor("Java//Additional Keyword//Constant", TextAttributesKey.createTextAttributesKey("JAVA_CONSTANT_KEYWORD"))
    };

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
}
