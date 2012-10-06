/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.zeromq;

import static org.elasticsearch.common.util.concurrent.EsExecutors.daemonThreadFactory;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;

/**
 * @author tlrx
 */
public class ZMQServer extends AbstractLifecycleComponent<ZMQServer> {

	private volatile ZMQServerTransport transport;

	@Inject
	public ZMQServer(Settings settings, ZMQServerTransport transport, ZMQRestImpl client) {
		super(settings);
		this.transport = transport;
	}

	@Override
	protected void doStart() throws ElasticSearchException {

		logger.debug("Starting Zeromq server...");
		daemonThreadFactory(settings, "zeromq_server").newThread(
				new Runnable() {
					@Override
                    public void run() {
                        transport.start();
                    }
		}).start();
	}



	@Override
	protected void doStop() throws ElasticSearchException {
	    transport.stop();
	}

	@Override
	protected void doClose() throws ElasticSearchException {
		transport.close();
	}
}
