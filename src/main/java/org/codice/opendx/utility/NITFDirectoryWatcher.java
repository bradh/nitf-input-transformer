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

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.apache.log4j.Logger;
import org.codice.opendx.NITFInputTransformer;

import java.io.IOException;
import java.net.URISyntaxException;






public class NITFDirectoryWatcher {
  private String path;
  private FileSystemManager fileSystemManager;
  private static final Logger log = Logger.getLogger(NITFDirectoryWatcher.class);

  public NITFDirectoryWatcher() throws FileSystemException {
    fileSystemManager = VFS.getManager();
  }

  public void init() throws IOException, URISyntaxException {
    log.info("Starting NITFDirectoryWatcher");

    FileObject directory = fileSystemManager.resolveFile("C:/naruto");

    NITFInputTransformer listener = new NITFInputTransformer();

    DefaultFileMonitor fileMonitor = new DefaultFileMonitor(listener);
    fileMonitor.setRecursive(false);
    fileMonitor.addFile(directory);
    fileMonitor.start();
  }

  private void setPath(String path){
    this.path = path;
  }
}