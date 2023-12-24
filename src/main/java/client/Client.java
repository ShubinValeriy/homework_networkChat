package client;

import tools.connection.Connector;
import tools.logger.Logger;
import tools.message.Message;
import tools.message.TypeOfMessage;
import tools.settings.FullAdress;
import tools.settings.SettingsReader;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private final String CLIENT_LOG_PATH = "src/main/resources/clients";
    private final String CLIENT_NAME;
    private Logger logger;
    public final FullAdress FULL_ADDRESS = getFullAdress(); // получим порт и адрес для соеденения с сервером через файл настроек
    private volatile boolean isConnect = false; //флаг отображающий состояние подключения клиента к серверу
    private final String STOP_WORD = "/exit";

//    public boolean isConnect() {
//        return isConnect;
//    }
//
//    public void setConnect(boolean connect) {
//        isConnect = connect;
//    }

    public Client(String clientName) {
        this.CLIENT_NAME = clientName;
        //создаем объект логгера
        logger = new Logger(CLIENT_NAME, CLIENT_LOG_PATH);
        //создаем сокет и объект connector
        try {
            Socket socket = new Socket(FULL_ADDRESS.getAddress(), FULL_ADDRESS.getPort());
            Connector connector = new Connector(socket);
            System.out.println("Вы подключились к серверу");
            logger.logMessage(new Message(TypeOfMessage.SERVICE_MESSAGE, "Подключение к серверу\n"));
            isConnect = true;
            String userName = chooseUserName(connector);
            logger.logMessage(new Message(TypeOfMessage.SERVICE_MESSAGE,
                    String.format("Зарегистрированы в чате с именем " + userName +"\n")));
            // запускаем отдельный поток для вывода прослушивания сервера и вывода сообщений на экран
            new ListenerThread(connector).start();
            while (isConnect){
                Scanner textScanner = new Scanner(System.in);
                String userText = textScanner.nextLine();
                //проверка не ввел ли Пользователь оператор остановки
                if (userText.equals(STOP_WORD)){
                    //отправляем на сервер информацию об отключении
                    connector.send(new Message(TypeOfMessage.DISABLE_USER));
                    logger.logMessage(new Message(TypeOfMessage.SERVICE_MESSAGE,
                            "Остановка клиента и выход из чата\n"));
                    // переходим в отключенный режим
                    isConnect = false;

                }else {
                    connector.send(new Message(TypeOfMessage.TEXT_MESSAGE, userText));
                    logger.logMessage(new Message(TypeOfMessage.TEXT_MESSAGE,
                            String.format("Вы : " + userText + "\n")));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    //метод, реализующий Выбор имени для участия в чате
    protected String chooseUserName(Connector connector) {
        String userName = "";
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                //Принимаем от сервера сообщение
                Message message = connector.receive();
                //Проверяем если сообщение от сервера на запрос Имени, то просим пользователя ввести имя
                if (message.getTypeOfMessage() == TypeOfMessage.REQUEST_USER_NAME) {
                    System.out.print("Укажите имя пользователя: ");
                    userName = scanner.nextLine();
                    connector.send(new Message(TypeOfMessage.USER_NAME, userName));
                }
                //если сообщение сервера говорит, что имя уже используется, повторяем запрос ввода имени
                if (message.getTypeOfMessage() == TypeOfMessage.NAME_USED) {
                    System.out.println("Данное имя уже используется! необходимо выбрать другое!");
                }
                //если имя принято, выходим из цикла
                if (message.getTypeOfMessage() == TypeOfMessage.USER_ADDED && userName.equals(message.getTextMessage())) {
                    System.out.println("Ваше имя принято! Добро пожаловать в чат\n" +
                            "Для выхода из чата отправьте /exit\n");
                    break;
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return userName;
    }

    private class ListenerThread extends Thread {
        private Connector connector;
        public ListenerThread (Connector connector){
            this.connector = connector;
        }

        @Override
        public void run() {
            while (isConnect) {
                try {
                    //Принимаем от сервера сообщение
                    Message message = connector.receive();
                    //если тип TEXT_MESSAGE, то выводим полученный текст
                    if (message.getTypeOfMessage() == TypeOfMessage.TEXT_MESSAGE) {
                        System.out.println(message.getTextMessage());
                    }
                    //если сообщение с типом USER_ADDED выводим сообщение о новом пользователе
                    if (message.getTypeOfMessage() == TypeOfMessage.USER_ADDED) {
                        System.out.println(String.format("Пользователь %s присоединился к чату.\n",
                                message.getTextMessage()));
                    }
                    //если сообщение с типом REMOVED_USER выводим сообщение об отключении пользователя
                    if (message.getTypeOfMessage() == TypeOfMessage.REMOVED_USER) {
                        System.out.println(String.format("Пользователь %s покинул чат.\n",
                                message.getTextMessage()));
                    }
                    logger.logMessage(message);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            //закрываем соединение
            try {
                connector.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private FullAdress getFullAdress() {
        SettingsReader settings = new SettingsReader();
        return settings.getFullAddress();
    }

}
