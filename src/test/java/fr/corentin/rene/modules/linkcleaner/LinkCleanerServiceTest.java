package fr.corentin.rene.modules.linkcleaner;

import fr.corentin.rene.modules.linkcleaner.service.LinkCleanerService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LinkCleanerServiceTest {
    private final LinkCleanerService service = new LinkCleanerService();

    @Test
    void removesGlobalTrackers() {
        assertEquals("https://example.com/page?id=3", service.clean("https://example.com/page?utm_source=a&id=3&fbclid=xyz"));
        assertEquals("https://example.com/page", service.clean("https://example.com/page?utm_source=a&gclid=b"));
    }

    @Test
    void keepsCleanLinksUntouched() {
        assertEquals("https://example.com/a?x=1#section", service.clean("https://example.com/a?x=1#section"));
        assertEquals("https://example.com/?", service.clean("https://example.com/?"));
    }

    @Test
    void removesProviderSpecificTrackers() {
        assertEquals("https://www.youtube.com/watch?v=abc", service.clean("https://www.youtube.com/watch?v=abc&si=TRACK&feature=share"));
        assertEquals("https://youtu.be/abc", service.clean("https://youtu.be/abc?si=TRACK"));
        assertEquals("https://open.spotify.com/track/1", service.clean("https://open.spotify.com/track/1?si=TRACK"));
        assertEquals("https://www.instagram.com/p/abc/", service.clean("https://www.instagram.com/p/abc/?igsh=TRACK"));
    }

    @Test
    void cleansAmazonRawRulesAndKeepsAffiliateTag() {
        assertEquals("https://www.amazon.fr/dp/B000?tag=moi-21",
                service.clean("https://www.amazon.fr/Produit/dp/B000/ref=sr_1_1?crid=A&sprefix=b&tag=moi-21").replace("/Produit", ""));
    }

    @Test
    void followsTrackingRedirections() {
        assertEquals("https://example.com/page?id=3",
                service.clean("https://www.google.com/url?sa=t&url=https%3A%2F%2Fexample.com%2Fpage%3Futm_source%3Dx%26id%3D3&usg=abc"));
    }

    @Test
    void respectsExceptions() {
        String github = "https://github.com/user/repo?ref=main";
        assertEquals(github, service.clean(github));
    }
}
