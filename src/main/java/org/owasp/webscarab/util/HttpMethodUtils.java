/**
 *
 */
package org.owasp.webscarab.util;

import java.io.IOException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.ProtocolException;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.TraceMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.NamedValue;
import org.owasp.webscarab.domain.StreamingConversation;

/**
 * @author rdawes
 *
 */
public class HttpMethodUtils {

    public static String[] SUPPORTED_METHODS = new String[] { "GET", "POST", "OPTIONS", "HEAD", "DELETE", "PUT", "TRACE" };

	public static HttpMethod constructMethod(Conversation conversation)
			throws ProtocolException {
		HttpMethod httpMethod = null;
		PostMethod postMethod = null;
		String method = conversation.getRequestMethod();
		String uri = conversation.getRequestUri().toString();
		if (method.equals("GET")) {
			httpMethod = new GetMethod(uri);
		} else if (method.equals("POST")) {
			postMethod = new PostMethod(uri);
			httpMethod = postMethod;
		} else if (method.equals("OPTIONS")) {
			httpMethod = new OptionsMethod(uri);
		} else if (method.equals("HEAD")) {
			httpMethod = new HeadMethod(uri);
		} else if (method.equals("DELETE")) {
			httpMethod = new DeleteMethod(uri);
		} else if (method.equals("PUT")) {
			httpMethod = new PutMethod(uri);
		} else if (method.equals("TRACE")) {
			httpMethod = new TraceMethod(uri);
		} else {
			conversation.setResponseVersion("HTTP/1.0");
			conversation.setResponseStatus("501");
			conversation.setResponseMessage("Not implemented");
			String error = "Method " + method + " not supported";
			conversation.setResponseContent(error.getBytes());
			return null;
		}
		httpMethod.getParams().setParameter(HttpMethodParams.PROTOCOL_VERSION,
				HttpVersion.parse(conversation.getRequestVersion()));
		NamedValue[] headers = conversation.getRequestHeaders();
		for (int i = 0; headers != null && i < headers.length; i++) {
			httpMethod.addRequestHeader(headers[i].getName(), headers[i]
					.getValue());
		}
		if (postMethod != null) {
			RequestEntity requestEntity = null;
			byte[] bytes = conversation.getRequestContent();
			if (bytes != null) {
				// System.out.println("Creating a ByteArrayRequestEntity");
				requestEntity = new ByteArrayRequestEntity(bytes);
			}
			postMethod.setRequestEntity(requestEntity);
		}
		httpMethod.setFollowRedirects(false);
		// Provide custom retry handler if necessary
		httpMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler(0, false));
		return httpMethod;
	}

	public static void fillResponse(Conversation conversation,
			HttpMethod httpMethod) throws IOException {
		conversation.setResponseVersion(httpMethod.getStatusLine()
				.getHttpVersion());
		conversation.setResponseStatus(Integer.toString(httpMethod
				.getStatusCode()));
        conversation.setResponseMessage(httpMethod.getStatusLine().getReasonPhrase());
        Header[] headers = httpMethod.getResponseHeaders();
        conversation.setResponseHeaders(convert(headers));
        if (conversation instanceof StreamingConversation) {
        	StreamingConversation sc = (StreamingConversation) conversation;
        	sc.setResponseContentStream(httpMethod.getResponseBodyAsStream());
        } else {
        	conversation.setResponseContent(httpMethod.getResponseBody());
        }
	}

	public static void fillFooters(Conversation conversation, HttpMethod httpMethod) {
        Header[] footers = httpMethod.getResponseFooters();
        conversation.setResponseFooters(convert(footers));
	}

    private static NamedValue[] convert(Header[] headers) {
        NamedValue[] nv = null;
        if (headers != null) {
            nv = new NamedValue[headers.length];
            for (int i = 0; i < headers.length; i++) {
                Header header = headers[i];
                nv[i] = new NamedValue(header.getName(), header.getValue());
            }
        }
        return nv;
    }

}
