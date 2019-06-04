package ru.noties.markwon.html.tag;

import android.support.annotation.NonNull;

import org.commonmark.node.ListItem;

import java.util.Arrays;
import java.util.Collection;

import ru.noties.markwon.MarkwonConfiguration;
import ru.noties.markwon.MarkwonVisitor;
import ru.noties.markwon.RenderProps;
import ru.noties.markwon.SpanFactory;
import ru.noties.markwon.SpannableBuilder;
import ru.noties.markwon.core.CoreProps;
import ru.noties.markwon.html.HtmlTag;
import ru.noties.markwon.html.MarkwonHtmlRenderer;
import ru.noties.markwon.html.TagHandler;

public class ListHandler extends TagHandler {

    @Override
    public void handle(
            @NonNull MarkwonVisitor visitor,
            @NonNull MarkwonHtmlRenderer renderer,
            @NonNull HtmlTag tag) {

        if (!tag.isBlock()) {
            return;
        }

        final HtmlTag.Block block = tag.getAsBlock();
        final boolean ol = "ol".equals(block.name());
        final boolean ul = "ul".equals(block.name());

        if (!ol && !ul) {
            return;
        }

        final MarkwonConfiguration configuration = visitor.configuration();
        final RenderProps renderProps = visitor.renderProps();
        final SpanFactory spanFactory = configuration.spansFactory().get(ListItem.class);

        int number = 1;
        final int bulletLevel = currentBulletListLevel(block);

        for (HtmlTag.Block child : block.children()) {

            visitChildren(visitor, renderer, child);

            if (spanFactory != null && "li".equals(child.name())) {

                // insert list item here
                if (ol) {
                    CoreProps.LIST_ITEM_TYPE.set(renderProps, CoreProps.ListItemType.ORDERED);
                    CoreProps.ORDERED_LIST_ITEM_NUMBER.set(renderProps, number++);
                } else {
                    CoreProps.LIST_ITEM_TYPE.set(renderProps, CoreProps.ListItemType.BULLET);
                    CoreProps.BULLET_LIST_ITEM_LEVEL.set(renderProps, bulletLevel);
                }

                SpannableBuilder.setSpans(
                        visitor.builder(),
                        spanFactory.getSpans(configuration, renderProps),
                        child.start(),
                        child.end());
            }
        }
    }

    @NonNull
    @Override
    public Collection<String> supportedTags() {
        return Arrays.asList("ol", "ul");
    }

    private static int currentBulletListLevel(@NonNull HtmlTag.Block block) {
        int level = 0;
        while ((block = block.parent()) != null) {
            if ("ul".equals(block.name())
                    || "ol".equals(block.name())) {
                level += 1;
            }
        }
        return level;
    }
}
