package com.google.gwt.sample.stockwatcher.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

// these methods match those defined in async
@RemoteServiceRelativePath("gnode")
public interface GnodeService extends RemoteService {
  public void addGnode(String symbol) throws NotLoggedInException;

void parseVnodes();

}