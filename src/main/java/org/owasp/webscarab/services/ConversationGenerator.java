/**
 *
 */
package org.owasp.webscarab.services;

import org.owasp.webscarab.domain.Conversation;

/**
 * @author rdawes
 *
 */
public interface ConversationGenerator {

	Conversation getNextRequest();

	void responseReceived(Conversation conversation);

	void errorFetchingResponse(Conversation request, Exception e);

}
