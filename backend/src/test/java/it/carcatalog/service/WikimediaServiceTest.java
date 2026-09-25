package it.carcatalog.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WikimediaServiceTest {

    @Test
    void estraeNomeFileDallaMiniatura() {
        assertEquals("2018_Fiat_Panda_Easy_1.2.jpg", WikimediaService.nomeFile(
                "//upload.wikimedia.org/wikipedia/commons/thumb/d/d2/2018_Fiat_Panda_Easy_1.2.jpg/60px-2018_Fiat_Panda_Easy_1.2.jpg"));
        assertEquals("Renault_Clio_(V,_Facelift).jpg", WikimediaService.nomeFile(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3b/Renault_Clio_%28V%2C_Facelift%29.jpg/60px-x.jpg?x=1"));
        assertNull(WikimediaService.nomeFile("https://esempio.it/immagine.jpg"));
    }

    @Test
    void accettaSoloLicenzeLibere() {
        assertTrue(WikimediaService.licenzaLibera("CC BY-SA 4.0"));
        assertTrue(WikimediaService.licenzaLibera("Public domain"));
        assertFalse(WikimediaService.licenzaLibera("Fair use"));
        assertFalse(WikimediaService.licenzaLibera(""));
    }
}
