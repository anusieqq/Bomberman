package Bomberman;
import java.io.*;
import java.net.Socket;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;

import Bomberman.BombermanServer.EventType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Klasa reprezentująca gracza.
 */
class Gracz {
    private int x;  // Aktualna pozycja gracza w osi X
    private int y;  // Aktualna pozycja gracza w osi Y
    private int id; // Identyfikator gracza

    /**
     * Konstruktor ustawiający początkową pozycję gracza.
     * @param initialX Początkowa pozycja gracza w osi X.
     * @param initialY Początkowa pozycja gracza w osi Y.
     * @param id Identyfikator gracza.
     */
    public Gracz(int initialX, int initialY, int id) {
        this.x = initialX;
        this.y = initialY;
        this.id = id;
    }

    /**
     * Metoda zwracająca aktualną pozycję gracza w osi X.
     * @return Aktualna pozycja gracza w osi X.
     */
    public int pobierzX() {
        return x;
    }

    /**
     * Metoda zwracająca aktualną pozycję gracza w osi Y.
     * @return Aktualna pozycja gracza w osi Y.
     */
    public int pobierzY() {
        return y;
    }

    /**
     * Metoda do przemieszczania gracza na nową pozycję.
     * @param noweX Nowa pozycja gracza w osi X.
     * @param noweY Nowa pozycja gracza w osi Y.
     */
    public void przemiesc(int noweX, int noweY) {
        this.x = noweX;
        this.y = noweY;
    }

    /**
     * Metoda do respawnowania gracza na nowej pozycji.
     * @param respawnX Nowa pozycja gracza w osi X po respawnie.
     * @param respawnY Nowa pozycja gracza w osi Y po respawnie.
     */
    public void respawn(int respawnX, int respawnY) {
        this.x = respawnX;
        this.y = respawnY;
    }

    /**
     * Metoda zwracająca identyfikator gracza.
     * @return Identyfikator gracza.
     */
    public int pobierzID() {
        return id;
    }
}
/**
 * Klasa reprezentująca panel z obrazem tła.
 */
class PanelObrazu extends JPanel {
    private Image obrazTla;  // Obraz tła, który będzie wyświetlany na panelu

    /**
     * Konstruktor wczytujący obraz tła z pliku.
     * @param nazwaPliku Nazwa pliku z obrazem tła.
     */
    public PanelObrazu(String nazwaPliku) {
        try {
            obrazTla = ImageIO.read(new File(nazwaPliku));
        } catch (IOException e) {
            e.printStackTrace();  // Wypisz informację o błędzie w przypadku niepowodzenia wczytania obrazu
        }
    }

    /**
     * Przesłonięta metoda do rysowania komponentu.
     * @param g Obiekt Graphics do rysowania.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(obrazTla, 0, 0, this.getWidth(), this.getHeight(), this);  // Rysuj obraz tła na panelu
    }
}

/**
 * Klasa reprezentująca panel gry.
 */
class PanelGry extends JPanel  {
    private boolean odbieranieDanych = true;  // Flaga odbierania danych od serwera

    private static final Logger logger = LogManager.getLogger(PanelGry.class);  // Logger do logowania zdarzeń
    private Socket socket;  // Gniazdo do komunikacji z serwerem
    private ObjectInputStream input;  // Strumień wejściowy do odbierania danych od serwera
    private ObjectOutputStream output;  // Strumień wyjściowy do wysyłania danych do serwera
    private static final int MAX_BOMBS = 5;  // Maksymalna liczba bomb, które gracz może postawić
    private int iloscPostawionychBomb;  // Licznik postawionych bomb

    // Dodaj zmienne kierunku dla obu graczy
    private int kierunekGracza1;  // Kierunek ruchu gracza 1
    private int kierunekGracza2;  // Kierunek ruchu gracza 2

    private int zyciaGracza1;  // Liczba żyć gracza 1
    private int zyciaGracza2;  // Liczba żyć gracza 2
    private Image serce;  // Obraz reprezentujący życie gracza

    // Obrazy elementów gry
    private Image obrazTrawy;
    private Image obrazKamienia;
    private Image gora;
    private Image dol;
    private Image lewo;
    private Image prawo;
    private Image gora1;
    private Image dol1;
    private Image lewo1;
    private Image prawo1;
    private Image obrazBomby;
    private Image obrazKamienia1;

