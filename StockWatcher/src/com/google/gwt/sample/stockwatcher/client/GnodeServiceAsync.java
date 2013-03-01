package com.google.gwt.sample.stockwatcher.client;

import com.google.gwt.user.client.rpc.AsyncCallback;


// comment these out to remove from inheritance
public interface GnodeServiceAsync {
	public void addGnode(String symbol, AsyncCallback<Void> async);

	public void parseVnodes(AsyncCallback<Void> async);

}