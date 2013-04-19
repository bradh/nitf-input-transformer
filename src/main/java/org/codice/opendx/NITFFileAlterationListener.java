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

package org.codice.opendx;


import ddf.content.ContentFramework;
import ddf.content.data.ContentItem;
import ddf.content.data.impl.IncomingContentItem;
import ddf.content.operation.CreateRequest;
import ddf.content.operation.CreateResponse;
import ddf.content.operation.Request;
import ddf.content.operation.impl.CreateRequestImpl;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;

public class NITFFileAlterationListener implements FileAlterationListener {


  private static final Logger log = Logger.getLogger(NITFFileAlterationListener.class);

  private ContentFramework contentFramework;

  public NITFFileAlterationListener() {
    log.info("Starting NITFFileAlterationListener");
  }



  @Override
  public void onStart(FileAlterationObserver fileAlterationObserver) {
    //NOOP
  }

  @Override
  public void onDirectoryCreate(File file) {
    //NOOP
  }

  @Override
  public void onDirectoryChange(File file) {
    //NOOP
  }

  @Override
  public void onDirectoryDelete(File file) {
    //NOOP
  }

  @Override
  public void onFileCreate(File file) {
    try {
      log.info("Processing file: " + FilenameUtils.getBaseName(file.getAbsolutePath()));
      if(!FilenameUtils.getExtension(file.getAbsolutePath()).toLowerCase().equals("nitf")
              && !FilenameUtils.getExtension(file.getAbsolutePath()).toLowerCase().equals("ntf")){
        log.info("Not processing file: " + FilenameUtils.getBaseName(file.getAbsolutePath()) + " extension doesn't match 'nitf' or 'ntf'");
        return;
      }

      //create content entry
      ContentItem newItem = new IncomingContentItem( new FileInputStream(file), "image/nitf", file.getName() );

      log.info("Creating content item...");

      CreateRequest createRequest = new CreateRequestImpl( newItem, null );
      CreateResponse createResponse = this.contentFramework.create( createRequest, Request.Directive.STORE_AND_PROCESS );
      ContentItem contentItem = createResponse.getCreatedContentItem();
      log.info("Created content item " + contentItem.getUri());


      log.info("Processing file: " + FilenameUtils.getBaseName(file.getAbsolutePath()) + " complete.");
    }catch (Exception e){
      log.error("Failed to ingest NITF File: ", e);
    }
  }

  @Override
  public void onFileChange(File file) {
    //TODO: update/delete metacard
  }

  @Override
  public void onFileDelete(File file) {
    log.info("File deleted: " + file.getPath());
  }

  @Override
  public void onStop(FileAlterationObserver fileAlterationObserver) {
    //NOOP
  }

  public void setContentFramework(ContentFramework contentFramework) {
    this.contentFramework = contentFramework;
  }
}
