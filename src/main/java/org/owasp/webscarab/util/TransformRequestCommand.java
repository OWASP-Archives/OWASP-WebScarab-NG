/**
 * 
 */
package org.owasp.webscarab.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;

import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.NamedValue;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.command.ActionCommand;

/**
 * @author rdawes
 *
 */
public abstract class TransformRequestCommand extends ActionCommand {

	private static final String[] properties = {
		Conversation.PROPERTY_REQUEST_METHOD,
		Conversation.PROPERTY_REQUEST_URI,
		Conversation.PROPERTY_REQUEST_VERSION,
		Conversation.PROPERTY_REQUEST_HEADERS,
		Conversation.PROPERTY_REQUEST_PROCESSED_CONTENT,
		Conversation.PROPERTY_REQUEST_CONTENT };
	
	private FormModel model;
	
	private PropertyChangeListener  listener;
	
	protected TransformRequestCommand(String commandId, FormModel model) {
		super(commandId);
		this.model = model;
		setEnabled(shouldEnable());
		addListener();
	}

	private void addListener() {
		listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
            	setEnabled(shouldEnable());
            }
		};
		for (int i=0; i<properties.length; i++)
			model.getValueModel(properties[i]).addValueChangeListener(listener);
	}
	
	protected Conversation getRequest() {
		Conversation request = new Conversation();
		request.setRequestMethod(getMethod());
		request.setRequestUri(getUri());
		request.setRequestVersion(getVersion());
		request.setRequestHeaders(getHeaders());
		request.setProcessedRequestContent(getProcessedContent());
		return request;
	}
	
	protected void setRequest(Conversation request) {
		setMethod(request.getRequestMethod());
		setURI(request.getRequestUri());
		setMethod(request.getRequestMethod());
		setHeaders(request.getRequestHeaders());
		setProcessedContent(request.getProcessedRequestContent());
		setEnabled(shouldEnable());
	}
	
	protected String getMethod() {
		return (String) model.getValueModel(Conversation.PROPERTY_REQUEST_METHOD).getValue();
	}
	
	protected void setMethod(String method) {
		model.getValueModel(Conversation.PROPERTY_REQUEST_METHOD).setValueSilently(method, listener);
	}
	
	protected URI getUri() {
		return (URI) model.getValueModel(Conversation.PROPERTY_REQUEST_URI).getValue();
	}
	
	protected void setURI(URI uri) {
		model.getValueModel(Conversation.PROPERTY_REQUEST_URI).setValueSilently(uri, listener);
	}
	
	protected String getVersion() {
		return (String) model.getValueModel(Conversation.PROPERTY_REQUEST_VERSION).getValue();
	}
	
	protected void setVersion(String version) {
		model.getValueModel(Conversation.PROPERTY_REQUEST_VERSION).setValueSilently(version, listener);
	}
	
	protected NamedValue[] getHeaders() {
		return (NamedValue[]) model.getValueModel(Conversation.PROPERTY_REQUEST_HEADERS).getValue();
	}
	
	protected void setHeaders(NamedValue[] headers) {
		model.getValueModel(Conversation.PROPERTY_REQUEST_HEADERS).setValueSilently(headers, listener);
	}
	
	protected byte[] getProcessedContent() {
		return (byte[]) model.getValueModel(Conversation.PROPERTY_REQUEST_PROCESSED_CONTENT).getValue();
	}
	
	protected void setProcessedContent(byte[] content) {
		model.getValueModel(Conversation.PROPERTY_REQUEST_PROCESSED_CONTENT).setValueSilently(content, listener);
	}
	
	/**
	 * @return true if the command should be enabled
	 */
	protected abstract boolean shouldEnable();
	
    public static TransformRequestCommand createGetToPost(FormModel model) {
    	return new TransformRequestCommand("requestTransformGetToPost", model) {

			/* (non-Javadoc)
             * @see org.springframework.richclient.command.ActionCommand#doExecuteCommand()
             */
            @Override
            protected void doExecuteCommand() {
            	try {
            		setRequest(RequestConverter.convertGetToPost(getRequest()));
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            }

			/* (non-Javadoc)
             * @see org.owasp.webscarab.util.TransformRequestCommand#shouldEnable()
             */
            @Override
            protected boolean shouldEnable() {
            	try {
            		RequestConverter.convertGetToPost(getRequest());
            		return true;
            	} catch (IllegalArgumentException iae) {
            		return false;
            	}
            }
    		
    	};
    }

    public static TransformRequestCommand createPostToMultipartPost(FormModel model) {
    	return new TransformRequestCommand("requestTransformPostToMultipartPost", model) {

			/* (non-Javadoc)
             * @see org.springframework.richclient.command.ActionCommand#doExecuteCommand()
             */
            @Override
            protected void doExecuteCommand() {
            	try {
            		setRequest(RequestConverter.convertPostToMultipartPost(getRequest()));
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            }

			/* (non-Javadoc)
             * @see org.owasp.webscarab.util.TransformRequestCommand#shouldEnable()
             */
            @Override
            protected boolean shouldEnable() {
            	try {
            		RequestConverter.convertPostToMultipartPost(getRequest());
            		return true;
            	} catch (IllegalArgumentException iae) {
            		return false;
            	}
            }
    		
    	};
    }
    
    public static TransformRequestCommand createPostToGet(FormModel model) {
    	return new TransformRequestCommand("requestTransformPostToGet", model) {

			/* (non-Javadoc)
             * @see org.springframework.richclient.command.ActionCommand#doExecuteCommand()
             */
            @Override
            protected void doExecuteCommand() {
            	try {
            		setRequest(RequestConverter.convertPostToGet(getRequest()));
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            }

			/* (non-Javadoc)
             * @see org.owasp.webscarab.util.TransformRequestCommand#shouldEnable()
             */
            @Override
            protected boolean shouldEnable() {
            	try {
            		RequestConverter.convertPostToGet(getRequest());
            		return true;
            	} catch (IllegalArgumentException iae) {
            		return false;
            	}
            }
    		
    	};
    }

}
