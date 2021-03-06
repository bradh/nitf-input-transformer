/**
 * Copyright (c) Lockheed Martin Corporation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version. 
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 *
 **/

package org.codice.opendx.utility;


import joms.oms.Init;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.log4j.Logger;
import org.codice.opendx.NITFFileAlterationListener;

import java.io.File;


public class NITFDirectoryWatcher {
  private String path;
  private FileAlterationMonitor monitor;
  private NITFFileAlterationListener alterationListener;

  private static final Logger log = Logger.getLogger(NITFDirectoryWatcher.class);


  public void init() throws Exception {
    log.info("Starting NITFDirectoryWatcher");

    File directory = new File(path);
    try{
      directory.mkdirs();
    }catch(Exception e){
      log.warn("Unable to create nitf target directory: " + directory.getPath(), e);
    }

    FileAlterationObserver observer = new FileAlterationObserver(directory);

    observer.addListener(alterationListener);

    long interval = 5000l;
    monitor = new FileAlterationMonitor(interval);
    monitor.addObserver(observer);
    monitor.start();
  }

  public void destroy() throws Exception{
    log.info("Stopping NITFDirectoryWatcher");
    Init.instance().delete();
    monitor.stop();
  }

  public void setPath(String path){
    this.path = path;
  }

  public void setAlterationListener(NITFFileAlterationListener alterationListener) {
    this.alterationListener = alterationListener;
  }
}