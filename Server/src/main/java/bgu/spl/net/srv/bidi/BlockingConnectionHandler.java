package bgu.spl.net.srv.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements ConnectionHandler<T>, Runnable, java.io.Closeable {

    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private Connections<T> conn;
    private int id;
    private volatile boolean connected = true;

    public BlockingConnectionHandler(Socket sock,
    		MessageEncoderDecoder<T> reader,
    		BidiMessagingProtocol<T> protocol,
    		Connections<T> conn,
    		int id) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.id = id;
        this.conn = conn;
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { 
            int read;
        	protocol.start(id,conn);

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process(nextMessage);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }


    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }
    @Override
    public void send(T msg) {
  //      System.out.println("here");
        if (msg != null) {
            try {
                out.write(encdec.encode(msg));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out.flush();
 //               System.out.println("flush");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
