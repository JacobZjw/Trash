package socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @author JwZheng
 */
public class SocketUtil {
    //对方的IP以及端口
    public static int oppositePort = 8888;
    public static InetAddress oppositeAddress = null;
    public static DatagramSocket socket = null;


    public static void sendPos(int arrayX, int arrayY) throws IOException {
        String info = arrayX + "," + arrayY;
        byte[] data = info.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, oppositeAddress, oppositePort);
        socket.send(packet);
    }

    public static int[] receive() throws IOException {
        byte[] data = new byte[1024];// 创建字节数组，指定接收的数据包的大小
        DatagramPacket packet = new DatagramPacket(data, data.length);
        socket.receive(packet);
        String info = new String(data, 0, packet.getLength());
        int[] res = new int[2];
        res[0] = Integer.valueOf(info.split(",")[0]);
        res[1] = Integer.valueOf(info.split(",")[1]);
        return res;
    }


    public static void closeResource() {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }
}
