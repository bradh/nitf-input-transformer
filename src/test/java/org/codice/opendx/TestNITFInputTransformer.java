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
import ddf.catalog.operation.CreateRequest;
import ddf.catalog.operation.CreateResponseImpl;
import joms.oms.DataInfo;
import joms.oms.Init;
import joms.oms.Util;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Init.class, DataInfo.class, Util.class})
@SuppressStaticInitializationFor("joms.oms.jomsJNI")
public class TestNITFInputTransformer {

  @Spy
  private NITFInputTransformer transformer = new NITFInputTransformer();
  private static ddf.catalog.CatalogFramework catalog;

  public NITFInputTransformer createTransformer() throws Exception {
    PowerMockito.mockStatic(Init.class);
    PowerMockito.mockStatic(DataInfo.class);
    PowerMockito.mockStatic(Util.class);

    DataInfo dataInfo = mock(DataInfo.class);
    doReturn(getInfo()).when(dataInfo).getInfo();

    PowerMockito.whenNew(DataInfo.class).withNoArguments().thenThrow(new IOException("error message"));


    Mockito.when(Util.writeImageSpaceThumbnail(
            any(String.class), any(Integer.class), any(String.class), any(String.class), any(Integer.class), any(Integer.class), any(String.class), any(String.class), any(Boolean.class)
    )).thenReturn(true);

    doReturn(getThumbnailBytes()).when(transformer).loadFile(any(File.class));
    doReturn(dataInfo).when(transformer).dataInfoForFile(anyString());

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

    transformer.onFileCreate(new File(file));
    verify(catalog).create(any(CreateRequest.class));
  }

