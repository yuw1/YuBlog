package cn.net.wangshifu.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlSanitizerTest {

    private final HtmlSanitizer sanitizer = new HtmlSanitizer();

    @Test
    void keepsTheFormattingTheEditorProduces() {
        String clean = sanitizer.sanitize(
                "<p class=\"lead\">lead</p><h2>title</h2><blockquote>quote</blockquote>"
                        + "<ul><li>one</li></ul><hr><strong>bold</strong>");
        assertTrue(clean.contains("<h2>title</h2>"));
        assertTrue(clean.contains("class=\"lead\""));
        assertTrue(clean.contains("<hr>"));
    }

    @Test
    void dropsScriptsAndEventHandlers() {
        String clean = sanitizer.sanitize(
                "<p>ok</p><script>alert(1)</script><img src=x onerror=alert(2)><a href=\"javascript:alert(3)\">x</a>");
        assertFalse(clean.contains("<script"));
        assertFalse(clean.contains("onerror"));
        assertFalse(clean.contains("javascript:"));
        assertTrue(clean.contains("ok"));
    }

    @Test
    void dropsUnknownClassesButKeepsTheAllowedOne() {
        String clean = sanitizer.sanitize("<p class=\"evil lead\">x</p>");
        assertTrue(clean.contains("class=\"lead\""));
        assertFalse(clean.contains("evil"));
    }
}