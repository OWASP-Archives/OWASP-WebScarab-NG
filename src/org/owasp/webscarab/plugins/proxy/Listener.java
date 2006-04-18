/**
 * 
 */
package org.owasp.webscarab.plugins.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.KeyStore;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.owasp.webscarab.Conversation;
import org.owasp.webscarab.ConversationSummary;
import org.owasp.webscarab.services.ConversationService;

/**
 * @author rdawes
 * 
 */
public class Listener implements Runnable {

	private ConversationService conversationService;

	private SocketAddress socketAddress;

	private String base;

	private SSLSocketFactory sslSocketFactory;

	private String keyStoreFile = null;

	private String keyStorePassword = null;

	private String keyPassword = null;

	private boolean stopped = true;

	private boolean running = false;

	private Logger logger = Logger.getLogger(getClass().getName());

	public Listener(SocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}

	public void setConversationService(ConversationService conversationService) {
		this.conversationService = conversationService;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public void run() {
		try {
			logger.info("Listening on " + socketAddress);
			ServerSocket serverSocket = new ServerSocket();
			serverSocket.setReuseAddress(true);
			serverSocket.bind(socketAddress, 5);
			try {
				serverSocket.setSoTimeout(100);
			} catch (SocketException se) {
				se.printStackTrace();
			}
			stopped = false;
			running = true;
			while (!stopped) {
				try {
					Socket sock = serverSocket.accept();
					logger.info("Connection from "
							+ sock.getRemoteSocketAddress());
					ConnectionHandler ch = new ConnectionHandler(this, sock,
							base);
					ch.setSslSocketFactory(getSslSocketFactory());
					Thread thread = new Thread(ch);
					thread.setDaemon(true);
					thread.start();
				} catch (SocketTimeoutException ste) {
					// we don't care
				} catch (IOException e) {
					e.printStackTrace();
					serverSocket.close();
					return;
				}
			}
			running = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		stopped = true;
	}

	public boolean isRunning() {
		return running;
	}

	/**
	 * @return Returns the keyPassword.
	 */
	public String getKeyPassword() {
		return keyPassword;
	}

	/**
	 * @param keyPassword
	 *            The keyPassword to set.
	 */
	public void setKeyPassword(String keyPassword) {
		this.keyPassword = keyPassword;
	}

	/**
	 * @return Returns the keyStoreFile.
	 */
	public String getKeyStoreFile() {
		return keyStoreFile;
	}

	/**
	 * @param keyStoreFile
	 *            The keyStoreFile to set.
	 */
	public void setKeyStoreFile(String keyStoreFile) {
		this.keyStoreFile = keyStoreFile;
	}

	/**
	 * @return Returns the keyStorePassword.
	 */
	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	/**
	 * @param keyStorePassword
	 *            The keyStorePassword to set.
	 */
	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	/**
	 * @return Returns the sslSocketFactory.
	 */
public SSLSocketFactory getSslSocketFactory() {
		if (sslSocketFactory == null) {
			if (getKeyStoreFile() != null) {
				try {
					KeyStore ks = KeyStore.getInstance("PKCS12");
					InputStream is = getClass().getClassLoader()
							.getResourceAsStream(getKeyStoreFile());
					if (is == null)
						throw new NullPointerException("No keystore found!!");
					char[] ksp = null;
					if (getKeyStorePassword() != null) 
						ksp = getKeyStorePassword().toCharArray();
					ks.load(is, ksp);
					KeyManagerFactory kmf = KeyManagerFactory
							.getInstance("SunX509");
					char[] kp = null;
					if (getKeyPassword() != null)
						kp = getKeyPassword().toCharArray();
					kmf.init(ks, kp);
					SSLContext sslcontext = SSLContext.getInstance("SSLv3");
					sslcontext.init(kmf.getKeyManagers(), null, null);
					sslSocketFactory = sslcontext.getSocketFactory();
				} catch (Exception e) {
				}
			}
		}
		return sslSocketFactory;
	}
	/**
	 * @param sslSocketFactory
	 *            The sslSocketFactory to set.
	 */
	public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
		this.sslSocketFactory = sslSocketFactory;
	}

	public void processedConversation(Conversation conversation,
			ConversationSummary summary) {
		if (conversationService != null)
			conversationService.addConversation(conversation, summary);
	}

}
