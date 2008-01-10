/**
 *
 */
package org.owasp.webscarab.ui.rcp.forms.support;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.NamedValue;
import org.springframework.binding.validation.ValidationResults;
import org.springframework.binding.validation.Validator;
import org.springframework.binding.validation.support.DefaultValidationResults;
import org.springframework.richclient.core.Severity;

/**
 * @author rdawes
 *
 */
public class ConversationValidator {

	public static final Validator REQUEST_VALIDATOR = new RequestValidator();
	public static final Validator RESPONSE_VALIDATOR = new ResponseValidator();

	private ConversationValidator() {}

	private static class RequestValidator implements Validator {

		private Set<String> methods = new HashSet<String>(Arrays.asList(new String[] {
				"GET", "POST", "HEAD", "TRACE", "OPTIONS"
			}));

		private Set<String> versions = new HashSet<String>(Arrays.asList(new String[] {
				"HTTP/1.0", "HTTP/1.1"
			}));

		public ValidationResults validate(Object object) {
			DefaultValidationResults results = new DefaultValidationResults();
			Conversation c = (Conversation) object;
			String method = c.getRequestMethod();
			if (method == null || method.equals("")) {
				results.addMessage(Conversation.PROPERTY_REQUEST_METHOD, Severity.ERROR, "Request method cannot be empty");
			} else if (!methods.contains(method)) {
				results.addMessage(Conversation.PROPERTY_REQUEST_METHOD, Severity.ERROR, "Unknown request method");
			}
			URI uri = c.getRequestUri();
			if (uri == null) {
				results.addMessage(Conversation.PROPERTY_REQUEST_URI, Severity.ERROR, "URI cannot be null");
			} else if (!uri.getScheme().equals("http") && !uri.getScheme().equals("https")) {
				results.addMessage(Conversation.PROPERTY_REQUEST_URI, Severity.ERROR, "Invalid URI Scheme");
			}
			String version = c.getRequestVersion();
			if (version == null || version.equals("")) {
				results.addMessage(Conversation.PROPERTY_REQUEST_VERSION, Severity.ERROR, "Request version cannot be empty");
			} else if (!versions.contains(version)) {
				results.addMessage(Conversation.PROPERTY_REQUEST_VERSION, Severity.ERROR, "Invalid request version");
			}
			NamedValue[] headers = c.getRequestHeaders();
			boolean hostRequired = version != null && version.equals("HTTP/1.1");
			if (headers != null)
				for (int i=0; i<headers.length; i++) {
					if (headers[i].getName() == null || headers[i].getName().equals("")) {
						results.addMessage(Conversation.PROPERTY_REQUEST_HEADERS, Severity.ERROR, "Header name may not be empty");
					} else if (headers[i].getValue() == null || headers[i].getValue().equals("")) {
						results.addMessage(Conversation.PROPERTY_REQUEST_HEADERS, Severity.ERROR, "Header value may not be empty");
					} else if (headers[i].getName().equalsIgnoreCase("host")) {
						hostRequired = false;
						if (uri != null && !uri.getHost().equalsIgnoreCase(headers[i].getValue()))
							results.addMessage(Conversation.PROPERTY_REQUEST_HEADERS, Severity.WARNING, "Host header does not match URI");
					}
				}
			if (hostRequired)
				results.addMessage(Conversation.PROPERTY_REQUEST_HEADERS, Severity.WARNING, "The selected HTTP version requires a Host header");

			byte[] content = c.getRequestContent();
			if ("GET".equals(method) && content != null && content.length > 0)
				results.addMessage(Conversation.PROPERTY_REQUEST_CONTENT, Severity.ERROR, "GET Requests cannot have a body");
			System.out.println(results);
			return results;
		}

	}

	private static class ResponseValidator implements Validator {

		private Set<String> versions = new HashSet<String>(Arrays.asList(new String[] {
				"HTTP/1.0", "HTTP/1.1"
			}));

		public ValidationResults validate(Object object) {
			DefaultValidationResults results = new DefaultValidationResults();
			Conversation c = (Conversation) object;
			String version = c.getResponseVersion();
			if (version == null || version.equals("")) {
				results.addMessage(Conversation.PROPERTY_RESPONSE_VERSION, Severity.ERROR, "Response version cannot be null");
			} else if (!versions.contains(version)) {
				results.addMessage(Conversation.PROPERTY_RESPONSE_VERSION, Severity.ERROR, "Invalid response version");
			}
			String status = c.getResponseStatus();
			if (status == null || status.equals("")) {
				results.addMessage(Conversation.PROPERTY_RESPONSE_STATUS, Severity.ERROR, "Response status cannot be empty");
			} else if (!status.matches("\\d\\d\\d")) {
				results.addMessage(Conversation.PROPERTY_REQUEST_VERSION, Severity.ERROR, "Invalid response status");
			}
			String message = c.getResponseMessage();
			if (message == null || message.equals("")) {
				results.addMessage(Conversation.PROPERTY_RESPONSE_STATUS, Severity.ERROR, "Response message cannot be empty");
			}

			NamedValue[] headers = c.getResponseHeaders();
			if (headers != null)
				for (int i=0; i<headers.length; i++) {
					if (headers[i].getName() == null || headers[i].getName().equals("")) {
						results.addMessage(Conversation.PROPERTY_RESPONSE_HEADERS, Severity.ERROR, "Header name may not be empty");
					} else if (headers[i].getValue() == null || headers[i].getValue().equals("")) {
						results.addMessage(Conversation.PROPERTY_RESPONSE_HEADERS, Severity.ERROR, "Header value may not be empty");
					}
				}

			return results;
		}
	}
}
