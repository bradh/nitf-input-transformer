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


import com.vividsolutions.jts.algorithm.CentroidPoint;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardImpl;
import ddf.catalog.operation.CreateRequestImpl;
import ddf.catalog.operation.CreateResponse;
import joms.oms.DataInfo;
import joms.oms.Init;
import joms.oms.Util;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.log4j.Logger;
import org.codice.opendx.utility.ThumbnailCreationException;
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
import java.io.*;
import java.util.Date;
import java.util.UUID;

public class NITFInputTransformer implements FileAlterationListener {

  static { /* works fine! ! */
    System.setProperty("java.awt.headless", "true");
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

  private String getPosition(Document info) throws ParseException {
    log.info("Original position data: " + info.getElementsByTagName("groundGeom").item(0).getFirstChild().getTextContent());
    Geometry geometry = new WKTReader().read(info.getElementsByTagName("groundGeom").item(0).getFirstChild().getTextContent());
    return new WKTWriter().write(geometry.getCentroid());
  }

  private String getTitle(Document info){
    return info.getElementsByTagName("ftitle").item(0).getFirstChild().getTextContent();
  }

  private String getNITF(Document info) throws ParserConfigurationException, IOException, SAXException {
    return toString(info.getElementsByTagName("NITF").item(0));
  }

  private Metacard buildMetacard(String title, String location, String metadata, String thumbnail){
    return buildMetacard(title, location, metadata, thumbnail.getBytes());
  }

  private Metacard buildMetacard(String title, String location, String metadata, byte [] thumbnail){
    MetacardImpl metacard = new MetacardImpl();
    metacard.setId(UUID.randomUUID().toString());
    metacard.setTitle( title );

    metacard.setContentTypeName("image/jpeg");
    metacard.setModifiedDate(new Date());

    metacard.setLocation(location);
    metacard.setMetadata(metadata);
    metacard.setThumbnail(thumbnail);
    metacard.setCreatedDate(new Date());
    metacard.setModifiedDate(new Date());

    return metacard;
  }

  String createThumbnail(String nitfPath) throws ThumbnailCreationException {
    File tempDirectory = new File(FileUtils.getTempDirectoryPath());
    String jpeg = UUID.randomUUID().toString() + ".jpeg";

    int entryId = 0;
    String outputFile = tempDirectory.getAbsolutePath() + jpeg;
    String writerType = "image/jpeg";
    int xRes = Integer.parseInt("32");
    int yRes = Integer.parseInt("32");
    String histogramFile = "";
    String stretchType = "linear_auto_min_max";
    boolean keepAspectFlag = true;

    boolean status = Util.writeImageSpaceThumbnail(
            nitfPath, entryId, outputFile, writerType, xRes, yRes, histogramFile, stretchType, keepAspectFlag
    );

    if(status)
      return outputFile;
    else
      throw new ThumbnailCreationException("Failed to created thumbnail " + outputFile + " from nitf " + nitfPath);
  }

  String encodeThumbnailToBase64Binary(String fileName)
          throws IOException {

    File file = new File(fileName);
    byte[] bytes = loadFile(file);
    byte[] encoded = Base64.encodeBase64(bytes);
    String encodedString = new String(encoded);

    return encodedString;
  }

  private static byte[] loadFile(File file) throws IOException {
    InputStream is = new FileInputStream(file);

    long length = file.length();
    if (length > Integer.MAX_VALUE) {
      new ThumbnailCreationException("Thumbnail is too large: " + length);
    }
    byte[] bytes = new byte[(int)length];

    int offset = 0;
    int numRead = 0;
    while (offset < bytes.length
            && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
      offset += numRead;
    }

    if (offset < bytes.length) {
      throw new IOException("Could not completely read file "+file.getName());
    }

    is.close();
    return bytes;
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
      if(!FilenameUtils.getExtension(file.getAbsolutePath()).equals("nitf") && !FilenameUtils.getExtension(file.getAbsolutePath()).equals("ntf")){
        log.info("Not processing file: " + FilenameUtils.getBaseName(file.getAbsolutePath()) + " extension doesn't match 'nitf' or 'ntf'");
        return;
      }

      DataInfo dataInfo = new DataInfo();
      dataInfo.open(file.getPath());

      String info = dataInfo.getInfo();
      String position = getPosition(buildDocument(info));
      String title = getTitle(buildDocument(info));
      String nitf = getNITF(buildDocument(info));
      byte [] thumbnail = loadFile(new File(createThumbnail(file.getPath())));
      log.info("Creating metacard with title: " + title +
              "," +
              " position: " + position +
              "," +
              " nitf: " + nitf +
              " and" +
              " thumbnail: " + (new String(thumbnail))
              );

      dataInfo.close();



      CreateResponse response = this.catalog.create(new CreateRequestImpl( buildMetacard(title,
              position,
              nitf, thumbnail)));

      assert(response.getCreatedMetacards().size() == 1);
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
    //TODO: update/delete metacard
  }

  @Override
  public void onStop(FileAlterationObserver fileAlterationObserver) {
    //NOOP
  }


  public void setCatalog(CatalogFramework catalog) {
    this.catalog = catalog;
  }
}
