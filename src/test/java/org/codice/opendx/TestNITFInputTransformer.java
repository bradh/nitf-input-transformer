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

import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardImpl;
import ddf.catalog.federation.FederationException;
import ddf.catalog.operation.CreateRequest;
import ddf.catalog.operation.CreateResponseImpl;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.QueryResponseImpl;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.VFS;
import org.junit.Test;


import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotSame;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TestNITFInputTransformer {

  private static ddf.catalog.CatalogFramework catalog;

  public static NITFInputTransformer createTransformer() throws SourceUnavailableException, FederationException, IngestException {
    NITFInputTransformer transformer = new NITFInputTransformer();
    catalog = mock(ddf.catalog.CatalogFramework.class);
    List<Metacard> metacards = new ArrayList<Metacard>();
    MetacardImpl metacard = new MetacardImpl();
    metacards.add(metacard);
    when(catalog.create(any(CreateRequest.class))).thenReturn(new CreateResponseImpl(null, null, metacards));
    transformer.setCatalog(catalog);

    return transformer;
  }

  @Test()
  public void testFileCreated() throws Exception {
    NITFInputTransformer transformer = createTransformer();
    Thread.currentThread().getContextClassLoader().getResource("i_3001a.ntf");
    String file = Thread.currentThread().getContextClassLoader().getResource("i_3001a.ntf").getFile();

    transformer.fileCreated(new FileChangeEvent(VFS.getManager().resolveFile(file)));
    verify(catalog).create(any(CreateRequest.class));
  }

  @Test()
  public void testFileNotCreated() throws Exception {
    NITFInputTransformer transformer = createTransformer();
    Thread.currentThread().getContextClassLoader().getResource("i_3001a.ntf");
    String file = Thread.currentThread().getContextClassLoader().getResource("notanitf.txt").getFile();

    transformer.fileCreated(new FileChangeEvent(VFS.getManager().resolveFile(file)));
    verify(catalog, never()).create(any(CreateRequest.class));
  }

  @Test()
  public void testCreateThumbnail() throws Exception {
    NITFInputTransformer transformer = createTransformer();

    String tempJpeg = transformer.createThumbnail(Thread.currentThread().getContextClassLoader().getResource("i_3001a.ntf").getFile());
    assertNotSame(tempJpeg, "");
  }

  @Test()
  public void testEncodeThumbnail() throws Exception {
    NITFInputTransformer transformer = createTransformer();
    String tempJpeg = transformer.createThumbnail(Thread.currentThread().getContextClassLoader().getResource("i_3001a.ntf").getFile());
    assertNotSame(tempJpeg, "");
    String encoding = transformer.encodeThumbnailToBase64Binary(tempJpeg);
    assertEquals(encoding, getTestEncoding());
  }

  private String getTestEncoding(){
    return "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";
  }

}
