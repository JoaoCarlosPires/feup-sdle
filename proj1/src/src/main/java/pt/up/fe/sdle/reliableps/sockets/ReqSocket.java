package pt.up.fe.sdle.reliableps.sockets;

import pt.up.fe.sdle.reliableps.messages.RequestMessage;
import org.zeromq.SocketType;
import org.zeromq.ZContext;

public class ReqSocket extends ZSocket {

    public ReqSocket(ZContext context, String port) {
        super(context.createSocket(SocketType.REQ));
        socket.connect(port);
    }

    public void sendMessage(String Content) {
        socket.send(Content);
    }

    public void sendMessage(RequestMessage message) {
        socket.send(message.getContent());
    }

    public RequestMessage receiveMessage() {
        return new RequestMessage(socket.recvStr());
    }
}
