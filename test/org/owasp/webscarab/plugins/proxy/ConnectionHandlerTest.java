/**
 * 
 */
package org.owasp.webscarab.plugins.proxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import junit.framework.TestCase;

/**
 * @author rdawes
 * 
 */
public class ConnectionHandlerTest extends TestCase {

    private static String stdHeaders = "Host: localhost\r\n"
            + "User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)\r\n"
            + "Proxy-Connection: Keep-Alive\r\n";

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ConnectionHandlerTest.class);
    }

    /*
     * Test method for
     * 'org.owasp.webscarab.plugins.proxy.ConnectionHandler.ConnectionHandler(Listener,
     * Socket, String)'
     */
    public void btestConnectionHandlerGet() throws IOException {
        System.err.println("TestConnectionHandlerGet");
        StringBuffer request = new StringBuffer();
        request.append("GET http://localhost:80/WebScarab-test/TestGet.jsp HTTP/1.0\r\n");
        request.append(stdHeaders);
        request.append("\r\n");
        
        request = request.append(request);
        
        InputStream is = new ByteArrayInputStream(request.toString().getBytes());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Socket socket = new MockSocket(is, os);
        ConnectionHandler connectionHandler = new ConnectionHandler(socket, null);
        connectionHandler.run();
        System.out.write(os.toByteArray());
        assertEquals("Response size", 477, os.size());
    }

    /*
     * Test method for
     * 'org.owasp.webscarab.plugins.proxy.ConnectionHandler.ConnectionHandler(Listener,
     * Socket, String)'
     */
    public void testConnectionHandlerGetKeepAlive() throws IOException {
        System.err.println("TestConnectionHandlerGet");
        StringBuffer request = new StringBuffer();
        request.append("GET http://localhost:80/WebScarab-test/TestGet.jsp HTTP/1.0\r\n");
        request.append(stdHeaders);
        request.append("Connection: Keep-Alive\r\n");
        request.append("\r\n");
        
        request.append("GET http://localhost:80/WebScarab-test/TestGet.jsp HTTP/1.0\r\n");
        request.append(stdHeaders);
        request.append("Connection: close\r\n");
        request.append("\r\n");
        
        InputStream is = new ByteArrayInputStream(request.toString().getBytes());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Socket socket = new MockSocket(is, os);
        ConnectionHandler connectionHandler = new ConnectionHandler(socket, null);
        connectionHandler.run();
        System.out.write(os.toByteArray());
        assertEquals("Response size", 482, os.size());
    }

    public void btestConnectionHandlerPost() throws IOException {
        System.err.println("TestConnectionHandlerPost");
        StringBuffer request = new StringBuffer();
        request.append("POST http://localhost:80/WebScarab-test/TestGet.jsp HTTP/1.0\r\n");
        request.append(stdHeaders);
        request.append("Content-Type: application/x-www-form-urlencoded\r\n");
        request.append("Content-length: 12\r\n");
        request.append("\r\n");
        request.append("person=RoganDawes");

        InputStream is = new ByteArrayInputStream(request.toString().getBytes());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Socket socket = new MockSocket(is, os);
        ConnectionHandler connectionHandler = new ConnectionHandler(socket, null);
        connectionHandler.run();
        System.out.write(os.toByteArray());
        assertEquals("Response size", 482, os.size());
    }

    private static class MockSocket extends Socket {
        
        private InputStream is;
        private OutputStream os;
        
        public MockSocket(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
        }

        /* (non-Javadoc)
         * @see java.net.Socket#getInputStream()
         */
        @Override
        public InputStream getInputStream() throws IOException {
            return this.is;
        }

        /* (non-Javadoc)
         * @see java.net.Socket#getOutputStream()
         */
        @Override
        public OutputStream getOutputStream() throws IOException {
            return this.os;
        }
        
    }

}
