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


import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardImpl;
import ddf.catalog.operation.CreateRequestImpl;
import ddf.catalog.operation.CreateResponse;
import joms.oms.DataInfo;
import joms.oms.Init;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

public class NITFInputTransformer implements FileListener {

  static { /* works fine! ! */
    System.setProperty("java.awt.headless", "true");
    System.out.println(java.awt.GraphicsEnvironment.isHeadless());
      /* ---> prints true */
  }

  private static final Logger log = Logger.getLogger(NITFInputTransformer.class);

  private CatalogFramework catalog;

  public NITFInputTransformer() {
    Init.instance().initialize();
  }

  private static String toString(Node doc) {
    if(doc == null)
      return "";

    try {
      StringWriter sw = new StringWriter();
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

      transformer.transform(new DOMSource(doc), new StreamResult(sw));
      return sw.toString();
    } catch (Exception ex) {
      throw new RuntimeException("Error converting to String", ex);
    }
  }

  private Document buildDocument(String info) throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();

    return builder.parse(IOUtils.toInputStream(info));
  }

  private String getPosition(Document info){
    return info.getElementsByTagName("groundGeom").item(0).getFirstChild().getTextContent();
  }

  private String getTitle(Document info){
    return info.getElementsByTagName("ftitle").item(0).getFirstChild().getTextContent();
  }

  private String getNITF(Document info) throws ParserConfigurationException, IOException, SAXException {
    return toString(info.getElementsByTagName("NITF").item(0));
  }

  private Metacard buildMetacard(String title, String location, String metadata ){
    MetacardImpl metacard = new MetacardImpl();

    metacard.setTitle( title );

    metacard.setContentTypeName("image/nitf");
    metacard.setModifiedDate(new Date());

    metacard.setLocation(location);
    metacard.setMetadata(metadata);

    return metacard;
  }

  @Override
  public void fileCreated(FileChangeEvent fileChangeEvent) throws Exception {
    FileObject file = fileChangeEvent.getFile();
    if(!file.getName().getExtension().equals("nitf") && !file.getName().getExtension().equals("ntf")){
      System.out.println(file.getName().getExtension());
      return;
    }

    DataInfo dataInfo = new DataInfo();
    dataInfo.open(file.getName().getPath());

    String info = dataInfo.getInfo();
    dataInfo.close();

    CreateResponse response = this.catalog.create(new CreateRequestImpl(buildMetacard(getTitle(buildDocument(info)),
            getPosition(buildDocument(info)),
            getNITF(buildDocument(info)))));

    assert(response.getCreatedMetacards().size() == 1);
  }

  @Override
  public void fileDeleted(FileChangeEvent fileChangeEvent) throws Exception {
    //TODO: remove metacard
  }

  @Override
  public void fileChanged(FileChangeEvent fileChangeEvent) throws Exception {
    //TODO: update metacard
  }

  public void setCatalog(CatalogFramework catalog) {
    this.catalog = catalog;
  }
}
