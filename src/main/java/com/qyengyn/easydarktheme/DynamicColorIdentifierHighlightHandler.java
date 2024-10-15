package com.qyengyn.easydarktheme;

import com.intellij.codeHighlighting.TextEditorHighlightingPassRegistrar;
import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.codeInsight.daemon.impl.*;
import com.intellij.codeInsight.highlighting.*;
import com.intellij.injected.editor.EditorWindow;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.model.Symbol;
import com.intellij.model.psi.impl.TargetsKt;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.editor.ex.util.LexerEditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorAndFontDescriptorsProvider;
import com.intellij.openapi.options.colors.ColorSettingsPages;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.AstLoadingFilter;
import com.intellij.util.Consumer;
import com.intellij.util.ObjectUtils;
import com.intellij.util.Processor;
import com.intellij.util.containers.JBIterable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.*;

import static com.intellij.codeInsight.daemon.impl.HighlightInfoType.*;


@SuppressWarnings({"UnstableApiUsage", "KotlinInternalInJava", "UseJBColor"})
public class DynamicColorIdentifierHighlightHandler extends HighlightUsagesHandlerBase<PsiElement> {

    private final Collection<TextRange> myReadAccessRanges;
    private final Collection<TextRange> myWriteAccessRanges;
    private final Collection<TextRange> myCodeBlockMarkerRanges;
    private final int myCaretOffset;
    private static volatile int id;

    private final @Nullable Color myTargetFgColor;
    private final @Nullable Color myTargetBgColor;
    private final TextAttributes defaultReadAttribute;
    private final TextAttributes defaultWriteAttribute;

    protected DynamicColorIdentifierHighlightHandler(@NotNull Editor editor, @NotNull PsiFile file, PsiElement target) {
        super(editor, file);
        this.myCaretOffset = editor.getCaretModel().getOffset();
        this.myReadAccessRanges =  Collections.synchronizedSet(new LinkedHashSet<>());
        this.myWriteAccessRanges = Collections.synchronizedSet(new LinkedHashSet<>());
        this.myCodeBlockMarkerRanges = Collections.synchronizedSet(new LinkedHashSet<>());

        Project project = file.getProject();
        Map<TextAttributesKey, Pair<ColorAndFontDescriptorsProvider, AttributesDescriptor>> keyMap = new HashMap<>();
        List<TextAttributesKey> keys = getTextAttributesKeys(project, editor, target);
        for (TextAttributesKey key : keys) {
            Pair<ColorAndFontDescriptorsProvider, AttributesDescriptor> p = key == null
                    ? null
                    : ColorSettingsPages.getInstance().getAttributeDescriptor(key);
            if (p != null) keyMap.put(key, p);
        }

        EditorColorsScheme colorsScheme = editor.getColorsScheme();
        this.defaultReadAttribute = colorsScheme.getAttributes(ELEMENT_UNDER_CARET_READ.getAttributesKey());
        this.defaultWriteAttribute = colorsScheme.getAttributes(ELEMENT_UNDER_CARET_WRITE.getAttributesKey());

        if (keyMap.isEmpty()) {
            this.myTargetFgColor = null;
            this.myTargetBgColor = null;
        } else {
            ArrayList<Pair<ColorAndFontDescriptorsProvider, AttributesDescriptor>> attrs = new ArrayList<>(keyMap.values());
            attrs.sort(Comparator.comparingInt(o -> -Math.abs(o.second.getKey().getExternalName().length())));

            TextAttributes attribute = attrs.stream()
                    .map(e -> e.second.getKey())
                    .map(colorsScheme::getAttributes)
                    .filter(e -> e.getForegroundColor() != null)
                    .findFirst()
                    .orElse(null);

            if (attribute != null && attribute.getForegroundColor() != null) {
                this.myTargetFgColor = attribute.getForegroundColor();
                this.myTargetBgColor = new Color(
                        myTargetFgColor.getRed(),
                        myTargetFgColor.getGreen(),
                        myTargetFgColor.getBlue(),
                        Integer.getInteger("easydarktheme.highlight.opacity", 60));
            } else {
                this.myTargetFgColor = null;
                this.myTargetBgColor = null;
            }
        }

    }

    @Override
    public @NotNull List<PsiElement> getTargets() {
        return Collections.emptyList();
    }

    @Override
    public void computeUsages(@NotNull List<? extends PsiElement> targets) {
        if (this.myCaretOffset <= 0) {
            return;
        }

        collectCodeBlockMarkerRanges();

        Collection<Symbol> targetSymbols = TargetsKt.targetSymbols(this.myFile, this.myCaretOffset);
        for (Symbol symbol : targetSymbols) {
            computeUsageRanges(symbol);
        }
    }