    private String loginGracza1;  // Login gracza 1
    private Gracz gracz;  // Obiekt gracza 1
    private Gracz gracz2;  // Obiekt gracza 2

    // Stałe do reprezentacji elementów na mapie
    private static final String TRAWA = "G";
    private static final String SCIANA = "H";
    private static final String SCIANA1 = "W";

    private String[][] ukladMapy;  // Układ mapy gry
    private int szerokoscMapy;  // Szerokość mapy
    private int wysokoscMapy;  // Wysokość mapy

    private int kierunekGracza;  // Kierunek ruchu aktualnie sterowanego gracza
    private boolean bombaPostawiona;  // Flaga informująca o postawionej bombie
    private int bombaX;  // Pozycja X postawionej bomby
    private int bombaY;  // Pozycja Y postawionej bomby
    private Timer timerBomby;  // Timer do obsługi bomb
    private List<Bomba> listaBombGracz1;  // Lista bomb gracza 1
    private List<Bomba> listaBombGracz2;  // Lista bomb gracza 2
    private Timer timerBombyGracz1;  // Timer do obsługi bomb gracza 1
    private Timer timerBombyGracz2;  // Timer do obsługi bomb gracza 2
    // Konstruktor panelu gry
    public PanelGry(String loginGracza1) {
        this.loginGracza1 = loginGracza1;

        // Inicjalizacja obrazów elementów gry
        try {
            obrazTrawy = ImageIO.read(new File("trawa.jpg"));
            obrazKamienia = ImageIO.read(new File("kamien2.jpg"));
            gora = ImageIO.read(new File("up.png"));
            dol = ImageIO.read(new File("postac.png"));
            lewo = ImageIO.read(new File("left.png"));
            prawo = ImageIO.read(new File("right.png"));
            gora1 = ImageIO.read(new File("up1.png"));
            dol1 = ImageIO.read(new File("postac1.png"));
            lewo1 = ImageIO.read(new File("left1.png"));
            prawo1 = ImageIO.read(new File("right1.png"));
            obrazBomby = ImageIO.read(new File("bomba.png"));
            obrazKamienia1 = ImageIO.read(new File("ziemia.jpg"));
        } catch (IOException e) {
            e.printStackTrace();  // Wypisz informację o błędzie w przypadku niepowodzenia wczytania obrazu
        }

        // Inicjalizacja gniazda i strumieni komunikacyjnych
        try {
            socket = new Socket("localhost", 8080);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();  // Wypisz informację o błędzie w przypadku problemów z gniazdem
        }


        Thread watekOdbierania = new Thread(new OdbiorcaZdarzen());
        watekOdbierania.start();
        // Inicjalizacja zmiennych gry
        this.zyciaGracza1 = 3; // Domyślna liczba żyć dla gracza 1
        this.zyciaGracza2 = 3; // Domyślna liczba żyć dla gracza 2
        inicjalizujMape();
        gracz = new Gracz(1, 1, 0);
        gracz2 = new Gracz(19, 11, 1);
        inicjalizujZdrowie();

        kierunekGracza1 = 0;  // Domyślny kierunek gracza 1
        kierunekGracza2 = 0;  // Domyślny kierunek gracza 2

        listaBombGracz1 = new ArrayList<>();
        listaBombGracz2 = new ArrayList<>();

        // Inicjalizacja timerów obsługujących bomby
        timerBombyGracz1 = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                detonujBomby(listaBombGracz1, timerBombyGracz1);
                logger.info("EKSPLOZJA(" + bombaX + ", " + bombaY + ")");

                Bomberman.BombermanServer.BombermanEvent EKSPLOZJA = new Bomberman.BombermanServer.BombermanEvent(EventType.EKSPLOZJA, 0,bombaX,bombaY);
                wyslijZdarzenieDoSerwera(EKSPLOZJA,0,bombaX,bombaY);

            }
        });
        timerBombyGracz1.setRepeats(false);

        timerBombyGracz2 = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                detonujBomby(listaBombGracz2, timerBombyGracz2);
            }
        });
        timerBombyGracz2.setRepeats(false);

        // Dodanie KeyListenera do obsługi klawiatury
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                nacisniecieKlawisza(e.getKeyCode());
                repaint();
            }

            @Override //postawienie bomby
            public void keyTyped(KeyEvent e) {
                if(gracz.pobierzID()==0){
                if (e.getKeyChar() == ' ' ) {
                    postawBombe(gracz);
                    repaint();
                }}
                else if(gracz.pobierzID()==1){
                 if (e.getKeyChar() == ' ') {
                    postawBombe(gracz2);
                    repaint();
                }}
                logger.info("BOMBA(" + bombaX + ", " + bombaY + ")");
                Bomberman.BombermanServer.BombermanEvent BOMBA = new Bomberman.BombermanServer.BombermanEvent(EventType.BOMBA, 0,bombaX,bombaY);
                wyslijZdarzenieDoSerwera(BOMBA,0,bombaX,bombaY);
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });


        this.setFocusable(true);
        this.requestFocusInWindow();
    }

    // Getter dla listy bomb gracza 1
    public List<Bomba> getListaBombGracz1() {
        return listaBombGracz1;
    }

    // Getter dla życia gracza 1
    public int getZyciaGracza1() {
        return zyciaGracza1;
    }

    // Setter dla życia gracza 1
    public void setZyciaGracza1(int zyciaGracza1) {
        this.zyciaGracza1 = zyciaGracza1;
    }
