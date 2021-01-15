package frame;

import socket.SocketUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

/**
 * @author JwZheng
 */
public class BoardListener implements MouseListener {
    private GoBangFrame gf;


    public BoardListener(GoBangFrame gf) {
        super();
        this.gf = gf;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        //�������
        int mouseX = e.getX();
        int mouseY = e.getY();

        //��Ӧ����������
        int chessX = (mouseX / GoBangFrame.SIZE) * GoBangFrame.SIZE + GoBangFrame.X;
        int chessY = (mouseY / GoBangFrame.SIZE) * GoBangFrame.SIZE + GoBangFrame.Y;

        //��Ӧ�������е�����
        int arrayX = (chessX - 20) / 40;
        int arrayY = (chessY - 20) / 40;

        //�ж��Ƿ��ֵ�
        if (!gf.isMyTurn) {
            if (SocketUtil.socket == null) {
                gf.popMessageDialog("���ȴ����������Ϸ");
            }
            return;
        }
        if (gf.chess[arrayX][arrayY] != 0) {
            JOptionPane.showMessageDialog(null, "�˴���������", "����", JOptionPane.PLAIN_MESSAGE);
            return;
        }
        //����Ϊ����
        int color;
        Graphics graphics = gf.getGraphics();

        if (gf.isRoomOwner) {
            graphics.setColor(Color.BLACK);
            color = 1;
        } else {
            graphics.setColor(Color.WHITE);
            color = 2;
        }
        //������
        graphics.fillOval(chessX - GoBangFrame.SIZE / 2, chessY - GoBangFrame.SIZE / 2, GoBangFrame.SIZE, GoBangFrame.SIZE);
        gf.chess[arrayX][arrayY] = color;
        gf.isMyTurn = !gf.isMyTurn;


        if (isEnd(arrayX, arrayY, color)) {
            try {
                SocketUtil.sendPos(chessX, chessY);
                SocketUtil.sendPos(-2, -2);
                SocketUtil.closeResource();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            gf.popMessageDialog("��Ӯ��");
        } else {
            try {
                SocketUtil.sendPos(chessX, chessY);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public boolean isEnd(int arrayX, int arrayY, int color) {
        int count = 0;
        //��
        for (int i = 0; i < GoBangFrame.COLUMN; i++) {
            if (gf.chess[arrayX][i] == color) {
                ++count;
            } else {
                count = 0;
            }
            if (count == 5) {
                return true;
            }
        }

        //��
        count = 0;
        for (int i = 0; i < GoBangFrame.ROW; i++) {
            if (gf.chess[i][arrayY] == color) {
                ++count;
            } else {
                count = 0;
            }
            if (count == 5) {
                return true;
            }
        }

        count = 0;
        for (int i = -4; i <= 4; i++) {
            if ((arrayX + i >= 0) && (arrayY + i >= 0) && (arrayX + i <= 14) && (arrayY + i <= 14)) {
                if (gf.chess[arrayX + i][arrayY + i] == 1) {
                    count++;
                } else {
                    count = 0;
                }
                if (count == 5) {
                    return true;
                }
            }
        }

        count = 0;
        for (int i = -4; i <= 4; i++) {
            if ((arrayX + i >= 0) && (arrayY - i >= 0) && (arrayX + i <= 14) && (arrayY - i <= 14)) {
                if (gf.chess[arrayX + i][arrayY - i] == 1) {
                    count++;
                } else {
                    count = 0;
                }
                if (count == 5) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

}
