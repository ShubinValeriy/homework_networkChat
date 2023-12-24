import server.Server;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Server server = new Server();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
