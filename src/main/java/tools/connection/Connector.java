package tools.connection;

import tools.message.Message;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connector implements Closeable {
    private final Socket socket;
    private final ObjectOutputStream outputMessage;
    private final ObjectInputStream inputMessage;

    public Connector(Socket socket) throws IOException {
        this.socket = socket;
        this.outputMessage = new ObjectOutputStream(socket.getOutputStream());
        this.inputMessage = new ObjectInputStream(socket.getInputStream());
    }

    //Отправка сообщения по сокетному соединению
    public void send(Message message) throws IOException {
        synchronized (this.outputMessage) {
            outputMessage.writeObject(message);
        }
    }

    //Прием сообщения по сокетному соединению
    public Message receive() throws IOException, ClassNotFoundException {
        synchronized (this.inputMessage) {
            Message message = (Message) inputMessage.readObject();
            return message;
        }
    }

    //Зарываем потоки чтения, записи и сокет
    @Override
    public void close() throws IOException {
        inputMessage.close();
        outputMessage.close();
        socket.close();
    }
}
