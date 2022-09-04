package pt.up.fe.sdle.reliableps.sockets;

import org.zeromq.ZMQ.Socket;

public abstract class ZSocket {
    protected final Socket socket;

    ZSocket(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }
}
