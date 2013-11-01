package edu.umass.cs.gns.client;

import edu.umass.cs.gns.main.GNS;
import edu.umass.cs.gns.nameserver.ResultValue;

//import edu.umass.cs.gns.packet.QueryResultValue;
/**
 * GroupAccess provides an interface to the group information in the GNSR.
 *
 * The members of a group are stored in a record whose key is the GROUP string. 
 *
 * @author westy
 */
public class GroupAccess {

  /**
   * Hidden field that stores group members
   */
  public static final String GROUP = GNS.makeInternalField("group");
  /**
   * Hidden field that stores group member join requests
   */
  public static final String JOINREQUESTS = GNS.makeInternalField("joinRequests");
  /**
   * Hidden field that stores group member quit requests
   */
  public static final String LEAVEREQUESTS = GNS.makeInternalField("leaveRequests");

  public GroupAccess() {
  }

  // make it a singleton class
  public static GroupAccess getInstance() {
    return GroupAccessHolder.INSTANCE;
  }

  private static class GroupAccessHolder {

    private static final GroupAccess INSTANCE = new GroupAccess();
  }

  public boolean addToGroup(String guid, String memberGuid) {
    Intercessor client = Intercessor.getInstance();
    return client.sendUpdateRecordWithConfirmation(guid, GROUP, memberGuid, null, UpdateOperation.APPEND_OR_CREATE);
  }
  
  public boolean addToGroup(String guid, ResultValue members) {
    Intercessor client = Intercessor.getInstance();
    return client.sendUpdateRecordWithConfirmation(guid, GROUP, members, null, UpdateOperation.APPEND_OR_CREATE);
  }

  public boolean removeFromGroup(String guid, String memberGuid) {
    Intercessor client = Intercessor.getInstance();
    return client.sendUpdateRecordWithConfirmation(guid, GROUP, memberGuid, null, UpdateOperation.REMOVE);
  }
  
  public boolean removeFromGroup(String guid, ResultValue members) {
    Intercessor client = Intercessor.getInstance();
    return client.sendUpdateRecordWithConfirmation(guid, GROUP, members, null, UpdateOperation.REMOVE);
  }

  public ResultValue lookup(String guid) {
    Intercessor client = Intercessor.getInstance();
    ResultValue result = client.sendQuery(guid, GROUP);
    if (result != null) {
      return new ResultValue(result);
    } else {
      return new ResultValue();
    }
  }

  public boolean requestJoinGroup(String guid, String memberGuid) {
    Intercessor client = Intercessor.getInstance();
    return client.sendUpdateRecordWithConfirmation(guid, JOINREQUESTS, memberGuid, null, UpdateOperation.APPEND_OR_CREATE);
  }
  
  public boolean requestLeaveGroup(String guid, String memberGuid) {
    Intercessor client = Intercessor.getInstance();
    return client.sendUpdateRecordWithConfirmation(guid, LEAVEREQUESTS, memberGuid, null, UpdateOperation.APPEND_OR_CREATE);
  }

  public ResultValue retrieveGroupJoinRequests(String guid) {
    Intercessor client = Intercessor.getInstance();
    ResultValue result = client.sendQuery(guid, JOINREQUESTS);
    if (result != null) {
      return new ResultValue(result);
    } else {
      return new ResultValue();
    }
  }
  
  public ResultValue retrieveGroupLeaveRequests(String guid) {
    Intercessor client = Intercessor.getInstance();
    ResultValue result = client.sendQuery(guid, LEAVEREQUESTS);
    if (result != null) {
      return new ResultValue(result);
    } else {
      return new ResultValue();
    }
  }

  public boolean grantMembership(String guid, ResultValue requests) {
    Intercessor client = Intercessor.getInstance();

    if (client.sendUpdateRecordWithConfirmation(guid, GROUP, requests, null, UpdateOperation.APPEND_OR_CREATE)) {
      if (client.sendUpdateRecordWithConfirmation(guid, JOINREQUESTS, requests, null, UpdateOperation.REMOVE)) {
        return true;
      }
    }
    return false;
  }
  
  public boolean revokeMembership(String guid, ResultValue requests) {
    Intercessor client = Intercessor.getInstance();

    if (client.sendUpdateRecordWithConfirmation(guid, GROUP, requests, null, UpdateOperation.REMOVE)) {
      if (client.sendUpdateRecordWithConfirmation(guid, LEAVEREQUESTS, requests, null, UpdateOperation.REMOVE)) {
        return true;
      }
    }
    return false;
  }
  public static String Version = "$Revision$";
}
