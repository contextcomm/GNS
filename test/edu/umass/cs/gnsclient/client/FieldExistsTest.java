/*
 * Copyright (C) 2013
 * University of Massachusetts
 * All Rights Reserved 
 */
package edu.umass.cs.gnsclient.client;

import edu.umass.cs.gnsclient.client.UniversalTcpClientExtended;
import edu.umass.cs.gnsclient.client.GuidEntry;
import edu.umass.cs.gnsclient.client.util.GuidUtils;
import edu.umass.cs.gnsclient.client.util.ServerSelectDialog;
import edu.umass.cs.gnsclient.client.util.Utils;
import edu.umass.cs.gnsclient.exceptions.GnsException;
import edu.umass.cs.gnsclient.exceptions.GnsFieldNotFoundException;
import java.net.InetSocketAddress;
import static org.junit.Assert.*;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Comprehensive functionality test for the GNS.
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FieldExistsTest {

  private static final String ACCOUNT_ALIAS = "westy@cs.umass.edu"; // REPLACE THIS WITH YOUR ACCOUNT ALIAS
  private static final String PASSWORD = "password";
  private static UniversalTcpClientExtended client = null;
  private static GuidEntry masterGuid;
  private static GuidEntry subGuidEntry;

  public FieldExistsTest() {
    if (client == null) {
      InetSocketAddress address = ServerSelectDialog.selectServer();
      client = new UniversalTcpClientExtended(address.getHostName(), address.getPort(), true);
      try {
        masterGuid = GuidUtils.lookupOrCreateAccountGuid(client, ACCOUNT_ALIAS, PASSWORD, true);
      } catch (Exception e) {
        fail("Exception when we were not expecting it: " + e);
      }
    }
  }

  @Test
  public void test_01_CreateEntity() {
    try {
      GuidUtils.registerGuidWithTestTag(client, masterGuid, "testGUID" + Utils.randomString(6));
    } catch (Exception e) {
      fail("Exception when we were not expecting it: " + e);
    }
  }

  @Test
  public void test_02_CreateSubGuid() {
    try {
      subGuidEntry = GuidUtils.registerGuidWithTestTag(client, masterGuid, "subGuid" + Utils.randomString(6));
      System.out.println("Created: " + subGuidEntry);
    } catch (Exception e) {
      fail("Exception when we were not expecting it: " + e);
    }
  }

  @Test
  public void test_03_FieldNotFoundException() {
    try {
      client.fieldReadArrayFirstElement(subGuidEntry.getGuid(), "environment", subGuidEntry);
      fail("Should have thrown an exception.");
    } catch (GnsFieldNotFoundException e) {
      System.out.println("This was expected: " + e);
    } catch (Exception e) {
      fail("Exception when we were not expecting it: " + e);
    }
  }

  @Test
  public void test_04_FieldExistsFalse() {
    try {
      assertFalse(client.fieldExists(subGuidEntry.getGuid(), "environment", subGuidEntry));
    } catch (Exception e) {
      fail("Exception when we were not expecting it: " + e);
    }
  }

  @Test
  public void test_05_CreateField() {
    try {
      client.fieldCreateOneElementList(subGuidEntry.getGuid(), "environment", "work", subGuidEntry);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception during create field: " + e);
    }
  }

  @Test
  public void test_06_FieldExistsTrue() {
    try {
      assertTrue(client.fieldExists(subGuidEntry.getGuid(), "environment", subGuidEntry));
    } catch (Exception e) {
      fail("Exception when we were not expecting it: " + e);
    }
  }
}