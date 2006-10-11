/**
 *
 */
package org.owasp.webscarab.domain;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * @author rdawes
 *
 */
public class StreamingConversation extends Conversation {

	protected CopyInputStream responseContentStream = null;

	protected ByteArrayOutputStream responseContent = null;

	public void setResponseContentStream(InputStream contentStream) {
		if (contentStream == null) {
			this.responseContentStream = null;
			this.responseContent = null;
		} else {
			this.responseContent = new ByteArrayOutputStream();
			this.responseContentStream = new CopyInputStream(contentStream,
					responseContent);
		}
	}

	public InputStream getResponseContentStream() {
		return responseContentStream;
	}

	public byte[] getResponseContent() {
		if (responseContentStream != null) {
			flushContentStream(responseContentStream);
			this.responseContentStream = null;
		}
		if (this.responseContent == null)
			return null;
		return this.responseContent.toByteArray();
	}

	private void flushContentStream(InputStream is) {
		if (is != null) {
			try {
				byte[] buff = new byte[2048];
				while (is.read(buff) > 0)
					; // flush whatever was in the stream
			} catch (IOException ioe) {
			} // ignore any errors
		}
	}

	private class CopyInputStream extends FilterInputStream {

		private OutputStream os;

		public CopyInputStream(InputStream is, OutputStream os) {
			super(is);
			this.os = os;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.io.FilterInputStream#read()
		 */
		@Override
		public int read() throws IOException {
			int result = super.read();
			if (result > -1) {
				os.write(result);
				os.flush();
			} else {
				// we close to signal downstream readers that the inputstream
				// has closed
				os.close();
			}
			return result;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.io.FilterInputStream#read(byte[], int, int)
		 */
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int num = super.read(b, off, len);
			if (num > 0) {
				os.write(b, off, num);
				os.flush();
			} else {
				os.close();
			}
			return num;
		}
	}

}