  @Test()
  public void testFileNotCreated() throws Exception {
    NITFInputTransformer transformer = createTransformer();
    Thread.currentThread().getContextClassLoader().getResource("i_3001a.ntf");
    String file = Thread.currentThread().getContextClassLoader().getResource("notanitf.txt").getFile();

    transformer.onFileCreate(new File(file));
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

  private static String getTestEncoding(){
    return "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";
  }

  private static byte[] getThumbnailBytes() throws IOException {
    DataInputStream is = new DataInputStream(new FileInputStream(Thread.currentThread().getContextClassLoader().getResource("binfile.dat").getFile()));
    byte [] thumbnail = new byte[is.available()];
    is.readFully(thumbnail);
    is.close();
    return thumbnail;
  }

  private static String getInfo() {
    return "<oms>\n" +
            "   <dataSets>\n" +
            "      <RasterDataSet>\n" +
            "         <fileObjects>\n" +
            "            <RasterFile type=\"main\" format=\"nitf\">\n" +
            "                <name>/Users/kwplummer/Documents/Workspace/nitf-input-transformer/target/test-classes/i_3001a.ntf</name>\n" +
            "            </RasterFile>\n" +
            "         </fileObjects>\n" +
            "         <rasterEntries>\n" +
            "            <RasterEntry>\n" +
            "               <entryId>0</entryId>\n" +
            "               <width>1024</width>\n" +
            "               <height>1024</height>\n" +
            "               <numberOfBands>1</numberOfBands>\n" +
            "               <numberOfResLevels>1</numberOfResLevels>\n" +
            "               <bitDepth>8</bitDepth>\n" +
            "               <dataType>uint</dataType>\n" +
            "               <gsd unit=\"meters\" dx=\"0.0301635883183536\" dy=\"0.0301635883183522\"/>\n" +
            "               <groundGeom srs=\"epsg:4326\">POLYGON((85.0000001356337 32.9833331976997,85.0002776421441 32.9833331976997,85.0002776421441 32.9830556911892,85.0000001356337 32.9830556911892,85.0000001356337 32.9833331976997))</groundGeom>\n" +
            "               <TiePointSet version='1'>\n" +
            "                  <Image>\n" +
            "                     <coordinates>0,0 1023,0 1023,1023 0,1023</coordinates>\n" +
            "                  </Image>\n" +
            "                  <Ground>\n" +
            "                     <coordinates>85.0000001356337,32.9833331976997 85.0002776421441,32.9833331976997 85.0002776421441,32.9830556911892 85.0000001356337,32.9830556911892</coordinates>\n" +
            "                  </Ground>\n" +
            "               </TiePointSet>\n" +
            "               <metadata>\n" +
            "                  <filename>/Users/kwplummer/Documents/Workspace/nitf-input-transformer/target/test-classes/i_3001a.ntf</filename>\n" +
            "                  <imageId></imageId>\n" +
            "                  <imageRepresentation>MONO    </imageRepresentation>\n" +
            "                  <targetId></targetId>\n" +
            "                  <productId></productId>\n" +
            "                  <beNumber>          </beNumber>\n" +
            "                  <sensorId></sensorId>\n" +
            "                  <missionId>Unknown                                   </missionId>\n" +
            "                  <countryCode></countryCode>\n" +
            "                  <imageCategory>VIS     </imageCategory>\n" +
            "                  <azimuthAngle>0</azimuthAngle>\n" +
            "                  <grazingAngle></grazingAngle>\n" +
            "                  <securityClassification>U</securityClassification>\n" +
            "                  <title>- BASE IMAGE -                                                                  </title>\n" +
            "                  <organization></organization>\n" +
            "                  <description></description>\n" +
            "                  <niirs></niirs>\n" +
            "\n" +
            "   <NITF>\n" +
            "      <abpp>08</abpp>\n" +
            "      <bmrlnth>0</bmrlnth>\n" +
            "      <clevel>03</clevel>\n" +
            "      <comrat>    </comrat>\n" +
            "      <encryp>0</encryp>\n" +
            "      <fdt>19971217102630</fdt>\n" +
            "      <fhdr>NITF02.10</fhdr>\n" +
            "      <fsclas>U</fsclas>\n" +
            "      <fscop>00000</fscop>\n" +
            "      <fscpys>00000</fscpys>\n" +
            "      <ftitle>Checks an uncompressed 1024x1024 8 bit mono image with GEOcentric data. Airfield</ftitle>\n" +
            "      <ialvl>000</ialvl>\n" +
            "      <ic>NC</ic>\n" +
            "      <icat>VIS     </icat>\n" +
            "      <icom/>\n" +
            "      <icords>G</icords>\n" +
            "      <idatim>19961217102630</idatim>\n" +
            "      <idlvl>001</idlvl>\n" +
            "      <ifc001>N</ifc001>\n" +
            "      <igeolo>325900N0850000E325900N0850001E325859N0850001E325859N0850000E</igeolo>\n" +
            "      <iid1>Missing ID</iid1>\n" +
            "      <iid2>- BASE IMAGE -                                                                  </iid2>\n" +
            "      <iloc>0000000000</iloc>\n" +
            "      <im>IM</im>\n" +
            "      <imag>1.0 </imag>\n" +
            "      <imdatoff>0</imdatoff>\n" +
            "      <imflt001/>\n" +
            "      <imode>B</imode>\n" +
            "      <irep>MONO    </irep>\n" +
            "      <irepband001>M </irepband001>\n" +
            "      <iscatp> </iscatp>\n" +
            "      <iscaut>                                        </iscaut>\n" +
            "      <isclas>U</isclas>\n" +
            "      <isclsy>  </isclsy>\n" +
            "      <iscltx>                                           </iscltx>\n" +
            "      <iscode>           </iscode>\n" +
            "      <iscrsn> </iscrsn>\n" +
            "      <isctlh>  </isctlh>\n" +
            "      <isctln>               </isctln>\n" +
            "      <isdcdt>        </isdcdt>\n" +
            "      <isdctp>  </isdctp>\n" +
            "      <isdcxm>    </isdcxm>\n" +
            "      <isdg> </isdg>\n" +
            "      <isdgdt>        </isdgdt>\n" +
            "      <isorce>Unknown                                   </isorce>\n" +
            "      <isrel>                    </isrel>\n" +
            "      <issrdt>        </issrdt>\n" +
            "      <isubcat001/>\n" +
            "      <isync>0</isync>\n" +
            "      <ixshdl>00000</ixshdl>\n" +
            "      <ixsofl>   </ixsofl>\n" +
            "      <nbands>1</nbands>\n" +
            "      <nbpc>0001</nbpc>\n" +
            "      <nbpp>08</nbpp>\n" +
            "      <nbpr>0001</nbpr>\n" +
            "      <ncols>00001024</ncols>\n" +
            "      <neluts001/>\n" +
            "      <nicom>0</nicom>\n" +
            "      <nluts001>0</nluts001>\n" +
            "      <nppbh>1024</nppbh>\n" +
            "      <nppbv>1024</nppbv>\n" +
            "      <nrows>00001024</nrows>\n" +
            "      <ostaid>i_3001a   </ostaid>\n" +
            "      <pjust>R</pjust>\n" +
            "      <pvtype>INT</pvtype>\n" +
            "      <stype>BF01</stype>\n" +
            "      <tgtid>                 </tgtid>\n" +
            "      <type>ossimNitfFileHeaderV2_1</type>\n" +
            "      <udidl>00000</udidl>\n" +
            "      <udofl>000</udofl>\n" +
            "      <xbands>     </xbands>\n" +
            "   </NITF>\n" +
            "\n" +
            "                  <fileType>nitf</fileType>\n" +
            "                  <className>ossimNitfTileSource</className>\n" +
            "               </metadata>\n" +
            "<TimeStamp>   <when>1996-12-17T10:26:30Z</when></TimeStamp>            </RasterEntry>\n" +
            "         </rasterEntries>\n" +
            "      </RasterDataSet>\n" +
            "   </dataSets>\n" +
            "</oms>";
  }
}
