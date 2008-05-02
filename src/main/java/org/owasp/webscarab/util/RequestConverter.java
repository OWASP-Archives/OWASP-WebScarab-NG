/**
 * 
 */
package org.owasp.webscarab.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.NamedValue;

import com.twmacinta.util.MD5;

/**
 * @author rdawes
 *
 */
public class RequestConverter {

	public static Conversation convertGetToPost(Conversation get) throws IllegalArgumentException {
		if (!"GET".equals(get.getRequestMethod()))
			throw new IllegalArgumentException("Request method must be 'GET', was '" + get.getRequestMethod() + "'");
		Conversation post = new Conversation();
		post.setRequestMethod("POST");
		URI uri = get.getRequestUri();
		if (uri == null)
			throw new IllegalArgumentException("URI may not be null");
		String query = uri.getQuery();
		try {
			uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), null, uri.getFragment());
		} catch (URISyntaxException use) {
			throw new IllegalArgumentException("Invalid uri syntax : " + use.getMessage(), use);
		}
		post.setRequestUri(uri);
		post.setRequestVersion(get.getRequestVersion());
		post.setRequestHeaders(get.getRequestHeaders());
		post.setRequestHeader(new NamedValue("Content-Type", "application/x-www-form-urlencoded"));
		if (query != null) {
			post.setRequestHeader(new NamedValue("Content-Length", Integer.toString(query.length())));
			try {
				post.setProcessedRequestContent(query.getBytes("ASCII"));
			} catch (UnsupportedEncodingException uee) {
				throw new IllegalArgumentException("Weird! " + uee, uee);
			}
		} else {
			post.setRequestHeader(new NamedValue("Content-Length", "0"));
			post.setProcessedRequestContent(null);
		}
		return post;
	}
	
	public static Conversation convertPostToMultipartPost(Conversation post) throws IllegalArgumentException {
		if (!"POST".equals(post.getRequestMethod()))
			throw new IllegalArgumentException("Request method must be 'POST', was '" + post.getRequestMethod() + "'");
		
		Conversation multi = new Conversation();
		multi.setRequestMethod(post.getRequestMethod());
		multi.setRequestUri(post.getRequestUri());
		multi.setRequestVersion(post.getRequestVersion());
		
		NamedValue[] headers = post.getRequestHeaders();
		NamedValue contentType = NamedValue.findOne("Content-Type", headers);
		if (contentType == null || !"application/x-www-form-urlencoded".equals(contentType.getValue()))
			throw new IllegalArgumentException("Invalid 'Content-Type' header");
		multi.setRequestHeaders(headers);
		
		byte[] content = post.getProcessedRequestContent();
		if (content == null)
			content = new byte[0];
        MD5 md5 = new MD5();
        md5.Update(content);
        String sep = md5.asHex();
        
        contentType = new NamedValue("Content-Type", "multipart/form-data; boundary=" + sep);
        multi.setRequestHeader(contentType);
        
        String boundary = "--" + sep;
        String disposition = "Content-Disposition: form-data; name=";
        NamedValue[] nvs;
        try {
        	nvs = NamedValue.parse(new String(content, "ASCII"), "&", "=");
        } catch (UnsupportedEncodingException uee) {
			throw new IllegalArgumentException("Weird! " + uee, uee);
        }
        
        StringBuffer buff = new StringBuffer();
        buff.append(boundary);
        for (int i=0; i<nvs.length; i++) {
            buff.append("\r\n").append(disposition).append("\"").append(nvs[i].getName()).append("\"\r\n\r\n");
            buff.append(nvs[i].getValue()).append("\r\n").append(boundary);
        }
        buff.append("--\r\n");
        multi.setRequestHeader(new NamedValue("Content-Length", Integer.toString(buff.length())));
        try {
        	multi.setProcessedRequestContent(buff.toString().getBytes("ASCII"));
        } catch (UnsupportedEncodingException uee) {
			throw new IllegalArgumentException("Weird! " + uee, uee);
        }
		return multi;
	}
	
	public static Conversation convertPostToGet(Conversation post) throws IllegalArgumentException {
		if (!"POST".equals(post.getRequestMethod()))
			throw new IllegalArgumentException("Request method must be 'POST', was '" + post.getRequestMethod() + "'");
		
		NamedValue[] headers = post.getRequestHeaders();
		NamedValue contentType = NamedValue.findOne("Content-Type", headers);
		if (contentType == null || !"application/x-www-form-urlencoded".equals(contentType.getValue()))
			throw new IllegalArgumentException("Invalid 'Content-Type' header");

		Conversation get = new Conversation();
		get.setRequestMethod("GET");
		URI uri = post.getRequestUri();
		try {
			String query = new String(post.getProcessedRequestContent(), "ASCII");
			uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), query, uri.getFragment());
		} catch (URISyntaxException use) {
			throw new IllegalArgumentException("Invalid uri syntax : " + use.getMessage(), use);
        } catch (UnsupportedEncodingException uee) {
			throw new IllegalArgumentException("Weird! " + uee, uee);
		}

		get.setRequestUri(uri);
		get.setRequestVersion(post.getRequestVersion());
		
		headers = NamedValue.delete("Content-Type", headers);
		headers = NamedValue.delete("Content-Length", headers);
		get.setRequestHeaders(headers);
		
      	get.setProcessedRequestContent(null);
		return get;
	}
	
}
