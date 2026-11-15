package cn.net.wangshifu.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Article bodies are rich text produced by a hand written editor, so they are HTML.
 * They are cleaned on write and rendered with {@code th:utext} on read.
 *
 * <p>This replaces the 2018 behaviour of stuffing the raw body into a hidden input and
 * calling {@code $('#content').html(...)}, which turned every article into a stored XSS.
 */
@Component
public class HtmlSanitizer {

    private static final Set<String> ALLOWED_CLASSES = Set.of("lead", "am-article-lead");

    private final Safelist safelist;

    public HtmlSanitizer() {
        this.safelist = Safelist.relaxed()
                .addTags("hr", "figure", "figcaption", "s", "del", "ins")
                .addAttributes("p", "class")
                .addAttributes("img", "loading")
                .addProtocols("img", "src", "http", "https", "data")
                .preserveRelativeLinks(true);
    }

    public String sanitize(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        String cleaned = Jsoup.clean(html, "", safelist,
                new Document.OutputSettings().prettyPrint(false));
        // class is only allowed so that "am-article-lead" survives; drop anything else
        Document doc = Jsoup.parseBodyFragment(cleaned);
        for (Element el : doc.select("[class]")) {
            java.util.List<String> kept = el.classNames().stream()
                    .filter(ALLOWED_CLASSES::contains)
                    .toList();
            if (kept.isEmpty()) {
                el.removeAttr("class");
            } else {
                el.attr("class", String.join(" ", kept));
            }
        }
        for (Element link : doc.select("a[href]")) {
            link.attr("rel", "nofollow noopener noreferrer");
            link.attr("target", "_blank");
        }
        return doc.body().html();
    }
}