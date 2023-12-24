package server;

import tools.connection.Connector;
import tools.logger.Logger;
import tools.message.Message;
import tools.message.TypeOfMessage;
import tools.settings.SettingsReader;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private final String SERVER_LOG_PATH = "src/main/resources/server";
    private final String SERVER_NAME = "myNetworkChat_server_log";
    private Logger logger;

    public final int PORT = getPort();
    private Map<String, Connector> usersMultiChat;

    public Server() {
        // создаем экземплар логгера для Сервера
        logger = new Logger(SERVER_NAME,SERVER_LOG_PATH);
        // используем потокобезопасную МАПу, так как будем использовать из разных потоков
        usersMultiChat = new ConcurrentHashMap<>();
        try {
            // создаём серверный сокет на порту полученном из настроек
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Сервер запущен!");
            logger.logMessage(new Message(TypeOfMessage.SERVICE_MESSAGE, "Сервер запущен!\n"));
            // Запускаем бесконечный цикл, в котором будем ждать подключений пользователей
            // и отрабатывать эти подключения в отдельных потоках
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandlerThread(socket).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //метод, рассылающий заданное сообщение всем Пользователям из мапы
    protected void sendMessageAllUsers(Message message) {
        logger.logMessage(message);
        for (Map.Entry<String, Connector> user : usersMultiChat.entrySet()) {
            try {
                user.getValue().send(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //метод, рассылающий заданное сообщение всем Пользователям из мапы кроме отправителя
    protected void sendMessageAllUsersExceptSender(Message message, String userName) {
        logger.logMessage(message);
        for (Map.Entry<String, Connector> user : usersMultiChat.entrySet()) {
            try {
                if (!user.getKey().equals(userName)){
                    user.getValue().send(message);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public class ClientHandlerThread extends Thread {
        private Socket socket;
        public ClientHandlerThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("Подключился новый пользователь с сокетом : " + socket.getRemoteSocketAddress());
            try {
                //Создаем коннектор
                Connector connector = new Connector(socket);
                //Запрашиваем Имя пользователя
                String userName = requestNameOfUser(connector);
                //Организуем общение
                messagingBetweenUsers(connector,userName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        //метод который реализует запрос сервера у клиента имени
        private String requestNameOfUser(Connector connector) {
            while (true) {
                try {
                    // запрашиваем у клиента Имя
                    connector.send(new Message(TypeOfMessage.REQUEST_USER_NAME));
                    // получаем ответ от Клиента с выбранным именем
                    Message responseMessage = connector.receive();
                    // Получаем имя
                    String userName = responseMessage.getTextMessage();

                    // Проверка имени на пустоту и наличие такого имени в МАПе с ИНФО о пользователях
                    if (responseMessage.getTypeOfMessage() == TypeOfMessage.USER_NAME &&
                            userName != null &&
                            !userName.isEmpty() &&
                            !usersMultiChat.containsKey(userName)) {
                        //добавляем пользователя в МАПу
                        usersMultiChat.put(userName, connector);
                        //отправляем всем клиентам сообщение о новом пользователе
                        sendMessageAllUsers(new Message(TypeOfMessage.USER_ADDED, userName));
                        return userName;
                    } else connector.send(new Message(TypeOfMessage.NAME_USED));
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        //метод, реализующий общение в чате
        private void messagingBetweenUsers(Connector connector, String userName) {
            while (true) {
                try {
                    //Ожидаем получения сообщения от пользователя
                    Message message = connector.receive();
                    //Если тип полученного сообщения TEXT_MESSAGE, то отправляем его всем пользователям в чате
                    if (message.getTypeOfMessage() == TypeOfMessage.TEXT_MESSAGE){
                        String textMessage = String.format("%s: %s\n", userName, message.getTextMessage());
                        sendMessageAllUsersExceptSender(new Message(TypeOfMessage.TEXT_MESSAGE, textMessage),userName);
                    }
                    //Если тип полученного сообщения DISABLE_USER, то запускаем процедуру закрытия потока
                    // с информированием пользователей об этом
                    if (message.getTypeOfMessage() == TypeOfMessage.DISABLE_USER){
                        // Рассылаем всем сообщение об удалении пользователя
                        sendMessageAllUsers(new Message(TypeOfMessage.REMOVED_USER, userName));
                        // Удаляем пользователя из МАПы
                        usersMultiChat.remove(userName);
                        // Закрываем соединение
                        connector.close();
                        // Закрываем цикл
                        break;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private int getPort() {
        SettingsReader settings = new SettingsReader();
        return settings.getPort();
    }
}
