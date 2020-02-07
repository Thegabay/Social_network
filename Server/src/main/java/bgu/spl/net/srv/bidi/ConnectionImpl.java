package bgu.spl.net.srv.bidi;

import bgu.spl.net.api.bidi.Connections;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionImpl <T> implements Connections <T> {
    private ConcurrentHashMap<Integer, ConnectionHandler> connHash;

    public ConnectionImpl(){

        this.connHash = new ConcurrentHashMap<>();
    }
    @Override
    public boolean send(int connectionId, T msg) {
        if(connHash.containsKey(connectionId)) {
            connHash.get(connectionId).send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void broadcast(T msg) {     //TODO - Check what to do with non-log in client
        for (Integer c:connHash.keySet()) {
            connHash.get(c).send(msg);
        }
    }

    @Override
    public void disconnect(int connectionId) {
        if(connHash.containsKey(connectionId))
            connHash.remove(connectionId);
    }
    public void putHandlerconnHash(int id,ConnectionHandler<T> CH){
  //      System.out.println("here5");
         connHash.put(id,CH);
    }
}
