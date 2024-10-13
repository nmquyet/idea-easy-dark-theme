package com.qyengyn.easydarktheme;

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactoryBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MyIdentifierHighlightHandlerFactory extends HighlightUsagesHandlerFactoryBase {

    @Override
    public @Nullable HighlightUsagesHandlerBase<PsiElement> createHighlightUsagesHandler(@NotNull Editor editor, @NotNull PsiFile file, @NotNull PsiElement element) {
        return new MyIdentifierHighlightHandler(editor, file, element);
    }
}
