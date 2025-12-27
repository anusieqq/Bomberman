package Bomberman;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
public class GraczTest {

    @Test
   public void testGraczInitialization() {
        Gracz gracz = new Gracz(1, 2, 0);

        assertEquals(1, gracz.pobierzX());
        assertEquals(2, gracz.pobierzY());
        assertEquals(0, gracz.pobierzID());
    }

    @Test
   public void testPrzemiesc() {
        Gracz gracz = new Gracz(1, 2, 0);
        gracz.przemiesc(3, 4);

        assertEquals(3, gracz.pobierzX());
        assertEquals(4, gracz.pobierzY());
    }

    @Test
    public void testRespawn() {
        Gracz gracz = new Gracz(1, 2, 0);
        gracz.respawn(5, 6);

        assertEquals(5, gracz.pobierzX());
        assertEquals(6, gracz.pobierzY());
    }
    @Test
    public void testGraczID() {
        Gracz gracz = new Gracz(1, 2, 3);

        assertEquals(3, gracz.pobierzID());
    }


    @Test
    public void testPostawBombe() {
        PanelGry panel = new PanelGry("Gracz1");
        Gracz gracz = new Gracz(1, 1, 0);

        // Sprawdź, czy bomba zostaje postawiona dla gracza 1
        panel.postawBombe(gracz);
        assertEquals(1, panel.getListaBombGracz1().size());
    }




    @Test
    public void testTracZycie() {
        PanelGry panel = new PanelGry("Gracz1");
        Gracz gracz = new Gracz(1, 1, 0);

        // Ustaw zdrowie gracza na 1 i sprawdź, czy tracZycie zmniejsza je o 1
        panel.setZyciaGracza1(1);
        panel.tracZycie(gracz);
        assertEquals(0, panel.getZyciaGracza1());
    }

    @Test
    public void testKoniecGry() {
        PanelGry panel = new PanelGry("Gracz1");

        // Ustaw zdrowie gracza na 0 i sprawdź, czy koniecGry zwraca true
        panel.setZyciaGracza1(0);
        assertTrue(panel.koniecGry());
    }





}
