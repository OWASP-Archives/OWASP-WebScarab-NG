/**
 *
 */
package org.owasp.webscarab.plugins.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.httpclient.ChunkedInputStream;
import org.apache.commons.httpclient.ChunkedOutputStream;
import org.apache.commons.httpclient.ContentLengthInputStream;
import org.bushe.swing.event.EventService;
import org.bushe.swing.event.EventServiceEvent;
import org.bushe.swing.event.EventSubscriber;
import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.NamedValue;
import org.owasp.webscarab.domain.Session;
import org.owasp.webscarab.domain.SessionEvent;
import org.owasp.webscarab.domain.StreamingConversation;
import org.owasp.webscarab.services.ConversationService;
import org.owasp.webscarab.services.HttpService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author rdawes
 *
 */
public class Proxy implements ApplicationContextAware, EventSubscriber {

    public final static String PROPERTY_LISTENERS = "listeners";

    private List<Listener> listeners = new ArrayList<Listener>();

    private List<ListenerConfiguration> configurations = new ArrayList<ListenerConfiguration>();

    private Annotator annotator;

    private SSLSocketFactory sslSocketFactory = null;

    private ConversationService conversationService = null;

    private HttpService httpService = null;

    private EventService eventService;

    private ApplicationContext applicationContext;

    private ProxyInterceptor proxyInterceptor = null;

    private Session session;

    public Proxy() {
    }

    public void setListeners(List<ListenerConfiguration> listeners)
            throws IOException {
        stopListeners();
        configurations = copy(listeners);
        try {
            startListeners();
        } catch (IOException ioe) {
            stopListeners();
            throw ioe;
        }
    }

    public List<ListenerConfiguration> getListeners() {
        List<ListenerConfiguration> copy = copy(configurations);
        if (copy.size() == 0) {
            ListenerConfiguration config = new ListenerConfiguration();
            config.setHostName("localhost");
            config.setPort(8008);
            copy.add(config);
        }
        return copy;
    }

    private List<ListenerConfiguration> copy(List<ListenerConfiguration> configs) {
        List<ListenerConfiguration> copy = new ArrayList<ListenerConfiguration>();
        Iterator<ListenerConfiguration> it = configs.iterator();
        while (it.hasNext())
            copy.add(new ListenerConfiguration(it.next()));
        return copy;
    }

    private void startListeners() throws IOException {
        if (listeners.size() > 0)
            throw new IOException("Existing listeners are still active!");
        Iterator<ListenerConfiguration> it = getListeners().iterator();
        while (it.hasNext()) {
            ListenerConfiguration config = it.next();
            Listener listener = new Listener(config);
            new Thread(listener, listener.getConfiguration().toString())
                    .start();
            listeners.add(listener);
        }
    }

    private void stopListeners() {
        Iterator<Listener> it = listeners.iterator();
        while (it.hasNext()) {
            Listener listener = it.next();
            listener.stop();
            it.remove();
        }
    }

    public Annotator getAnnotator() {
        return this.annotator;
    }

    public void setAnnotator(Annotator annotator) {
        this.annotator = annotator;
    }

    public SSLSocketFactory getSslSocketFactory(String hostName, int port) {
        return getDefaultSslSocketFactory();
    }

    /**
     * @return Returns the sslSocketFactory.
     */
    public SSLSocketFactory getDefaultSslSocketFactory() {
        if (sslSocketFactory == null) {
            try {
                KeyStore ks = KeyStore.getInstance("PKCS12");
                InputStream is = getClass().getClassLoader()
                        .getResourceAsStream("certificates/server.p12");
                if (is != null) {
                    char[] ksp = "password".toCharArray();
                    ks.load(is, ksp);
                    KeyManagerFactory kmf = KeyManagerFactory
                            .getInstance("SunX509");
                    char[] kp = "password".toCharArray();
                    kmf.init(ks, kp);
                    SSLContext sslcontext = SSLContext.getInstance("SSLv3");
                    sslcontext.init(kmf.getKeyManagers(), null, null);
                    sslSocketFactory = sslcontext.getSocketFactory();
                }
            } catch (KeyStoreException kse) {
            } catch (IOException ioe) {
            } catch (CertificateException ce) {
            } catch (NoSuchAlgorithmException nsae) {
            } catch (UnrecoverableKeyException uke) {
            } catch (KeyManagementException kme) {
            }
        }
        return sslSocketFactory;
    }

