/**
 * 
 */
package org.owasp.webscarab.plugins.proxy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.commons.httpclient.ChunkedOutputStream;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.owasp.webscarab.Conversation;
import org.owasp.webscarab.NamedValue;
import org.owasp.webscarab.util.HttpMethodUtils;

import junit.framework.TestCase;

/**
 * @author rdawes
 *
 */
public class HttpClientConversationTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(HttpClientConversationTest.class);
    }

    /*
     * Test method for 'org.owasp.webscarab.plugins.proxy.HttpClientConversation.constructHttpMethod()'
     */
    public void testConstructHttpMethod() throws Exception {
        Conversation c = new Conversation();
        c.setRequestMethod("GET");
        c.setRequestUri(new URI("http://localhost/WebScarab-test/TestGet.jsp"));
        c.setRequestVersion("HTTP/1.0");
        c.addRequestHeader(new NamedValue("Accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*"));
        c.addRequestHeader(new NamedValue("Accept-Language", "en-us"));
        c.addRequestHeader(new NamedValue("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        c.addRequestHeader(new NamedValue("Host", "webmail.dawes.za.net"));
        c.addRequestHeader(new NamedValue("Proxy-Connection", "Keep-Alive"));
        HttpMethod method = HttpMethodUtils.constructMethod(c);
        HttpClient client = new HttpClient();
        client.executeMethod(method);
        HttpMethodUtils.fillResponse(c, method);
        System.out.println(method.getStatusLine());
        Header[] headers = method.getResponseHeaders();
        if (headers != null) 
        	for (int i=0; i<headers.length; i++) 
        		System.out.print(headers[i]);
        assertEquals("Got the wrong length", 231, c.getResponseContent().length);
    }

    public void btestWriteToBrowser() throws Exception {
        Conversation conversation = new Conversation();
        conversation.setRequestMethod("GET");
        conversation.setRequestUri(new URI("http://webmail.dawes.za.net/"));
        conversation.setRequestVersion("HTTP/1.0");
        conversation.addRequestHeader(new NamedValue("Accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*"));
        conversation.addRequestHeader(new NamedValue("Accept-Language", "en-us"));
        conversation.addRequestHeader(new NamedValue("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        conversation.addRequestHeader(new NamedValue("Host", "webmail.dawes.za.net"));
        conversation.addRequestHeader(new NamedValue("Proxy-Connection", "Keep-Alive"));
        HttpMethod method = HttpMethodUtils.constructMethod(conversation);
        HttpClient client = new HttpClient();
        client.executeMethod(method);
        OutputStream os = System.out; // new BufferedOutputStream(System.out);
        os.write((conversation.getResponseVersion() + " "
                + conversation.getResponseStatus() + " "
                + conversation.getResponseMessage() + "\r\n").getBytes());
        NamedValue[] responseHeaders = conversation.getResponseHeaders();
        for (int i = 0; responseHeaders != null && i < responseHeaders.length; i++) {
            os.write((responseHeaders[i].getName() + ": "
                    + responseHeaders[i].getValue() + "\r\n").getBytes());
        }
        os.write("\r\n".getBytes());
        String chunked = conversation.getResponseHeader("Transfer-Encoding");
        ChunkedOutputStream cos = null;
        InputStream cs = conversation.getResponseContentStream();
        if (cs != null) {
            if (chunked != null && chunked.equalsIgnoreCase("chunked")) {
                cos = new ChunkedOutputStream(os);
                os = cos;
            }
            byte[] buff = new byte[512];
            int got;
            while ((got = cs.read(buff)) > -1)
                os.write(buff, 0, got);
        }
        os.flush();
        if (cos != null) cos.finish();
    }

    public void testPost() throws Exception {
        Conversation conversation = new Conversation();
        conversation.setRequestMethod("POST");
        conversation.setRequestUri(new URI("http://localhost/WebScarab-test/TestGet.jsp"));
        conversation.setRequestVersion("HTTP/1.0");
        conversation.addRequestHeader(new NamedValue("Accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*"));
        conversation.addRequestHeader(new NamedValue("Referer", "http://webmail.dawes.za.net/src/login.php"));
        conversation.addRequestHeader(new NamedValue("Accept-Language", "en-us"));
        conversation.addRequestHeader(new NamedValue("Content-Type", "application/x-www-form-urlencoded"));
        conversation.addRequestHeader(new NamedValue("Proxy-Connection", "Keep-Alive"));
        conversation.addRequestHeader(new NamedValue("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"));
        conversation.addRequestHeader(new NamedValue("Content-Length", "123"));
        conversation.addRequestHeader(new NamedValue("Pragma", "no-cache"));
        conversation.addRequestHeader(new NamedValue("Cookie", "SQMSESSID=78fc5f4c1edfa52e1ddb1ffb7adc5493"));
        ByteArrayInputStream bais = new ByteArrayInputStream("SQMSESSID=78fc5f4c1edfa52e1ddb1ffb7adc5493&login_username=rdawes&secretkey=enitusa&js_autodetect_results=1&just_logged_in=1".getBytes());
        HttpMethodUtils.setRequestContent(conversation, bais);
        HttpMethod method = HttpMethodUtils.constructMethod(conversation);
        HttpClient client = new HttpClient();
        client.executeMethod(method);
        HttpMethodUtils.fillResponse(conversation, method);
        OutputStream os = System.out;
        os.write((conversation.getResponseVersion() + " "
                + conversation.getResponseStatus() + " "
                + conversation.getResponseMessage() + "\r\n").getBytes());
        NamedValue[] responseHeaders = conversation.getResponseHeaders();
        for (int i = 0; responseHeaders != null && i < responseHeaders.length; i++) {
            os.write((responseHeaders[i].getName() + ": "
                    + responseHeaders[i].getValue() + "\r\n").getBytes());
        }
        os.write("\r\n".getBytes());
        String chunked = conversation.getResponseHeader("Transfer-Encoding");
        ChunkedOutputStream cos = null;
        InputStream cs = conversation.getResponseContentStream();
        if (cs != null) {
            if (chunked != null && chunked.equalsIgnoreCase("chunked")) {
                cos = new ChunkedOutputStream(os);
                os = cos;
            }
            byte[] buff = new byte[1024];
            int got;
            while ((got = cs.read(buff)) > -1)
                os.write(buff, 0, got);
        }
        os.flush();
        if (cos != null) cos.finish();
    }

}
