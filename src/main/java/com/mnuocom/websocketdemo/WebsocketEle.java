/**
 * WebsocketEle.java created at 2016年6月20日 下午3:02:35
 */
package com.mnuocom.websocketdemo;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * @author mnuo
 */
@ServerEndpoint("/websocket/{relationId}/{userCode}")//该注解用来指定一个URI,客户端可以通过这个URI来连接到WebSocket,类似Servlet的注解Mapping,无需再web.xml中配置
public class WebsocketEle {
	//静态变量,用来记录当前在线连接数,应该把它设计成线程安全的
	private static int onlineCount = 0;
	//concurrent包的线程安全Set 用来存放每个客户端对应的WebsocketEle对象,若要实现服务端与单一客户端通信的,可以使用Map来存放,其中Key可以为用户标识
	private static CopyOnWriteArraySet<WebsocketEle> websocketSet = new CopyOnWriteArraySet<WebsocketEle>();
	//与某个客户端的连接会话,需要通过它来给客户端发送数据
	private Session session;
	/**
	 * 连接建立成功调用的方法
	 * @param session 可选参数,session为与某个客户端的连接会话需要通过它来给客户端发送数据
	 * 2016年6月20日 下午3:39:42
	 */
	@OnOpen
	public void onOpen(@PathParam("relativeId") String relativeId, Session session){
		this.session = session;
		websocketSet.add(this);
		addOnlineCount();
		System.out.println("有新连接加入!当前在线人数为: " + getOnlineCount());
	}
	/**
	 * 连接关闭调用的方法 
	 * 2016年6月20日 下午3:40:37
	 */
	@OnClose
	public void onClose(){
		websocketSet.remove(this);//从set中删除
		subOnlineCount();//在线人数减1
		System.out.println("有一连接关闭!当前在线人数为: " + getOnlineCount());
	}
	/**
	 * 收到客户端消息后调用的方法
	 * @param message 客户端发送过来的消息
	 * @param session 可选的参数
	 * 2016年6月20日 下午3:42:48
	 */
	@OnMessage
	public void onMessage(String message, Session session){
		System.out.println("来自客户端的消息: " + message);
		for(WebsocketEle item : websocketSet){
			try {
				item.sendMessage(message);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
		}
	}
	/**
	 * 发生错误时调用
	 * @param session
	 * @param error
	 * 2016年6月20日 下午3:44:15
	 */
	@OnError
	public void onError(Session session, Throwable error){
		System.out.println("发生错误");
		error.printStackTrace();
	}
	
	public void sendMessage(String message) throws IOException{
		//this.session.getBasicRemote().sendText(message);//同步
		this.session.getAsyncRemote().sendText(message);//异步发送
	}
	
	public static synchronized int getOnlineCount(){
		return onlineCount;
	}
	
	public static synchronized void addOnlineCount(){
		WebsocketEle.onlineCount ++;
	}

	public static synchronized void subOnlineCount(){
		WebsocketEle.onlineCount --;
	}
}
