package org.ptit.b22cn539.Handler;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.ptit.b22cn539.DTO.UserResponse;

import io.socket.client.IO;
import io.socket.client.Socket;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class ClientHandler {
    Socket socket;
    String token;

    @NonFinal
    List<UserResponse> userResponses;
    public ClientHandler(String token) throws IOException {
        this.token = token;
        URI uri = URI.create("http://10.109.180.251:9092?token=" + token.strip());
        this.socket = IO.socket(uri);
        this.socket.connect();
        socket.on(Socket.EVENT_DISCONNECT, objects -> System.out.println("Disconnected"));
    }
}