    private void collectCodeBlockMarkerRanges() {
        InjectedLanguageManager manager = InjectedLanguageManager.getInstance(this.myFile.getProject());
        PsiElement contextElement = this.myFile.findElementAt(
                TargetElementUtil.adjustOffset(
                        this.myFile,
                        this.myEditor.getDocument(),
                        this.myEditor.getCaretModel().getOffset())
        );
        if (contextElement != null) {
            for (TextRange range : CodeBlockSupportHandler.findMarkersRanges(contextElement)) {
                this.myCodeBlockMarkerRanges.add(manager.injectedToHost(contextElement, range));
            }
        }
    }

    private void computeUsageRanges(@NotNull Symbol target) {
        try {
            AstLoadingFilter.disallowTreeLoading(() -> {
                UsageRanges ranges = HighlightUsagesKt.getUsageRanges(myFile, target);
                if (ranges == null) {
                    return;
                }
                myReadAccessRanges.addAll(ranges.getReadRanges());
                myReadAccessRanges.addAll(ranges.getReadDeclarationRanges());
                myWriteAccessRanges.addAll(ranges.getWriteRanges());
                myWriteAccessRanges.addAll(ranges.getWriteDeclarationRanges());
            }, () -> "Currently highlighted file: \n" +
                    "psi file: " + myFile + ";\n" +
                    "virtual file: " + myFile.getVirtualFile());
        }
        catch (IndexNotReadyException ignore) {
            //logIndexNotReadyException(e); // TODO
        }
    }

    @Override
    public boolean highlightReferences() {
        if (this.myReadAccessRanges.isEmpty() && this.myWriteAccessRanges.isEmpty() && this.myCodeBlockMarkerRanges.isEmpty()) {
            IdentifierHighlighterPass.clearMyHighlights(this.myEditor.getDocument(), this.myEditor.getProject());
            return true;
        }
        if (!this.myEditor.isDisposed()) {
            boolean virtSpace = EditorUtil.isCaretInVirtualSpace(this.myEditor);
            List<HighlightInfo> infos = !virtSpace && !this.isCaretOverCollapsedFoldRegion()
                    ? this.createHighlightInfos()
                    : Collections.emptyList();
            PsiFile hostFile = InjectedLanguageManager.getInstance(this.myFile.getProject())
                    .getTopLevelFile(this.myFile);
            BackgroundUpdateHighlightersUtil.setHighlightersToEditor(
                    hostFile.getProject(),
                    hostFile,
                    hostFile.getFileDocument(),
                    hostFile.getTextRange().getStartOffset(),
                    hostFile.getTextRange().getEndOffset(),
                    infos,
                    this.getId());
        }

        return false;
    }

    private boolean isCaretOverCollapsedFoldRegion() {
        return this.myEditor.getFoldingModel().getCollapsedRegionAtOffset(this.myEditor.getCaretModel().getOffset()) != null;
    }

