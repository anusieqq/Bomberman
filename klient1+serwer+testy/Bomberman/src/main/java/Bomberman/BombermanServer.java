// Pakiet, w którym znajduje się klasa
package Bomberman;

// Importowanie klas do obsługi wyjątków, wejścia/wyjścia, gniazd sieciowych, list i wątków
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Klasa reprezentująca serwer gry Bomberman
public class BombermanServer {

    // Ustawienia serwera
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private ServerSocket serverSocket;
    private boolean akceptowanieKlientow = true;
    private List<ClientHandler> klienci = new ArrayList<>();

    // Konstruktor serwera
    public BombermanServer(int port) {
        try {
            // Tworzenie gniazda serwera na określonym porcie
            serverSocket = new ServerSocket(port);
            System.out.println("Serwer działa na porcie " + port);

            // Akceptowanie klientów do momentu, gdy dołączy dwóch graczy
            while (akceptowanieKlientow) {
                Socket gniazdoKlienta = serverSocket.accept();
                System.out.println("Nowy klient podłączony: " + gniazdoKlienta);

                // Sprawdzanie, czy serwer nie został zamknięty
                if (executorService.isShutdown()) {
                    gniazdoKlienta.close();
                    continue;
                }

                // Tworzenie obsługi klienta i dodawanie go do puli
                ClientHandler obslugaKlienta = new ClientHandler(gniazdoKlienta);
                klienci.add(obslugaKlienta);
                executorService.execute(obslugaKlienta);

                // Zatrzymywanie akceptowania klientów, gdy dołączą dwaj gracze
                if (klienci.size() == 2) {
                    akceptowanieKlientow = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Klasa reprezentująca zdarzenia w grze
    protected static class BombermanEvent implements java.io.Serializable {
        public EventType eventType;
        public  int playerId; // Identyfikator gracza
        public  int x; // Współrzędna X
        public  int y; // Współrzędna Y

        // Konstruktor zdarzenia
        public BombermanEvent(EventType eventType, int playerId, int x, int y) {
            this.eventType = eventType;
            this.playerId = playerId;
            this.x = x;
            this.y = y;
        }

        // Metody dostępowe do pól zdarzenia
        public EventType getEventType() {
            return eventType;
        }

        public int getPlayerId() {
            return playerId;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZycie(){

            return x;
        }
        public void setPlayerId(int playerId) {
            this.playerId = playerId;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

    }

    // Enumeracja określająca różne typy zdarzeń w grze
    public enum EventType {
        EKSPLOZJA,
        RUCH,
        BOMBA,
        ZYCIE,
        ZNISZCZENIE,
        RESPAWN,


        EKSPLOZJA1,
        RUCH1,
        BOMBA1,
        ZYCIE1,
        ZNISZCZENIE1,
        RESPAWN1,


    }

    // Klasa obsługująca pojedynczego klienta (wątek obsługi klienta)
    private class ClientHandler implements Runnable {
        private Socket gniazdoKlienta;
        private ObjectInputStream wejscie;
        private ObjectOutputStream wyjscie;
        private int playerId; // Identyfikator gracza

        // Konstruktor obsługi klienta
        public ClientHandler(Socket gniazdoKlienta) {
            this.gniazdoKlienta = gniazdoKlienta;
            try {
                // Tworzenie strumieni do komunikacji z klientem
                wyjscie = new ObjectOutputStream(gniazdoKlienta.getOutputStream());
                wejscie = new ObjectInputStream(gniazdoKlienta.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Logika obsługi klienta w oddzielnym wątku
        @Override
        public void run() {
            try {
                // Przypisywanie identyfikatora gracza
                playerId = klienci.indexOf(this);

                // Odczytywanie obiektów od klienta i rozgłaszanie ich do innych klientów
                while (!executorService.isShutdown()) {
                    Object odczytanyObiekt = wejscie.readObject();
                    System.out.println("Odebrano zdarzenie od klienta " + playerId);
                    if (odczytanyObiekt instanceof BombermanEvent) {
                        BombermanEvent odebraneZdarzenie = (BombermanEvent) odczytanyObiekt;

                        // Rozgłaszanie zdarzeń do innych klientów
                        rozglosZdarzenie(odebraneZdarzenie, this, odebraneZdarzenie.getX(), odebraneZdarzenie.getY());

                        System.out.println("Wysłano zdarzenie: " + odebraneZdarzenie.getEventType()+ " x:"+ odebraneZdarzenie.getX()+" y:" + odebraneZdarzenie.getY());

                    } else {
                        // Obsługa błędu, jeśli otrzymany obiekt nie jest zdarzeniem BombermanEvent
                        System.err.println("Otrzymano obiekt niebędący instancją BombermanEvent");
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        // Rozgłaszanie zdarzenia do innych klientów
        private void rozglosZdarzenie(BombermanEvent zdarzenie, ClientHandler nadawca,int x,int y) {
            for (ClientHandler klient : klienci) {
                if (klient != nadawca) {
                    klient.wyslijZdarzenie(zdarzenie);
                }
            }
        }


        // Wysyłanie zdarzenia do klienta
        private void wyslijZdarzenie(BombermanEvent zdarzenie) {
            try {
                wyjscie.writeObject(zdarzenie);
                wyjscie.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Metoda main uruchamiająca serwer na określonym porcie
    public static void main(String[] args) {
        int port = 8080;
        new BombermanServer(port);
    }
}
