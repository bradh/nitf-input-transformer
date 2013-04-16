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
import ddf.content.ContentFramework;
import ddf.content.operation.CreateRequest;
import ddf.content.operation.Request;
import ddf.content.operation.impl.CreateResponseImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class TestNITFAlterationListener {

  @Spy
  private NITFFileAlterationListener listener = new NITFFileAlterationListener();
  private static ContentFramework contentFramework;

  public NITFFileAlterationListener createListener() throws Exception {

    contentFramework = mock(ContentFramework.class);
    List<Metacard> metacards = new ArrayList<Metacard>();
    MetacardImpl metacard = new MetacardImpl();
    metacards.add(metacard);
    when(contentFramework.create(any(CreateRequest.class), eq(Request.Directive.STORE_AND_PROCESS))).thenReturn(new CreateResponseImpl(null, null));
    listener.setContentFramework(contentFramework);


    return listener;
  }

  @Test()
  public void testFileCreated() throws Exception {
    NITFFileAlterationListener transformer = createListener();
    String file = Thread.currentThread().getContextClassLoader().getResource("i_3001a.ntf").getFile();

    transformer.onFileCreate(new File(file));
    verify(contentFramework).create(any(CreateRequest.class),eq(Request.Directive.STORE_AND_PROCESS));
  }

  @Test()
  public void testFileNotCreated() throws Exception {
    NITFFileAlterationListener transformer = createListener();
    String file = Thread.currentThread().getContextClassLoader().getResource("notanitf.txt").getFile();

    transformer.onFileCreate(new File(file));
    verify(contentFramework, never()).create(any(CreateRequest.class), eq(Request.Directive.STORE_AND_PROCESS));
  }

}
