package org.elasticsearch.zeromq;

import java.util.concurrent.atomic.AtomicBoolean;

import org.elasticsearch.common.logging.ESLogger;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

/**
 * @author tlrx
 * 
 */
public class ZMQSocket implements Runnable {

	public final static String SEPARATOR = "|";

	private final ESLogger logger;

	private ZMQ.Socket socket;

	private final ZMQRestImpl client;
	
	private final AtomicBoolean isRunning;

	public ZMQSocket(ESLogger logger, ZMQ.Socket socket, ZMQRestImpl client, AtomicBoolean isRunning) {
		super();
		this.socket = socket;
		this.logger = logger;
		this.client = client;
		this.isRunning = isRunning;
	}

	@Override
	public void run() {
		while (true) {

			// Reads all parts of the message
			byte[] lastPart = null;

			try {

				do {
					lastPart = socket.recv(0);
				} while (socket.hasReceiveMore());

			} catch (ZMQException zmqe) {
				if (!isRunning.get()) {
					socket.close();
			        logger.info("ZeroMQ socket closed");
					return;
				}
				// Close the socket
				if (logger.isWarnEnabled()) {
					logger.warn("Exception when receiving message", zmqe);
				}
			}

			if (lastPart != null) {
				// Payload
				String payload = new String(lastPart);
	
				if (logger.isDebugEnabled()) {
					logger.debug("ZeroMQ socket receives message: {}", payload);
				}
	
				try {
					// Construct an ES request
					ZMQRestRequest request = new ZMQRestRequest(payload);
	
					// Process the request
					client.process(request);
				} catch (Exception e) {
					if (logger.isErrorEnabled()) {
						logger.error("Exception when processing ZeroMQ message", e);
					}
				}
			}
		}
	}
}
