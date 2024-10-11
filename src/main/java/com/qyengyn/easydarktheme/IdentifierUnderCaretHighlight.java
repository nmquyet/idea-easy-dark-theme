package com.qyengyn.easydarktheme;

import com.intellij.model.Symbol;
import com.intellij.model.psi.impl.TargetsKt;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class IdentifierUnderCaretHighlight implements CaretListener {
    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
        Editor editor = event.getEditor();
        if (editor.getProject() == null) {
            return;
        }

        PsiFile psiFile = PsiUtilCore.getPsiFile(editor.getProject(), editor.getVirtualFile());
        Collection<Symbol> allTargets = TargetsKt.targetSymbols(psiFile, editor.getCaretModel().getOffset());

        var it = allTargets.iterator();
        while (it.hasNext()) {
            var symbol = it.next();
            symbol.
        }

    }
}
