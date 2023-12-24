package tools.settings;

public class FullAdress {
    private int port;
    private String address;

    public FullAdress(int port, String address) {
        this.port = port;
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }
}
