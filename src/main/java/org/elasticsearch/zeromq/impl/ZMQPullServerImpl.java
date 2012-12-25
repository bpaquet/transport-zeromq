package org.elasticsearch.zeromq.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.zeromq.ZMQRestImpl;
import org.elasticsearch.zeromq.ZMQServerTransport;
import org.elasticsearch.zeromq.ZMQSocket;
import org.zeromq.ZMQ;

public class ZMQPullServerImpl extends AbstractLifecycleComponent<ZMQServerTransport> implements
ZMQServerTransport {

	private String address;
	
	private final ZMQ.Context context;
	
	private ZMQ.Socket socket;
	
	private Thread worker;
	
	private final ZMQRestImpl client;
	
	private final AtomicBoolean isRunning;
	
	@Inject
	protected ZMQPullServerImpl(Settings settings, ZMQRestImpl client) {
		super(settings);
		this.client = client;
		
		logger.debug("Reading ZeroMQ transport layer settings...");

		address = settings.get("zeromq.bind", "tcp://127.0.0.1:9700");
		
		logger.debug("ZeroMQ settings [zeromq.bind={}]", address);

		logger.info("Creating ZeroMQ server context...");
		context = ZMQ.context(1);
		
		isRunning = new AtomicBoolean(true);
		
		logger.info("ZeroMQ context ready");
	}
	
	@Override
	protected void doClose() throws ElasticSearchException {
		logger.info("Closing ZeroMQ server...");
		
		isRunning.set(false);

        // Stops the worker
		logger.info("Interrupt ZeroMQ worker thread listening on {}", address);
		worker.interrupt();
        
        // Stop the zmq context
        // All socket must be stop from another thread
		context.term();
		logger.info("ZeroMQ context closed");
	}

	@Override
	protected void doStart() throws ElasticSearchException {
		logger.debug("Starting ZeroMQ pull socket...");
		socket = context.socket(ZMQ.PULL);
	
        socket.bind(address);
        
        worker = new Thread(new ZMQSocket(logger, socket, client, isRunning));
        
        worker.start();
        
        logger.info("ZeroMQ socket ready on {}", address);
	}

	@Override
	protected void doStop() throws ElasticSearchException {
		logger.debug("Stopping ZeroMQ server...");
	}

}
