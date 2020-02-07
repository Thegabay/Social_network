package bgu.spl.net.api.bidi;

import java.io.IOException;

import bgu.spl.net.srv.bidi.ConnectionHandler;

public interface Connections<T> {

    boolean send(int connectionId, T msg);

    void broadcast(T msg);

    void disconnect(int connectionId);
    
}