    private @NotNull List<HighlightInfo> createHighlightInfos() {
        if (myReadAccessRanges.isEmpty() && myWriteAccessRanges.isEmpty() && myCodeBlockMarkerRanges.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Pair<Object, TextRange>> existingMarkupTooltips = new HashSet<>();
        for (RangeHighlighter highlighter : myEditor.getMarkupModel().getAllHighlighters()) {
            existingMarkupTooltips.add(Pair.create(highlighter.getErrorStripeTooltip(), highlighter.getTextRange()));
        }

        List<HighlightInfo> result = new ArrayList<>(myReadAccessRanges.size() + myWriteAccessRanges.size() + myCodeBlockMarkerRanges.size());
        for (TextRange range: myReadAccessRanges) {
            result.add(createHighlightInfo(range, ELEMENT_UNDER_CARET_READ, existingMarkupTooltips));
        }
        for (TextRange range: myWriteAccessRanges) {
            result.add(createHighlightInfo(range, ELEMENT_UNDER_CARET_WRITE, existingMarkupTooltips));
        }
        if (CodeInsightSettings.getInstance().HIGHLIGHT_BRACES) {
            myCodeBlockMarkerRanges.forEach(range ->
                    result.add(createHighlightInfo(range, ELEMENT_UNDER_CARET_STRUCTURAL, existingMarkupTooltips)));
        }

        return result;
    }

    private @NotNull HighlightInfo createHighlightInfo(@NotNull TextRange range, @NotNull HighlightInfoType type,
                                                       @NotNull Set<Pair<Object, TextRange>> existingMarkupTooltips) {
        int start = range.getStartOffset();
        String tooltip = start <= myEditor.getDocument().getTextLength()
                ? HighlightHandlerBase.getLineTextErrorStripeTooltip(myEditor.getDocument(), start, false)
                : null;
        String unescapedTooltip = existingMarkupTooltips.contains(new Pair<Object, TextRange>(tooltip, range))
                ? null
                : tooltip;
        HighlightInfo.Builder builder = HighlightInfo.newHighlightInfo(type).range(range);
        if (unescapedTooltip != null) {
            builder.unescapedToolTip(unescapedTooltip);
        }
        if (this.myTargetFgColor != null && this.myTargetBgColor != null) {
            AttributesFlyweight textAttributes =  type == ELEMENT_UNDER_CARET_READ
                    ? AttributesFlyweight.create(this.myTargetFgColor,
                                                 this.myTargetBgColor,
                                                 Font.PLAIN,
                                                 null,
                                                 null,
                                                 this.defaultReadAttribute.getErrorStripeColor())
                    : AttributesFlyweight.create(this.myTargetFgColor,
                                                 //this.defaultWriteAttribute.getBackgroundColor(),
                                                 this.myTargetBgColor,
                                                 Font.PLAIN,
                                                 this.myTargetFgColor,
                                                 EffectType.ROUNDED_BOX,
                                                 this.defaultWriteAttribute.getErrorStripeColor());
            builder.textAttributes(TextAttributes.fromFlyweight(textAttributes));
        }
        return builder.createUnconditionally();
    }

    @Override
    protected void selectTargets(@NotNull List<? extends PsiElement> list,
                                 @NotNull Consumer<? super List<? extends PsiElement>> consumer) {
    }

    private int getId() {
        int id = DynamicColorIdentifierHighlightHandler.id;
        if (id == 0) {

            Method getNextAvailableId = null;
            try {
                getNextAvailableId = TextEditorHighlightingPassRegistrarImpl.class.getDeclaredMethod("getNextAvailableId");
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            getNextAvailableId.setAccessible(true);

            TextEditorHighlightingPassRegistrarImpl registrar = (TextEditorHighlightingPassRegistrarImpl) TextEditorHighlightingPassRegistrar.getInstance(this.myFile.getProject());
            synchronized(DynamicColorIdentifierHighlightHandler.class) {
                id = DynamicColorIdentifierHighlightHandler.id;
                if (id == 0) {
                    try {
                        DynamicColorIdentifierHighlightHandler.id = id = (int) getNextAvailableId.invoke(registrar);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return id;
    }


    public static @NotNull List<TextAttributesKey> getTextAttributesKeys(@NotNull Project project, @NotNull Editor editor, @NotNull PsiElement element) {
        List<TextAttributesKey> keys = new ArrayList<>();
        Ref<TextRange> selectionRef = new Ref<>();
        Ref<Boolean> hasEraseMarkerRef = new Ref<>();
        Processor<RangeHighlighterEx> processor = r -> {
            HighlightInfo info = HighlightInfo.fromRangeHighlighter(r);
            boolean relevant =
                    selectionRef.get().getStartOffset() < r.getEndOffset() &&
                            (selectionRef.get().getLength() == 0 || r.getStartOffset() < selectionRef.get().getEndOffset());
            TextAttributesKey key = info != null && relevant
                    ? ObjectUtils.chooseNotNull(info.forcedTextAttributesKey, info.type.getAttributesKey())
                    : null;
            if (r.getForcedTextAttributes() == TextAttributes.ERASE_MARKER) {
                hasEraseMarkerRef.set(true);
            }
            else if (key != null) {
                keys.add(key);
            }
            return true;
        };
        JBIterable<Editor> editors = editor instanceof EditorWindow ? JBIterable.of(editor, ((EditorWindow)editor).getDelegate()) : JBIterable.of(
                editor);
        for (Editor ed : editors) {
            TextRange selection = element.getTextRange();
            selectionRef.set(selection);
            hasEraseMarkerRef.set(false);
            MarkupModel forDocument = DocumentMarkupModel.forDocument(ed.getDocument(), project, false);
            if (forDocument != null) {
                ((MarkupModelEx)forDocument).processRangeHighlightersOverlappingWith(selection.getStartOffset(), selection.getEndOffset(), processor);
            }
            ((MarkupModelEx)ed.getMarkupModel()).processRangeHighlightersOverlappingWith(selection.getStartOffset(), selection.getEndOffset(), processor);
            EditorHighlighter highlighter = editor.getHighlighter();
            SyntaxHighlighter syntaxHighlighter = highlighter instanceof LexerEditorHighlighter ? ((LexerEditorHighlighter)highlighter).getSyntaxHighlighter() : null;
            if (syntaxHighlighter != null && !hasEraseMarkerRef.get()) {
                HighlighterIterator iterator = highlighter.createIterator(selection.getStartOffset());
                while (!iterator.atEnd()) {
                    for (TextAttributesKey key : syntaxHighlighter.getTokenHighlights(iterator.getTokenType())) {
                        if (key != null) {
                            keys.add(key);
                        }
                    }
                    if (iterator.getEnd() >= selection.getEndOffset()) break;
                    iterator.advance();
                }
            }
        }
        return keys;
    }
}
