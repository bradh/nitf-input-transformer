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

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.MetacardType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * User: kwplummer
 * Date: 4/10/13
 * Time: 11:26 AM
 */
public class NITFMetacardType implements MetacardType{


  @Override
  public String getName() {
    return "NITF";
  }

  @Override
  public Set<AttributeDescriptor> getAttributeDescriptors() {
    return new HashSet<AttributeDescriptor>();
  }

  @Override
  public AttributeDescriptor getAttributeDescriptor(String s) {
    return null;
  }
}
