package org.nextwin.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.nextwin.protocol.Header;
import org.nextwin.util.JsonManager;

import com.fasterxml.jackson.core.JsonProcessingException;

public class NetworkManager {
		
	private Socket socket;
	private OutputStream sender;
	private InputStream receiver;
		
	public NetworkManager(Socket socket) {
		this.socket = socket;
		try {
			sender = socket.getOutputStream();
			receiver = socket.getInputStream();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Check socket is connected.
	 * @return socket.isConnected() && !socket.isClosed()
	 */
	public boolean isConnected() {
		return (socket.isConnected() && !socket.isClosed());
	}
	
	/**
	 * Receive data except header.
	 * @return received data, null when fail
	 * @throws IOException
	 */
	public byte[] receive(int length) {
		if(socket.isClosed())
			return null;
		
		byte[] data = new byte[length];
		try {
			receiver.read(data, 0, length);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return data;
	}
	
	/**
	 * Receive header of packets.
	 * @return received header, null when fail
	 * @throws IOException
	 */
	public Header receive() {
		if(socket.isClosed())
			return null;
		
		byte[] head = new byte[Header.HEADER_LENGTH];
		try {
			receiver.read(head, 0, Header.HEADER_LENGTH);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		Header header = (Header)JsonManager.bytesToObject(head, Header.class);
		return header;
	}
	
	/**
	 * Create appropriate header of data and combine them. Finally send one.
	 * @param msgType
	 * @param object
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	public void send(int msgType, Object object) {
		byte[] data = JsonManager.objectToBytes(object);
		
		Header header = new Header(msgType, data.length);
		byte[] head = JsonManager.objectToBytes(header);
		
		// 최종 전송할 패킷(헤더 + 데이터) 직렬화
		byte[] packet = new byte[head.length + data.length];
		System.arraycopy(head, 0, packet, 0, head.length);
		System.arraycopy(data, 0, packet, head.length, data.length);
		
		try {
			sender.write(packet);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Close socket, inputStream and outputStream.
	 */
	public void close() {
		try {
			if(socket != null)
				socket.close();
			if(sender != null)
				sender.close();
			if(receiver != null)
				receiver.close();			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void checkSocketState() {
		System.out.println("socket.isConnected " + socket.isConnected());
		System.out.println("socket.isClosed " + socket.isClosed());
	}
	
	public Socket getSocket() {
		return socket;
	}
	
}