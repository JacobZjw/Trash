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
        //初始化一个界面,并设置标题大小等属性
        JFrame jf = new JFrame();
        jf.setTitle("五子棋");
        jf.setSize(800, 650);
        jf.setLocationRelativeTo(null);
        jf.setDefaultCloseOperation(3);

        //设置顶级容器JFrame为框架布局
        jf.setLayout(new BorderLayout());
        //设置右半部分的大小
        Dimension panelDim = new Dimension(150, 0);
        //设置左半部分的大小
        Dimension boardDim = new Dimension(550, 0);
        //设置右边按钮组件的大小
        Dimension buttonDim = new Dimension(140, 40);

        this.setPreferredSize(boardDim);
        this.setBackground(Color.LIGHT_GRAY);
        jf.add(this, BorderLayout.CENTER);

        //实现右边的JPanel容器界面
        JPanel jp = new JPanel();
        jp.setPreferredSize(panelDim);
        jp.setBackground(Color.white);
        jf.add(jp, BorderLayout.EAST);
        jp.setLayout(new FlowLayout());

        JLabel addressLabel = new JLabel("请输入房主IP地址：");

        JTextField addressField = new JTextField();
        addressField.setPreferredSize(new Dimension(100, 30));
        jp.add(addressLabel);
        jp.add(addressField);

        JLabel portLabel = new JLabel("请输入房主端口：");
        JTextField portField = new JTextField();
        portField.setPreferredSize(new Dimension(100, 30));
        jp.add(portLabel);
        jp.add(portField);

        JButton addToGameButton = new JButton("加入游戏");
        JButton CreateGameButton = new JButton("创建游戏");
        JButton exitGameButton = new JButton("退出游戏");

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
                    popMessageDialog("请输入服务器IP和端口");
                    return;
                }
                try {
                    if (SocketUtil.socket == null) {
                        SocketUtil.socket = new DatagramSocket();
                        SocketUtil.socket.setSoTimeout(20000);
                        chess = new int[COLUMN][ROW];
                        paint(graphics);
                    } else {
                        popMessageDialog("已经在游戏中");
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
                        popMessageDialog("成功加入:" + SocketUtil.oppositeAddress + ":" + SocketUtil.oppositePort);
                        createListenerThread();
                    } else {
                        popMessageDialog("加入:" + SocketUtil.oppositeAddress + ":" + SocketUtil.oppositePort + "失败");
                    }
                } catch (SocketTimeoutException socketTimeoutException) {
                    SocketUtil.closeResource();
                    popMessageDialog("超时，请重新创建或加入游戏");
                    return;
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        CreateGameButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                byte[] data = new byte[1024];// 创建字节数组，指定接收的数据包的大小
                DatagramPacket packet = new DatagramPacket(data, data.length);
                try {
                    if (SocketUtil.socket == null) {
                        SocketUtil.socket = new DatagramSocket(8888);
                        SocketUtil.socket.setSoTimeout(20000);
                        chess = new int[COLUMN][ROW];
                        paint(graphics);
                    } else {
                        popMessageDialog("已经在游戏中");
                        return;
                    }
                    popMessageDialog("IP地址：" + InetAddress.getLocalHost().getHostAddress() + "\n端口：" + SocketUtil.socket.getLocalPort() + "\n等待客户端连接");
                    SocketUtil.socket.receive(packet);

                } catch (SocketTimeoutException socketTimeoutException) {
                    SocketUtil.closeResource();
                    popMessageDialog("超时，请重新创建或加入游戏");
                    return;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                SocketUtil.oppositeAddress = packet.getAddress();
                SocketUtil.oppositePort = packet.getPort();

                //向客户端反馈
                try {
                    SocketUtil.sendPos(-1, -1);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                isRoomOwner = true;
                isMyTurn = true;
                popMessageDialog(SocketUtil.oppositeAddress + ":" + SocketUtil.oppositePort + "加入游戏");

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
                        popMessageDialog("你输了");
                        SocketUtil.closeResource();
                        return;
                    }

                    //对应在数组中的坐标
                    int arrayX = (chessX - 20) / 40;
                    int arrayY = (chessY - 20) / 40;
                    int color;
                    //画棋子
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
                    popMessageDialog("超时，请重新创建或加入游戏");
                    return;
                } catch (Exception exception) {
                    System.out.println("Socket close");
                }
            }
        }).start();
    }

    @Override
    public void paint(Graphics g) {
        //画出白框
        super.paint(g);
        //重绘出棋盘
        g.setColor(Color.black);
        for (int i = 0; i < ROW; i++) {
            g.drawLine(X, Y + SIZE * i, X + SIZE * (COLUMN - 1), Y + SIZE * i);
        }
        for (int j = 0; j < COLUMN; j++) {
            g.drawLine(X + SIZE * j, Y, X + SIZE * j, Y + SIZE * (ROW - 1));
        }

        //重绘出棋子
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                if (chess[i][j] == 1) {
                    //黑棋为 1
                    int countX = SIZE * j + 20;
                    int countY = SIZE * i + 20;
                    g.setColor(Color.black);
                    g.fillOval(countX - SIZE / 2, countY - SIZE / 2, SIZE, SIZE);
                } else if (chess[i][j] == 2) {
                    //白棋为 2
                    int countX = SIZE * j + 20;
                    int countY = SIZE * i + 20;
                    g.setColor(Color.white);
                    g.fillOval(countX - SIZE / 2, countY - SIZE / 2, SIZE, SIZE);
                }
            }
        }
    }

}
