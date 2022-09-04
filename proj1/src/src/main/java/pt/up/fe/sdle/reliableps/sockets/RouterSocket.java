package pt.up.fe.sdle.reliableps.sockets;

import pt.up.fe.sdle.reliableps.messages.RouterMessage;
import org.zeromq.SocketType;
import org.zeromq.ZContext;

public class RouterSocket extends ZSocket {
    public RouterSocket(ZContext context, String port) {
        super(context.createSocket(SocketType.ROUTER));
        socket.bind(port);
    }

    public void sendMessage(String to, String Content) {
        socket.sendMore(to);
        socket.sendMore("");
        socket.send(Content);
    }

    public void sendMessage(RouterMessage message) {
        socket.sendMore(message.getTarget());
        socket.sendMore("");
        socket.send(message.getContent());
    }

    public RouterMessage receiveMessage() {
        byte[] destination = socket.recv();
        socket.recv();
        String message = socket.recvStr();

        return new RouterMessage(destination, message);
    }

    public void close() {
        socket.close();
    }
}