// Metoda do wysyłania zdarzenia do serwera
    private void wyslijZdarzenieDoSerwera(Bomberman.BombermanServer.BombermanEvent zdarzenie,int id,int x,int y) {
        zdarzenie.setPlayerId(id);
        zdarzenie.setX(x);
        zdarzenie.setY(y);
        // Obsługa zdarzeń od serwera
        obsluzZdarzeniaOdSerwera(zdarzenie);
        try {
            if (output != null) {

                output.writeObject(zdarzenie);
                output.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


//odbiera zdarzenia od klienta
    private class OdbiorcaZdarzen implements Runnable {
        @Override
        public void run() {
            try {
                while (odbieranieDanych) {
                    // Odbierz zdarzenie od serwera
                    Bomberman.BombermanServer.BombermanEvent zdarzenie = (Bomberman.BombermanServer.BombermanEvent) input.readObject();

                    // Przetwórz zdarzenie
                    obsluzZdarzeniaOdSerwera(zdarzenie);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }




    // Metoda do obsługi zdarzeń od serwera
    private void obsluzZdarzeniaOdSerwera(Bomberman.BombermanServer.BombermanEvent zdarzenie) {
        if (zdarzenie.getEventType() == EventType.RUCH1) {

            // Obsługa zdarzenia ruchu
            int noweX = zdarzenie.getX();
            int noweY = zdarzenie.getY();

            if (!czyKolizja( noweX, noweY)) {
                int deltaX = noweX - gracz2.pobierzX();
                int deltaY = noweY - gracz2.pobierzY();

                if (deltaY < 0) {
                   kierunekGracza2=0;

                    repaint();
                } else if (deltaY > 0) {
                    kierunekGracza2=1;
                    repaint();
                } else if (deltaX < 0) {
                    kierunekGracza2=2;
                    repaint();
                } else if (deltaX > 0) {
                    kierunekGracza2=3;
                    repaint();
                }

                gracz2.przemiesc(noweX, noweY);

                repaint();
            }

            logger.info("odebrano zdarzenie: " + zdarzenie.getEventType() + " x:"+ zdarzenie.getX() +" y:" + zdarzenie.getY());
            repaint();
        } else if (zdarzenie.getEventType() == EventType.BOMBA1) {
            // Obsługa zdarzenia postawienia bomby
            bombaX = zdarzenie.getX();
            bombaY = zdarzenie.getY();

            postawBombe(gracz2);
            repaint();
        } else if (zdarzenie.getEventType() == EventType.EKSPLOZJA1) {
            // Obsługa zdarzenia eksplozji bomby
            bombaX = zdarzenie.getX();
            bombaY = zdarzenie.getY();

           // detonujBomby(listaBombGracz2,timerBombyGracz2);
            repaint();
        } else if (zdarzenie.getEventType() == EventType.RESPAWN1) {
            // Obsługa zdarzenia respawnu gracza



                gracz2.respawn(19, 11); // Przesuwa gracza 2 na początkową pozycję
            repaint();

            }else if (zdarzenie.getEventType() == EventType.ZYCIE1) {


            zyciaGracza2 -= 1;


            repaint();
            if (koniecGry()) {
                koniecGryInformacja();
            }
        }
    }

    //do konczenia gry
    boolean koniecGry() {
        return zyciaGracza1 <= 0 || zyciaGracza2 <= 0;
    }

    // Inicjalizacja mapy gry
    private void inicjalizujMape() {
        szerokoscMapy = 21;
        wysokoscMapy = 13;
        /**
         * H - bloki niezniszczalne
         * G - trawa
         * W - bloki zniszczalne
         */
        ukladMapy = new String[][]{
                {"H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},
                {"H", "G", "G", "G", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "H"},
                {"H", "G", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H"},
                {"H", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "H"},
                {"H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H"},
                {"H", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "H"},
                {"H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H"},
                {"H", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "H"},
                {"H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H"},
                {"H", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "H"},
                {"H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "W", "H", "G", "H"},
                {"H", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "G", "G", "G", "H"},
                {"H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H", "H"}
        };
    }


    // Umieszczenie bomby na planszy
    void postawBombe(Gracz gracz) {
        List<Bomba> listaBomb = (gracz.pobierzID() == 0) ? listaBombGracz1 : listaBombGracz2;

        if (listaBomb.size() < MAX_BOMBS) {
            int bombaX = gracz.pobierzX();
            int bombaY = gracz.pobierzY();
            Bomba nowaBomba = new Bomba(bombaX, bombaY);
            listaBomb.add(nowaBomba);

            Timer timerBombyGracza = (gracz.pobierzID() == 0) ? timerBombyGracz1 : timerBombyGracz2;
            if (!bombaPostawiona) {
                bombaPostawiona = true;
                timerBombyGracza.start();
            }

            repaint();
        }
    }


//eksplozja w kolumnie i wierszu
    private boolean czyKolizjaWKolumnieWierszu(int x, int y, Gracz gracz, int zasieg) {
        int xGracza = gracz.pobierzX();
        int yGracza = gracz.pobierzY();

        // Sprawdź, czy x lub y mieści się w zakresie zasięgu
        return (x >= xGracza - zasieg && x <= xGracza + zasieg && y == yGracza) ||
                (y >= yGracza - zasieg && y <= yGracza + zasieg && x == xGracza);
    }

//detonacja bomby (wykorzystuje funkcje explode, ktora wykorzystuje funkcje explodeblocks)
    private void detonujBomby(List<Bomba> listaBomb, Timer timer) {
        Iterator<Bomba> iterator = listaBomb.iterator();
        while (iterator.hasNext()) {
            Bomba bomba = iterator.next();
            int zasieg = 2;  // Określ zasięg eksplozji

            explode(bomba.getX(), bomba.getY(), 0, -1, zasieg); // Eksplozja w górę
            explode(bomba.getX(), bomba.getY(), 0, 1, zasieg);  // Eksplozja w dół
            explode(bomba.getX(), bomba.getY(), -1, 0, zasieg); // Eksplozja w lewo
            explode(bomba.getX(), bomba.getY(), 1, 0, zasieg);  // Eksplozja w prawo
            explode(bomba.getX(), bomba.getY(), 0, 0, 1);  // Eksplozja w miejscu

            // Sprawdzenie, czy bomba eksploduje w obszarze gracza 1
            if (czyKolizjaZGraczem(bomba.getX(), bomba.getY(), gracz) || czyKolizjaWKolumnieWierszu(bomba.getX(), bomba.getY(), gracz, zasieg)) {
                tracZycie(gracz);
            }

            // Sprawdzenie, czy bomba eksploduje w obszarze gracza 2
            if (czyKolizjaZGraczem(bomba.getX(), bomba.getY(), gracz2) || czyKolizjaWKolumnieWierszu(bomba.getX(), bomba.getY(), gracz2, zasieg)) {
               // tracZycie(gracz2);
            }

            iterator.remove();
            iloscPostawionychBomb--;
        }

        bombaPostawiona = !listaBomb.isEmpty();
        repaint();
        if (koniecGry()) {
            koniecGryInformacja();
        }

        if (listaBomb.isEmpty()) {
            timer.stop();
        }
    }


    //okienko na koniec gry
    private void koniecGryInformacja() {
        JOptionPane.showMessageDialog(
                this,
                "Koniec gry!\nWygrał gracz: " + (zyciaGracza1 > 0 ? "Gracz 1" : "Gracz 2"),
                "Koniec gry",
                JOptionPane.INFORMATION_MESSAGE);

        System.exit(0);
    }


    // Metoda sprawdzająca kolizję z graczem
    private boolean czyKolizjaZGraczem(int x, int y, Gracz gracz) {
        return x == gracz.pobierzX() && y == gracz.pobierzY();
    }

    // Metoda do rysowania paska zdrowia graczy
    private void rysujPasekZdrowia(Graphics g) {
        int szerokoscSerce = 30;
        int wysokoscSerce = 30;
        int margines = 10;

        // Rysuj pasek zdrowia dla gracza 1 po lewej
        for (int i = 0; i < zyciaGracza1; i++) {
            g.drawImage(serce, margines + i * (szerokoscSerce + margines)+200, 1, szerokoscSerce, wysokoscSerce, this);
        }

        // Rysuj pasek zdrowia dla gracza 2 po prawej
        for (int i = 0; i < zyciaGracza2; i++) {
            g.drawImage(serce, getWidth() - margines - (i + 1) * (szerokoscSerce + margines)-200, 1, szerokoscSerce, wysokoscSerce, this);
        }
    }

    // Metoda do inicjalizacji zdrowia graczy
    private void inicjalizujZdrowie() {
        try {
            serce = ImageIO.read(new File("serce.png"));
        } catch (IOException e) {
            e.printStackTrace();}
        zyciaGracza1 = 3;
        zyciaGracza2 = 3;
    }
    // Metoda do zmniejszania zdrowia gracza
    void tracZycie(Gracz gracz) {
        if (gracz.pobierzID() == 0 && zyciaGracza1 > 0) {
            zyciaGracza1--;
            gracz.respawn(1,1);
        } else if (gracz.pobierzID() == 1 && zyciaGracza2 > 0) {
            zyciaGracza2--;
            gracz2.respawn(19,11);
        }
        logger.info("ZYCIE" + zyciaGracza1);
        logger.info("RESPAWN: " + gracz.pobierzX()+ " , " + gracz.pobierzY());
        Bomberman.BombermanServer.BombermanEvent ZYCIE = new Bomberman.BombermanServer.BombermanEvent(EventType.ZYCIE, 0,1,1);
wyslijZdarzenieDoSerwera(ZYCIE,0,1,1);
        Bomberman.BombermanServer.BombermanEvent RESPAWN = new Bomberman.BombermanServer.BombermanEvent(EventType.RESPAWN, 0,1,1);
        wyslijZdarzenieDoSerwera(RESPAWN,0,1,1);


    }

    private void explode(int startX, int startY, int directionX, int directionY, int radius) {
        explodeBlocks(startX, startY, directionX, directionY, radius);

    }
    // Obsługa eksplozji bloków w zadanym kierunku i zasięgu
    private void explodeBlocks(int startX, int startY, int directionX, int directionY, int radius) {
        for (int i = 1; i <= radius; i++) {
            int x = startX + i * directionX;
            int y = startY + i * directionY;

            // Sprawdzenie, czy pozycja znajduje się na planszy i czy nie ma kolizji z blokiem ściany
            if (x < 0 || x >= szerokoscMapy || y < 0 || y >= wysokoscMapy || ukladMapy[y][x].equals(SCIANA)) {
                break;
            }

            // Jeśli blok to ściana, zmień go na trawę i wyślij zdarzenie do serwera
            if (ukladMapy[y][x].equals(SCIANA1)) {
                ukladMapy[y][x] = TRAWA;
                logger.info("ZNISZCZENIE(" + x + ", " + y + ")");
                Bomberman.BombermanServer.BombermanEvent ZNISZCZENIE1 = new Bomberman.BombermanServer.BombermanEvent(EventType.ZNISZCZENIE1, 1, x, y);
                wyslijZdarzenieDoSerwera(ZNISZCZENIE1,0,x,y);
            } else {
                break;  // Zakończ pętlę, jeśli natrafiono na inny blok niż ściana
            }
        }
    }




    // Obsługa naciśnięcia klawisza
    private void nacisniecieKlawisza(int keyCode) {
        int noweXGracz1 = gracz.pobierzX();
        int noweYGracz1 = gracz.pobierzY();

        int noweXGracz2 = gracz2.pobierzX();
        int noweYGracz2 = gracz2.pobierzY();
 if(gracz.pobierzID()==0){
        switch (keyCode) {
            case KeyEvent.VK_UP:
                noweYGracz1 = Math.max(0, gracz.pobierzY() - 1);
                kierunekGracza1 = 0;
                break;
            case KeyEvent.VK_DOWN:
                noweYGracz1 = Math.min(wysokoscMapy - 1, gracz.pobierzY() + 1);
                kierunekGracza1 = 1;
                break;

            case KeyEvent.VK_LEFT:
                noweXGracz1 = Math.max(0, gracz.pobierzX() - 1);
                kierunekGracza1 = 2;
                break;
            case KeyEvent.VK_RIGHT:
                noweXGracz1 = Math.min(szerokoscMapy - 1, gracz.pobierzX() + 1);
                kierunekGracza1 = 3;
                break;}
     if (!czyKolizja(noweXGracz1, noweYGracz1)) {
         gracz.przemiesc(noweXGracz1, noweYGracz1);
     }
 }
 else if(gracz.pobierzID()==1){
                switch (keyCode){
            case KeyEvent.VK_W:
                noweYGracz2 = Math.max(0, gracz2.pobierzY() - 1);
                kierunekGracza2 = 0;
                break;
            case KeyEvent.VK_S:
                noweYGracz2 = Math.min(wysokoscMapy - 1, gracz2.pobierzY() + 1);
                kierunekGracza2 = 1;
                break;
            case KeyEvent.VK_A:
                noweXGracz2 = Math.max(0, gracz2.pobierzX() - 1);
                kierunekGracza2 = 2;
                break;
            case KeyEvent.VK_D:
                noweXGracz2 = Math.min(szerokoscMapy - 1, gracz2.pobierzX() + 1);
                kierunekGracza2 = 3;
                break;
        }
     if (!czyKolizja(noweXGracz2, noweYGracz2)) {
         gracz2.przemiesc(noweXGracz2, noweYGracz2);
     }}

        logger.info("RUCH(" + noweXGracz1 + ", " + noweYGracz1 + ")");


        Bomberman.BombermanServer.BombermanEvent RUCH = new Bomberman.BombermanServer.BombermanEvent(EventType.RUCH, 0,noweXGracz1,noweYGracz1);
        wyslijZdarzenieDoSerwera(RUCH,0,noweXGracz1,noweYGracz1);


    }

    // Sprawdzenie kolizji z elementem planszy
    boolean czyKolizja(int x, int y) {
        return ukladMapy[y][x].equals(SCIANA) || ukladMapy[y][x].equals(SCIANA1);
    }

    // Przesłonięta metoda do rysowania komponentu
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        rysujTlo(g);
        rysujInformacjeOGraczu(g);
        rysujGracza(g, gracz.pobierzX(), gracz.pobierzY());
        rysujGracza(g, gracz2.pobierzX(), gracz2.pobierzY());
        rysujBomby(g);
    }

    // Rysowanie tła planszy
    private void rysujTlo(Graphics g) {
        int szerokoscKafelka = getWidth() / szerokoscMapy;
        int wysokoscKafelka = getHeight() / wysokoscMapy;
// Iteracja przez mapę i rysowanie odpowiednich obrazków na podstawie elementów
        for (int y = 0; y < wysokoscMapy; y++) {
            for (int x = 0; x < szerokoscMapy; x++) {
                if (ukladMapy[y][x].equals(TRAWA)) {
                    int xPos = x * szerokoscKafelka;
                    int yPos = y * wysokoscKafelka;
                    g.drawImage(obrazTrawy, xPos, yPos, szerokoscKafelka, wysokoscKafelka, this);
                } else if (ukladMapy[y][x].equals(SCIANA)) {
                    int xPos = x * szerokoscKafelka;
                    int yPos = y * wysokoscKafelka;
                    g.drawImage(obrazKamienia, xPos, yPos, szerokoscKafelka, wysokoscKafelka, this);
                } else if (ukladMapy[y][x].equals(SCIANA1)) {
                    int xPos = x * szerokoscKafelka;
                    int yPos = y * wysokoscKafelka;
                    g.drawImage(obrazKamienia1, xPos, yPos, szerokoscKafelka, wysokoscKafelka, this);
                }
            }
        }
    }



    // Rysowanie informacji o graczu

    private void rysujInformacjeOGraczu(Graphics g) {
        int szerokoscPanelu = getWidth();
        int wysokoscPanelu = getHeight();

        g.setColor(Color.WHITE);
        g.setFont(new Font("Verdana", Font.PLAIN, 20));

        // Rysowanie loginu gracza 1
        String informacjeGracz1 = "Gracz 1: " + loginGracza1;
        int xGracz1 = 10;
        int yGracz1 = 20;
        g.drawString(informacjeGracz1, xGracz1, yGracz1);

        // Rysowanie loginu gracza 2
        String informacjeGracz2 = "Gracz 2: " + gracz2.pobierzID(); // Dodaj odpowiedni login gracza 2
        int xGracz2 = szerokoscPanelu - g.getFontMetrics().stringWidth(informacjeGracz2) - 10;
        int yGracz2 = 20;
        g.drawString(informacjeGracz2, xGracz2-80, yGracz2);

        // Wywołanie rysowania paska zdrowia
        rysujPasekZdrowia(g);
    }





    // Rysowanie gracza na planszy
    private void rysujGracza(Graphics g, int x, int y) {
        Image ObrazGracza1 = null;
        Image ObrazGracza2 = null;
        int szerokoscPanelu = getWidth();
        int wysokoscPanelu = getHeight();

        int szerokoscGracza = (szerokoscPanelu / szerokoscMapy) - 20;
        int wysokoscGracza = (wysokoscPanelu / wysokoscMapy) - 5;

        int xPos = x * (szerokoscPanelu / szerokoscMapy) + 10;
        int yPos = y * (wysokoscPanelu / wysokoscMapy);

        // Rysuj gracza 1
        if (gracz.pobierzID() == 0) {
            switch (kierunekGracza1) {
                case 0:
                    ObrazGracza1 = gora;
                    break;
                case 1:
                    ObrazGracza1 = dol;
                    break;
                case 2:
                    ObrazGracza1 = lewo;
                    break;
                case 3:
                    ObrazGracza1 = prawo;
                    break;
                default:
                    ObrazGracza1 = dol;
            }
            g.drawImage(ObrazGracza1, gracz.pobierzX() * (szerokoscPanelu / szerokoscMapy) + 10,
                    gracz.pobierzY() * (wysokoscPanelu / wysokoscMapy), szerokoscGracza, wysokoscGracza, this);
        }

        // Rysuj gracza 2
        if (gracz2.pobierzID() == 1) {
            switch (kierunekGracza2) {
                case 0:
                    ObrazGracza2 = gora1;
                    break;
                case 1:
                    ObrazGracza2 = dol1;
                    break;
                case 2:
                    ObrazGracza2 = lewo1;
                    break;
                case 3:
                    ObrazGracza2 = prawo1;
                    break;
                default:
                    ObrazGracza2 = dol1;
            }
            g.drawImage(ObrazGracza2, gracz2.pobierzX() * (szerokoscPanelu / szerokoscMapy) + 10,
                    gracz2.pobierzY() * (wysokoscPanelu / wysokoscMapy), szerokoscGracza, wysokoscGracza, this);
        }
    }


    // Rysowanie bomb na planszy
    private void rysujBomby(Graphics g) {
        // Rysuj bomby dla gracza 1 i gracza 2
        rysujBombyZListy(g, listaBombGracz1);
        rysujBombyZListy(g, listaBombGracz2);
    }

    // Rysowanie bomb z listy
    private void rysujBombyZListy(Graphics g, List<Bomba> listaBomb) {
        // Pobranie szerokości i wysokości kafelka
        int szerokoscKafelka = getWidth() / szerokoscMapy;
        int wysokoscKafelka = getHeight() / wysokoscMapy;

        // Iteracja po liście bomb i rysowanie ich obrazków na planszy
        Iterator<Bomba> iterator = listaBomb.iterator();
        while (iterator.hasNext()) {
            Bomba bomba = iterator.next();
            int xPos = bomba.getX() * szerokoscKafelka;
            int yPos = bomba.getY() * wysokoscKafelka;
            g.drawImage(obrazBomby, xPos, yPos, szerokoscKafelka, wysokoscKafelka, this);
        }
    }



    // Klasa reprezentująca bombę
    private class Bomba {
        private int x;
        private int y;

        public Bomba(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}

// Klasa głównego okna menu gry
public class BombermanMenu extends JFrame  {
    private JPanel obecnyPanel;
    private String loginGracza1;

    // Konstruktor menu gry
    public BombermanMenu() {
        setTitle("Bomberman");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 506);
        setLocationRelativeTo(null);
        // Ustawienia panelu obrazu
        PanelObrazu panelObrazu = new PanelObrazu("bomberman1.jpg");
        setContentPane(panelObrazu);

        // Inicjalizacja panelu przycisków
        JPanel panelPrzyciskow = new JPanel(new BorderLayout());
        panelObrazu.add(panelPrzyciskow);

        // Ustawienia etykiety tytułowej
        JLabel tytulLabel = new JLabel("BOMBERMAN", JLabel.CENTER);
        tytulLabel.setFont(new Font("Verdana", Font.BOLD, 80));
        tytulLabel.setForeground(Color.BLACK);
        panelPrzyciskow.add(tytulLabel, BorderLayout.NORTH);

        // Inicjalizacja panelu logowania
        JPanel panelLogowania = new JPanel();
        panelLogowania.setLayout(new FlowLayout());
        panelLogowania.setOpaque(false);

        // Etykieta loginu
        JLabel labelLoginu = new JLabel("Login: ");
        JTextField poleLoginu = new JTextField(20);

        panelLogowania.add(labelLoginu);
        panelLogowania.add(poleLoginu);

        panelPrzyciskow.add(panelLogowania, BorderLayout.CENTER);

        // Inicjalizacja przycisków i ich obsługa
        JButton przyciskStartu = new JButton("Start");
        przyciskStartu.addActionListener(new ActionListener() {
            @Override
            // Pobranie loginu gracza 1 i przełączenie na panel gry
            public void actionPerformed(ActionEvent e) {
                loginGracza1 = poleLoginu.getText();
                przelaczNaPanelGry(loginGracza1);
            }
        });

        JButton przyciskWyjscia = new JButton("Wyjście");
        przyciskWyjscia.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Zamknięcie aplikacji po naciśnięciu przycisku "Wyjście"
                System.exit(0);
            }
        });
// Inicjalizacja panelu przycisków
        JPanel panelButton = new JPanel();
        panelButton.setLayout(new FlowLayout());
        panelButton.add(przyciskStartu);
        panelButton.add(przyciskWyjscia);
        panelButton.setOpaque(false);

        panelPrzyciskow.add(panelButton, BorderLayout.SOUTH);

        obecnyPanel = panelPrzyciskow;
        addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    powrotDoMenu();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
    }

    // Przełączanie na panel gry
    private void przelaczNaPanelGry(String loginGracza1) {
        // Utworzenie nowego panelu gry z przekazanym loginem gracza 1
        PanelGry panelGry = new PanelGry(loginGracza1);
        // Ustawienie zawartości okna na nowy panel gry
        setContentPane(panelGry);
        obecnyPanel = panelGry;
        // Wymuszenie ponownego walidowania i odrysowywania okna
        revalidate();
        repaint();
        // Ustalenie fokusu na nowym panelu gry
        panelGry.grabFocus();
    }

    // Powrót do menu głównego
    public void powrotDoMenu() {
        // Ustawienie zawartości okna na obecny panel
        setContentPane(obecnyPanel);
        revalidate();
        repaint();
        obecnyPanel.grabFocus();
    }

    // Metoda main uruchamiająca grę
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BombermanMenu menu = new BombermanMenu();
            menu.setVisible(true);
        });
    }
}