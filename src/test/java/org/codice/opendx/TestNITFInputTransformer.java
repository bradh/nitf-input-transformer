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

}
