/**
 * @Title: Main.java
 * @Package: com.Wechat.app
 * @author: JwZheng
 * @date: 2020年11月2日 下午12:18:41
 * @Description: 
 */

package com.WeChat.app;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.WeChat.Daemon.GetUsersThread;
import com.WeChat.Daemon.ListenResponsesThread;
import com.WeChat.DataType.Request;
import com.WeChat.DataType.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;

/**
 * @ClassName: Main
 * @author JwZheng
 * @date: 2020年11月2日 下午12:18:41
 * @Description:
 */
public class Main {

	protected Shell shlWechat;
	private Text toSendText;
	private Text address;
	private Text port;
	private Text user;
	private Text messageText;
	private List userList;
	private String messageToSend;
	private ListenResponsesThread listenThread;
	private MessageBox messageBox;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Main window = new Main();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlWechat.open();
		shlWechat.layout();
		while (!shlWechat.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlWechat = new Shell(Display.getDefault(), SWT.CLOSE | SWT.MIN);

		shlWechat.addShellListener(new ShellAdapter() {
			@Override
			/**
			 * @Title: shellClosed
			 * @Description: 安全退出
			 * @param e
			 * @see org.eclipse.swt.events.ShellAdapter#shellClosed(org.eclipse.swt.events.ShellEvent)
			 */
			public void shellClosed(ShellEvent e) {
				if (DataBuffer.socket != null && !DataBuffer.socket.isClosed()) {
					try {
						Request logoutRequest = DataBuffer.requestBuilder.getLogoutRequest();
						DataBuffer.oos.writeObject(logoutRequest);
						DataBuffer.oos.flush();

						DataBuffer.socket.close();
					} catch (Exception e2) {
						// TODO: handle exception
					} finally {
						DataBuffer.socket = null;
					}
				}
			}
		});
		shlWechat.setSize(900, 600);
		shlWechat.setText("Wechat");

		toSendText = new Text(shlWechat, SWT.BORDER);
		toSendText.setBounds(10, 463, 738, 80);

		messageBox = new MessageBox(shlWechat, SWT.OK | SWT.CANCEL | SWT.ICON_WARNING);

		Button btnSend = new Button(shlWechat, SWT.NONE);
		btnSend.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				messageToSend = toSendText.getText().toString();
				if (DataBuffer.socket != null && !DataBuffer.socket.isClosed() && !messageToSend.equals("")) {
					try {
						User toUser = null;
						if (userList.getSelectionIndex() != -1) {
							toUser = DataBuffer.onlineUsersList.get(userList.getSelectionIndex());
							if (toUser.equals(DataBuffer.currentUser))
								toUser = null;
						}
						Request msgRequest = DataBuffer.requestBuilder.getChatRequest(toUser, messageToSend);
						DataBuffer.oos.writeObject(msgRequest);
						DataBuffer.oos.flush();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						System.out.println("发送消息失败");
						e1.printStackTrace();
					} finally {
						messageToSend = "";
						toSendText.setText("");
					}
				} else if (DataBuffer.socket == null) {
					messageBox.setMessage("未连接到服务器");
					messageBox.open();
				} else if (DataBuffer.socket.isClosed()) {
					messageText.append("\t\t\t服务器关闭连接\n");
					messageBox.setMessage("服务器关闭连接 !");
					messageBox.open();
				} else {
					messageBox.setMessage("不能发送空消息!");
					messageBox.open();
				}
			}
		});
		btnSend.setBounds(754, 461, 98, 30);

		btnSend.setText("\u53D1\u9001");
		Label lblip = new Label(shlWechat, SWT.NONE);
		lblip.setBounds(10, 38, 76, 20);

		lblip.setText("\u670D\u52A1\u5668IP:");
		address = new Text(shlWechat, SWT.BORDER);
		address.setBounds(92, 35, 144, 26);

		Label label = new Label(shlWechat, SWT.NONE);
		label.setBounds(274, 38, 43, 20);
		label.setText("\u7AEF\u53E3:");

		port = new Text(shlWechat, SWT.BORDER);
		port.setBounds(344, 35, 73, 26);

		Label label_1 = new Label(shlWechat, SWT.NONE);
		label_1.setBounds(453, 38, 43, 20);

		label_1.setText("\u59D3\u540D:");

		user = new Text(shlWechat, SWT.BORDER);
		user.setBounds(535, 35, 73, 26);

		Button btnConnect = new Button(shlWechat, SWT.NONE);
		btnConnect.addSelectionListener(new SelectionAdapter() {
			@Override
			/**
			 * @Description: 连接按钮
			 */
			public void widgetSelected(SelectionEvent e) {
				messageBox.setMessage("无法连接到服务器!");
				if (DataBuffer.socket == null) {
					try {
						DataBuffer.currentUser = new User(user.getText());
						DataBuffer.socket = new Socket("127.0.0.1", 9999);
						DataBuffer.oos = new ObjectOutputStream(DataBuffer.socket.getOutputStream());
						DataBuffer.ois = new ObjectInputStream(DataBuffer.socket.getInputStream());

						listenThread = new ListenResponsesThread(messageText, userList);
						listenThread.setDaemon(true);
						listenThread.start();

						Request loginRequest = DataBuffer.requestBuilder.getLoginRequest();
						DataBuffer.oos.writeObject(loginRequest);
						DataBuffer.oos.flush();

						messageBox.setMessage("连接成功!");

					} catch (UnknownHostException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					} catch (Exception e3) {
						messageBox.setMessage("请输入用户名 !");
					} finally {
						DataBuffer.getUsersThread = new GetUsersThread(messageBox);
						DataBuffer.getUsersThread.setDaemon(true);
						DataBuffer.getUsersThread.start();

						messageBox.open();
					}
				} else {
					System.out.println("Already connect success !");
				}
			}
		});
		btnConnect.setBounds(650, 33, 98, 30);

		btnConnect.setText("\u8FDE\u63A5");

		Button btnBroken = new Button(shlWechat, SWT.NONE);
		btnBroken.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (DataBuffer.socket != null && !DataBuffer.socket.isClosed()) {
					try {
						Request logoutRequest = DataBuffer.requestBuilder.getLogoutRequest();
						DataBuffer.oos.writeObject(logoutRequest);
						DataBuffer.oos.flush();

						DataBuffer.socket.close();
						DataBuffer.getUsersThread.interrupt();
						listenThread.interrupt();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} finally {
						DataBuffer.socket = null;
					}
				} else {
					messageBox.setMessage("当前未连接到服务器 !");
					messageBox.open();
				}
			}
		});
		btnBroken.setBounds(754, 33, 98, 30);

		btnBroken.setText("\u65AD\u5F00");

		userList = new List(shlWechat, SWT.BORDER | SWT.V_SCROLL);
		userList.setToolTipText("");
		userList.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		userList.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		userList.setFont(SWTResourceManager.getFont("微软雅黑", 14, SWT.BOLD));
		userList.setItems(new String[] {});
		userList.setBounds(10, 104, 226, 333);

		messageText = new Text(shlWechat, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		messageText.setToolTipText("");
		messageText.setForeground(SWTResourceManager.getColor(SWT.COLOR_MAGENTA));
		messageText.setEditable(false);
		messageText.setBounds(274, 114, 578, 327);

		Button btnClear = new Button(shlWechat, SWT.NONE);
		btnClear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				messageText.setText("");
			}
		});
		btnClear.setBounds(754, 513, 98, 30);
		btnClear.setText("\u6E05\u7A7A");
	}
}
