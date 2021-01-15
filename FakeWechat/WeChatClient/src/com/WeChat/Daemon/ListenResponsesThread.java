/**
 * @Title: Receive.java
 * @Package: com.Wechat.messages
 * @author: JwZheng
 * @date: 2020�?11�?2�? 下午3:54:09
 * @Description: 
 */

package com.WeChat.Daemon;

import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.List;

import com.WeChat.DataType.Message;
import com.WeChat.DataType.Response;
import com.WeChat.DataType.ResponseType;
import com.WeChat.DataType.User;
import com.WeChat.app.DataBuffer;

/**
 * @ClassName: Receive
 * @author JwZheng
 * @date: 2020�?11�?2�? 下午3:54:09
 * @Description:
 */
public class ListenResponsesThread extends Thread {

	Display display = Display.getDefault();

	private String str;
	private Text messageText;
	private List userList;
	private Date data;
	private Message msgObj;

	public ListenResponsesThread(Text messageText, List usrList) {
		this.messageText = messageText;
		this.userList = usrList;
	}

	@SuppressWarnings("unchecked")
	public void run() {
		System.out.println("Start receive messages !");

		while (!currentThread().isInterrupted()) {
			try {
				Response response = (Response) DataBuffer.ois.readObject();
				ResponseType type = response.getType();

				if (!type.equals(ResponseType.UPDATE_USERS)) {
					msgObj = (Message) response.getObject();
					data = msgObj.getSendTime();
				}

				switch (type) {
				case UPDATE_USERS:
					DataBuffer.onlineUsersList = (CopyOnWriteArrayList<User>) response.getObject();
					updateUserList();
					break;
				case CHAT:
					if (msgObj.getToUser() == null)
						str = String.format("【%s】说:%s", msgObj.getFromUser().getUserName(), msgObj.getMessage());
					else {
						str = String.format("【%s】对【%s】说:%s", msgObj.getFromUser().getUserName(),
								msgObj.getToUser().getUserName(), msgObj.getMessage());
					}
					appendMessage(str);
					break;
				case USER_LOGIN:
					str = String.format("%60s已上线", msgObj.getToUser().getUserName());
					appendMessage(str);
					break;
				case USER_LOGOUT:
					str = String.format("%60s已下线", msgObj.getToUser().getUserName());
					appendMessage(str);
				case OK:
					break;
				case LOGIN_REFUSED:
					str = String.format("%60s", msgObj.getMessage());
					// TODO:这里显示用户名被占用，然后关闭连接
					DataBuffer.socket.close();
					DataBuffer.socket = null;
					return;
				case SERVER_CLOSE:
					DataBuffer.socket.close();
					DataBuffer.socket = null;
					return;
				default:
					str = null;
					break;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		DataBuffer.getUsersThread.interrupt();
	}

	public void updateUserList() {
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				userList.removeAll();
				for (User user : DataBuffer.onlineUsersList) {
					userList.add(user.getUserName());
				}
			}
		});
	}

	public void appendMessage(String str) {
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				String TimeString = DataBuffer.time.format(data);
				messageText.append(TimeString);
				messageText.append("\n");
				messageText.append(str);
				messageText.append("\n");
			}
		});
	}
}
