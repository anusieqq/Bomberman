
package Bomberman;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GraczTest {

    @Test
    public void testGraczInitialization() {
        Gracz gracz = new Gracz(1, 2, 2); // Zmiana ID na 2

        assertEquals(1, gracz.pobierzX());
        assertEquals(2, gracz.pobierzY());
        assertEquals(2, gracz.pobierzID()); // Zmiana ID na 2
    }

    @Test
    public void testPrzemiesc() {
        Gracz gracz = new Gracz(1, 2, 2); // Zmiana ID na 2
        gracz.przemiesc(3, 4);

        assertEquals(3, gracz.pobierzX());
        assertEquals(4, gracz.pobierzY());
    }

    @Test
    public void testRespawn() {
        Gracz gracz = new Gracz(1, 2, 2); // Zmiana ID na 2
        gracz.respawn(5, 6);

        assertEquals(5, gracz.pobierzX());
        assertEquals(6, gracz.pobierzY());
    }

    @Test
    public void testGraczID() {
        Gracz gracz = new Gracz(1, 2, 2); // Zmiana ID na 2

        assertEquals(2, gracz.pobierzID()); // Zmiana ID na 2
    }

    @Test
    public void testPostawBombe() {
        PanelGry panel = new PanelGry("Gracz2"); // Zmiana nazwy gracza na Gracz2
        Gracz gracz = new Gracz(1, 1, 2); // Zmiana ID na 2

        // Sprawdź, czy bomba zostaje postawiona dla gracza 2
        panel.postawBombe(gracz);
        assertEquals(1, panel.getListaBombGracz2().size()); // Zmiana na getListaBombGracz2
    }



    @Test
    public void testKoniecGry() {
        PanelGry panel = new PanelGry("Gracz2"); // Zmiana nazwy gracza na Gracz2

        // Ustaw zdrowie gracza na 0 i sprawdź, czy koniecGry zwraca true
        panel.setZyciaGracza2(0); // Zmiana na setZyciaGracza2
        assertTrue(panel.koniecGry());
    }

}
