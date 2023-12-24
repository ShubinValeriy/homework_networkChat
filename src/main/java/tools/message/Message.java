package tools.message;

import java.io.Serializable;

public class Message implements Serializable {
    private TypeOfMessage typeOfMessage; //тип отправляемого сообщения
    private String textMessage; //текст отправляемого сообщения

    public Message(TypeOfMessage typeOfMessage) {
        this.typeOfMessage = typeOfMessage;
        this.textMessage = null;
    }

    public Message(TypeOfMessage typeOfMessage, String textMessage) {
        this.typeOfMessage = typeOfMessage;
        this.textMessage = textMessage;
    }

    public TypeOfMessage getTypeOfMessage() {
        return typeOfMessage;
    }

    public String getTextMessage() {
        return textMessage;
    }
}
