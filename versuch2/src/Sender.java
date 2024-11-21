import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Die "Klasse" Sender liest einen String von der Konsole und zerlegt ihn in einzelne Worte. Jedes Wort wird in ein
 * einzelnes {@link Packet} verpackt und an das Medium verschickt. Erst nach dem Erhalt eines entsprechenden
 * ACKs wird das nächste {@link Packet} verschickt. Erhält der Sender nach einem Timeout von einer Sekunde kein ACK,
 * überträgt er das {@link Packet} erneut.
 */
public class Sender {
    /**
     * Hauptmethode, erzeugt Instanz des {@link Sender} und führt {@link #send()} aus.
     *
     * @param args Argumente, werden nicht verwendet.
     */
    public static void main(String[] args) {
        Sender sender = new Sender();
        try {
            sender.send();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Erzeugt neuen Socket. Liest Text von Konsole ein und zerlegt diesen. Packt einzelne Worte in {@link Packet}
     * und schickt diese an Medium. Nutzt {@link SocketTimeoutException}, um eine Sekunde auf ACK zu
     * warten und das {@link Packet} ggf. nochmals zu versenden.
     *
     * @throws IOException Wird geworfen falls Sockets nicht erzeugt werden können.
     */
    private void send() throws IOException {
        //Text einlesen und in Worte zerlegen
        Scanner scanner = new Scanner(System.in);

        System.out.println("Please enter a message to send:");

        String message = scanner.nextLine();

        String[] mParts = message.split(" ");
        String[] messageParts = Arrays.copyOf(mParts, mParts.length + 1);
        messageParts[messageParts.length - 1] = "EOT";

        // Socket erzeugen auf Port 9998 und Timeout auf eine Sekunde setzen
        var clientSocket = new DatagramSocket(9998);
        clientSocket.setSoTimeout(1000);

        int counter = 0;
        int seq = 0;

        // Iteration über den Konsolentext
        while (true) {
            String word = messageParts[counter];

            // Paket an Port 9997 senden
            var packetOut = new Packet(seq, 0, false, word.getBytes());

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(b);
            o.writeObject(packetOut);
            byte[] buf = b.toByteArray();

            var datagramPacket = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(), 9997);
            clientSocket.send(datagramPacket);

            var expectedAckNum = seq + packetOut.getPayload().length;

            try {
                // Auf ACK warten und erst dann Schleifenzähler inkrementieren
                byte[] recBuf = new byte[256];
                var responsePacketRaw = new DatagramPacket(recBuf, recBuf.length);
                clientSocket.receive(responsePacketRaw);

                // deserialize Packet
                ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(responsePacketRaw.getData()));
                Packet packetIn = (Packet) is.readObject();

                if (packetIn.getAckNum() != expectedAckNum) {
                    System.out.println("ACK num missmatch");
                    continue;
                }

                seq = packetIn.getAckNum();
                counter++;

                if (counter >= messageParts.length) {
                    break;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SocketTimeoutException e) {
                System.out.println("Receive timed out, retrying...");
            }
        }

        // Wenn alle Packete versendet und von der Gegenseite bestätigt sind, Programm beenden
        clientSocket.close();

        if (System.getProperty("os.name").equals("Linux")) {
            clientSocket.disconnect();
        }

        System.exit(0);
    }
}