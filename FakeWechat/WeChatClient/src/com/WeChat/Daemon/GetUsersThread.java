/**
 * @Title: GetUsersThread.java
 * @Package: com.WeChat.Daemon
 * @author: JwZheng
 * @date: 2020��11��12�� ����11:00:01
 * @Description: 
 */

package com.WeChat.Daemon;

import java.io.IOException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import com.WeChat.app.DataBuffer;

/**
 * @ClassName: GetUsersThread
 * @author JwZheng
 * @date: 2020��11��12�� ����11:00:01
 * @Description:
 */
public class GetUsersThread extends Thread {

	private MessageBox messageBox;

	public GetUsersThread(MessageBox messageBox) {
		this.messageBox = messageBox;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (!Thread.currentThread().isInterrupted() && DataBuffer.socket != null) {
			try {
				DataBuffer.oos.writeObject(DataBuffer.requestBuilder.getUsersRequest());
				DataBuffer.oos.flush();

				Thread.sleep(5 * 1000L);

			} catch (InterruptedException e) {
				System.out.println("��ʱ�����ж� !");
			} catch (IOException e) {
				// TODO:�����������ʧ��
				DataBuffer.socket = null;
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						messageBox.setMessage("�����ѶϿ������������� !");
						messageBox.open();
					}
				});
				break;
			}
		}

	}

}
