package frame;

import socket.SocketUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

/**
 * @author JwZheng
 */
public class GoBangFrame extends JPanel {
    public final static int X = 20;
    public final static int Y = 20;
    public final static int SIZE = 40;
    public final static int ROW = 15;
    public final static int COLUMN = 15;
    public boolean isRoomOwner;
    public boolean isMyTurn;
    public int[][] chess;

    public Graphics graphics;


    public GoBangFrame() {
        initUI();
        chess = new int[COLUMN][ROW];
        this.isMyTurn = false;
        this.graphics = this.getGraphics();
    }

    public static void main(String[] args) {
        GoBangFrame frame = new GoBangFrame();
    }

    public void popMessageDialog(String result) {
        JOptionPane.showMessageDialog(null, result, "", JOptionPane.PLAIN_MESSAGE);
    }

    public void initUI() {
        //��ʼ��һ������,�����ñ����С������
        JFrame jf = new JFrame();
        jf.setTitle("������");
        jf.setSize(800, 650);
        jf.setLocationRelativeTo(null);
        jf.setDefaultCloseOperation(3);

        //���ö�������JFrameΪ��ܲ���
        jf.setLayout(new BorderLayout());
        //�����Ұ벿�ֵĴ�С
        Dimension panelDim = new Dimension(150, 0);
        //������벿�ֵĴ�С
        Dimension boardDim = new Dimension(550, 0);
        //�����ұ߰�ť����Ĵ�С
        Dimension buttonDim = new Dimension(140, 40);

        this.setPreferredSize(boardDim);
        this.setBackground(Color.LIGHT_GRAY);
        jf.add(this, BorderLayout.CENTER);

        //ʵ���ұߵ�JPanel��������
        JPanel jp = new JPanel();
        jp.setPreferredSize(panelDim);
        jp.setBackground(Color.white);
        jf.add(jp, BorderLayout.EAST);
        jp.setLayout(new FlowLayout());

        JLabel addressLabel = new JLabel("�����뷿��IP��ַ��");

        JTextField addressField = new JTextField();
        addressField.setPreferredSize(new Dimension(100, 30));
        jp.add(addressLabel);
        jp.add(addressField);

        JLabel portLabel = new JLabel("�����뷿���˿ڣ�");
        JTextField portField = new JTextField();
        portField.setPreferredSize(new Dimension(100, 30));
        jp.add(portLabel);
        jp.add(portField);

        JButton addToGameButton = new JButton("������Ϸ");
        JButton CreateGameButton = new JButton("������Ϸ");
        JButton exitGameButton = new JButton("�˳���Ϸ");

        addToGameButton.setPreferredSize(buttonDim);
        CreateGameButton.setPreferredSize(buttonDim);
        exitGameButton.setPreferredSize(buttonDim);

        jp.add(addToGameButton);
        jp.add(CreateGameButton);
        jp.add(exitGameButton);

        addToGameButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String address = addressField.getText();
                String port = portField.getText();
                if ("".equals(address) || "".equals(port)) {
                    popMessageDialog("�����������IP�Ͷ˿�");
                    return;
                }
                try {
                    if (SocketUtil.socket == null) {
                        SocketUtil.socket = new DatagramSocket();
                        SocketUtil.socket.setSoTimeout(20000);
                        chess = new int[COLUMN][ROW];
                        paint(graphics);
                    } else {
                        popMessageDialog("�Ѿ�����Ϸ��");
                        return;
                    }
                    SocketUtil.oppositePort = Integer.parseInt(port);
                    SocketUtil.oppositeAddress = InetAddress.getByName(address);
                    SocketUtil.sendPos(0, 0);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                try {
                    int[] rev = SocketUtil.receive();
                    if (rev[0] == -1 && rev[1] == -1) {
                        isRoomOwner = false;
                        popMessageDialog("�ɹ�����:" + SocketUtil.oppositeAddress + ":" + SocketUtil.oppositePort);
                        createListenerThread();
                    } else {
                        popMessageDialog("����:" + SocketUtil.oppositeAddress + ":" + SocketUtil.oppositePort + "ʧ��");
                    }
                } catch (SocketTimeoutException socketTimeoutException) {
                    SocketUtil.closeResource();
                    popMessageDialog("��ʱ�������´����������Ϸ");
                    return;
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        CreateGameButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                byte[] data = new byte[1024];// �����ֽ����飬ָ�����յ����ݰ��Ĵ�С
                DatagramPacket packet = new DatagramPacket(data, data.length);
                try {
                    if (SocketUtil.socket == null) {
                        SocketUtil.socket = new DatagramSocket(8888);
                        SocketUtil.socket.setSoTimeout(20000);
                        chess = new int[COLUMN][ROW];
                        paint(graphics);
                    } else {
                        popMessageDialog("�Ѿ�����Ϸ��");
                        return;
                    }
                    popMessageDialog("IP��ַ��" + InetAddress.getLocalHost().getHostAddress() + "\n�˿ڣ�" + SocketUtil.socket.getLocalPort() + "\n�ȴ��ͻ�������");
                    SocketUtil.socket.receive(packet);

                } catch (SocketTimeoutException socketTimeoutException) {
                    SocketUtil.closeResource();
                    popMessageDialog("��ʱ�������´����������Ϸ");
                    return;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                SocketUtil.oppositeAddress = packet.getAddress();
                SocketUtil.oppositePort = packet.getPort();

                //��ͻ��˷���
                try {
                    SocketUtil.sendPos(-1, -1);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                isRoomOwner = true;
                isMyTurn = true;
                popMessageDialog(SocketUtil.oppositeAddress + ":" + SocketUtil.oppositePort + "������Ϸ");

                createListenerThread();
            }
        });

        exitGameButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SocketUtil.closeResource();
            }
        });


        this.addMouseListener(new BoardListener(this));

        jf.setVisible(true);

    }

    public void createListenerThread() {
        new Thread(() -> {
            while (SocketUtil.socket != null) {
                try {
                    int[] rev1 = SocketUtil.receive();
                    int chessX = rev1[0];
                    int chessY = rev1[1];

                    if (chessX == -2 && chessY == -2) {
                        popMessageDialog("������");
                        SocketUtil.closeResource();
                        return;
                    }

                    //��Ӧ�������е�����
                    int arrayX = (chessX - 20) / 40;
                    int arrayY = (chessY - 20) / 40;
                    int color;
                    //������
                    if (isRoomOwner) {
                        graphics.setColor(Color.WHITE);
                        color = 2;
                    } else {
                        graphics.setColor(Color.BLACK);
                        color = 1;
                    }
                    graphics.fillOval(chessX - GoBangFrame.SIZE / 2, chessY - GoBangFrame.SIZE / 2, GoBangFrame.SIZE, GoBangFrame.SIZE);
                    chess[arrayX][arrayY] = color;
                    isMyTurn = !isMyTurn;
                } catch (SocketTimeoutException socketTimeoutException) {
                    SocketUtil.closeResource();
                    popMessageDialog("��ʱ�������´����������Ϸ");
                    return;
                } catch (Exception exception) {
                    System.out.println("Socket close");
                }
            }
        }).start();
    }

    @Override
    public void paint(Graphics g) {
        //�����׿�
        super.paint(g);
        //�ػ������
        g.setColor(Color.black);
        for (int i = 0; i < ROW; i++) {
            g.drawLine(X, Y + SIZE * i, X + SIZE * (COLUMN - 1), Y + SIZE * i);
        }
        for (int j = 0; j < COLUMN; j++) {
            g.drawLine(X + SIZE * j, Y, X + SIZE * j, Y + SIZE * (ROW - 1));
        }

        //�ػ������
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                if (chess[i][j] == 1) {
                    //����Ϊ 1
                    int countX = SIZE * j + 20;
                    int countY = SIZE * i + 20;
                    g.setColor(Color.black);
                    g.fillOval(countX - SIZE / 2, countY - SIZE / 2, SIZE, SIZE);
                } else if (chess[i][j] == 2) {
                    //����Ϊ 2
                    int countX = SIZE * j + 20;
                    int countY = SIZE * i + 20;
                    g.setColor(Color.white);
                    g.fillOval(countX - SIZE / 2, countY - SIZE / 2, SIZE, SIZE);
                }
            }
        }
    }

}