    public void setConversationService(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    public ConversationService getConversationService() {
        if (conversationService == null)
            conversationService = (ConversationService) applicationContext
                    .getBean("conversationService");
        return conversationService;
    }

    public ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public EventService getEventService() {
        return this.eventService;
    }

    public void setEventService(EventService eventService) {
        if (getEventService() != null) {
            getEventService().unsubscribe(SessionEvent.class, this);
        }
        this.eventService = eventService;
        if (getEventService() != null) {
            getEventService().subscribeStrongly(SessionEvent.class, this);
        }
    }

    public void onEvent(EventServiceEvent evt) {
        if (evt instanceof SessionEvent) {
            SessionEvent event = (SessionEvent) evt;
            if (event.getType() == SessionEvent.SESSION_CHANGED) {
                setSession(event.getSession());
                try {
                    startListeners();
                } catch (IOException ioe) {
                    System.out.println("Error starting listeners " + ioe);
                }
            }
        }
    }

    class Listener implements Runnable {

        private ListenerConfiguration configuration;

        private ServerSocket serverSocket;

        private boolean stopped = true;

        private boolean running = false;

        private Logger logger = Logger.getLogger(getClass().getName());

        public Listener(ListenerConfiguration configuration) throws IOException {
            this.configuration = configuration;
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.setSoTimeout(100);

            // we force an exception on the creating thread
            // if the configurations is bad
            logger.info("Listening on " + configuration.getSocketAddress());
            serverSocket.bind(configuration.getSocketAddress(), 5);
        }

        public ListenerConfiguration getConfiguration() {
            return this.configuration;
        }

        public void run() {
            try {
                stopped = false;
                running = true;
                while (!stopped) {
                    try {
                        Socket sock = serverSocket.accept();
                        logger.info("Connection from "
                                + sock.getRemoteSocketAddress());
                        ConnectionHandler ch = new ConnectionHandler(sock,
                                configuration.getBase());
                        Thread thread = new Thread(ch);
                        thread.setDaemon(true);
                        thread.start();
                    } catch (SocketTimeoutException ste) {
                        // we don't care
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (serverSocket.isBound() && !serverSocket.isClosed())
                    try {
                        serverSocket.close();
                    } catch (IOException ioe) {
                    }
                running = false;
            }
        }

        public void stop() {
            stopped = true;
        }

        public boolean isRunning() {
            return running;
        }

        class ConnectionHandler implements Runnable {

            private final static String NO_CERTIFICATE = "HTTP/1.0 503 Service unavailable - SSL server certificate not available\r\n\r\n";

            private final static String NO_CERTIFICATE_MESSAGE = "There is no SSL server certificate available for use";

            private Socket socket;

            private String base;

            public ConnectionHandler(Socket socket, String base) {
                this.socket = socket;
                this.base = base;
            }

            public void run() {
                try {
                    InputStream is = socket.getInputStream();
                    OutputStream os = socket.getOutputStream();

                    boolean close;
                    do {
                        close = false;
                        StreamingConversation conversation = readRequest(is);

                        // empty request line, connection closed?
                        if (conversation == null)
                            return;

                        if (conversation.getRequestMethod().equals("CONNECT")) {
                            if (getDefaultSslSocketFactory() == null) {
                                os.write(NO_CERTIFICATE.getBytes());
                                os.write(NO_CERTIFICATE_MESSAGE.getBytes());
                                os.flush();
                                return;
                            } else {
                                os.write("HTTP/1.0 200 Ok\r\n\r\n".getBytes());
                                os.flush();
                                // start over from the beginning to handle this
                                // connection as an SSL connection
                                this.socket = negotiateSSL();
                                this.base = conversation.getRequestUri()
                                        .toString();
                                this.run();
                                return;
                            }
                        }

                        // Get the request content (if any) from the stream,
                        // apply it
                        // to the HttpMethod, as well as to the conversation.
                        setRequestContent(conversation, is);

                        // see if we can get an annotation for this conversation
                        Annotation annotation = null;
                        if (getAnnotator() != null)
                            annotation = getAnnotator().getAnnotation();

                        if (annotation == null)
                            annotation = new Annotation();

                        if (proxyInterceptor != null)
                            proxyInterceptor.editRequest(conversation,
                                    annotation);

                        httpService.fetchResponse(conversation);

                        // we use a buffered conversation to record any changes
                        // made
                        // to the response during editing since we want to save
                        // the
                        // original response as sent by the server in the
                        // archive
                        BufferedConversation bc = null;
                        if (proxyInterceptor != null) {
                            bc = new BufferedConversation(conversation);
                            proxyInterceptor.editResponse(bc, annotation);
                        }
                        try {
                            if (bc != null) {
                                writeConversationToBrowser(bc, os);
                            } else {
                                writeConversationToBrowser(conversation, os);
                            }
                        } catch (SocketException se) {
                            conversation.getResponseContent();
                            close = true;
                        }
                        if (getConversationService() != null) {
                            conversation.setSource("Proxy");
                            getConversationService().addConversation(
                                    getSession(), conversation);
                            if (!"".equals(annotation.getAnnotation())) {
                                annotation.setId(conversation.getId());
                                getConversationService().updateAnnotation(
                                        annotation);
                            }
                        }
                        String connection = conversation
                                .getResponseHeader("Connection");
                        if ("Keep-Alive".equalsIgnoreCase(connection)) {
                            close = false;
                        } else {
                            close = true;
                        }
                    } while (!close);
                } catch (IllegalArgumentException iae) {
                    logger.severe(iae.getMessage());
                    iae.printStackTrace();
                } catch (IOException ioe) {
                    logger.severe(ioe.getMessage());
                    ioe.printStackTrace();
                } finally {
                    try {
                        if (!socket.isClosed())
                            socket.close();
                    } catch (IOException ioe) {
                    }
                }
            }

            private String readLine(InputStream is) throws IOException {
                StringBuffer line = new StringBuffer();
                int i;
                char c;
                i = is.read();
                if (i == -1)
                    return null;
                while (i > -1 && i != 10 && i != 13) {
                    // Convert the int to a char
                    c = (char) (i & 0xFF);
                    line = line.append(c);
                    i = is.read();
                }
                if (i == 13) {
                    // 10 is unix LF, but DOS does 13+10,
                    // so read the 10 if we got 13
                    is.read();
                }
                return line.toString();
            }

            private StreamingConversation readRequest(InputStream is)
                    throws IOException {
                StreamingConversation conversation = new StreamingConversation();
                String requestLine;
                try {
                    do {
                        requestLine = readLine(is);
                        if (requestLine == null)
                            return null;
                    } while (requestLine.trim().equals(""));
                } catch (IOException ioe) {
                    logger
                            .info("Error reading requestLine - incomplete SSL connection?");
                    return null;
                }
                int first = requestLine.indexOf(" ");
                int last = requestLine.lastIndexOf(" ");
                conversation.setRequestMethod(requestLine.substring(0, first));
                if (conversation.getRequestMethod().equals("CONNECT"))
                    base = "https://";
                conversation.setRequestVersion(requestLine.substring(last + 1));
                try {
                    URI uri;
                    if (base == null) {
                        uri = new URI(requestLine.substring(first + 1, last));
                    } else {
                        uri = new URI(base
                                + requestLine.substring(first + 1, last));
                    }
                    conversation.setRequestUri(uri);
                } catch (URISyntaxException use) {
                    IOException ioe = new IOException(
                            "URI Syntax exception parsing '" + base + "'");
                    ioe.initCause(use);
                    throw ioe;
                }

                String previous = null;
                String header = null;
                do {
                    header = readLine(is);
                    if (header == null)
                        throw new IOException(
                                "Unexpected null response reading header line");
                    if (header.startsWith(" ")) {
                        if (previous == null)
                            throw new IOException("Malformed header line: '"
                                    + header + "'");
                        previous = previous.trim() + " " + header.trim();
                    } else {
                        if (previous != null) {
                            int colon = previous.indexOf(":");
                            String name = previous.substring(0, colon);
                            String value = previous.substring(colon + 1).trim();
                            conversation.addRequestHeader(new NamedValue(name,
                                    value));
                        }
                        previous = header;
                    }
                } while (!header.equals(""));
                return conversation;
            }

            private void setRequestContent(Conversation conversation,
                    InputStream is) throws IOException {
                if ("CONNECT".equals(conversation.getRequestMethod()))
                    return;
                if ("HEAD".equals(conversation.getRequestMethod()))
                    return;
                if ("GET".equals(conversation.getRequestMethod()))
                    return;
                InputStream contentInputStream = null;
                if ("POST".equals(conversation.getRequestMethod())) {
                    String te = conversation
                            .getRequestHeader("Transfer-Encoding");
                    String length = conversation
                            .getRequestHeader("Content-Length");
                    if ("chunked".equalsIgnoreCase(te)) {
                        contentInputStream = new ChunkedInputStream(is);
                    } else if (length != null) {
                        try {
                            long cl = Long.parseLong(length);
                            contentInputStream = new ContentLengthInputStream(
                                    is, cl);
                        } catch (NumberFormatException nfe) {
                            IOException ioe = new IOException(
                                    "Error parsing Content-Length header: "
                                            + length);
                            ioe.initCause(nfe);
                            throw ioe;
                        }
                    } else {
                        return;
                    }
                } else {
                    throw new IOException("Can "
                            + conversation.getRequestMethod()
                            + " have a body or not? Not implemented yet!");
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buff = new byte[4096];
                int got;
                while ((got = contentInputStream.read(buff)) > -1) {
                    baos.write(buff, 0, got);
                }
                conversation.setRequestContent(baos.toByteArray());
            }

            private void writeConversationToBrowser(Conversation conversation,
                    OutputStream os) throws IOException {
                os.write((conversation.getResponseVersion() + " "
                        + conversation.getResponseStatus() + " "
                        + conversation.getResponseMessage() + "\r\n")
                        .getBytes());
                NamedValue[] responseHeaders = conversation
                        .getResponseHeaders();
                for (int i = 0; responseHeaders != null
                        && i < responseHeaders.length; i++) {
                    os.write((responseHeaders[i].getName() + ": "
                            + responseHeaders[i].getValue() + "\r\n")
                            .getBytes());
                }
                os.write("\r\n".getBytes());
                String chunked = conversation
                        .getResponseHeader("Transfer-Encoding");
                ChunkedOutputStream cos = null;
                if (chunked != null && chunked.equalsIgnoreCase("chunked")) {
                    cos = new ChunkedOutputStream(os);
                    os = cos;
                }
                if (conversation instanceof StreamingConversation) {
                    StreamingConversation sc = (StreamingConversation) conversation;
                    InputStream cs = sc.getResponseContentStream();
                    byte[] buff = new byte[4096];
                    int got;
                    while ((got = cs.read(buff)) > -1)
                        os.write(buff, 0, got);
                } else {
                    byte[] content = conversation.getResponseContent();
                    if (content != null)
                        os.write(content);
                }
                os.flush();
                if (cos != null)
                    cos.finish();
            }

            private Socket negotiateSSL() throws IOException {
                SSLSocketFactory factory = getSslSocketFactory(socket
                        .getInetAddress().getHostName(), socket.getPort());
                SSLSocket sslsock = (SSLSocket) factory.createSocket(socket,
                        socket.getInetAddress().getHostName(),
                        socket.getPort(), true);
                sslsock.setUseClientMode(false);
                return sslsock;
            }

        }

    }

    public ProxyInterceptor getProxyInterceptor() {
        return this.proxyInterceptor;
    }

    public void setProxyInterceptor(ProxyInterceptor proxyInterceptor) {
        this.proxyInterceptor = proxyInterceptor;
    }

    /**
     * @param httpService
     *            the httpService to set
     */
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    /**
     * @return the session
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * @param session
     *            the session to set
     */
    public void setSession(Session session) {
        this.session = session;
    }

}
