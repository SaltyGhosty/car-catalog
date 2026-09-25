package it.carcatalog.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class EmailServiceTest {

    @Test
    void ogniValoreVieneEscapato() {
        var d = new DatiNotifica(1L, "x@y.it", "<script>alert(1)</script>", "Fi\"at", "<b>Panda</b>",
                new BigDecimal("9000"), new BigDecimal("9500"), "tok");
        String html = EmailService.html(d, "https://front.example/avvisi/disattiva#token=tok");
        assertFalse(html.contains("<script>"));
        assertFalse(html.contains("<b>Panda</b>"));
        assertTrue(html.contains("&lt;script&gt;"));
    }
}
