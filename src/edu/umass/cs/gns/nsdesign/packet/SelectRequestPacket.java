/*
 * Copyright (C) 2013
 * University of Massachusetts
 * All Rights Reserved 
 */
package edu.umass.cs.gns.nsdesign.packet;

import edu.umass.cs.gns.util.NameRecordKey;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A SelectRequestPacket is like a DNS packet without a GUID, but with a key and value.
 * The semantics is that we want to look up all the records that have a field named key with the given value.
 * We also use this to do automatic group GUID maintenence.
 * @author westy
 */
public class SelectRequestPacket extends BasicPacket {

  public enum SelectOperation {

    EQUALS, // special case query for field with value
    NEAR, // special case query for location field near point
    WITHIN, // special case query for location field within bounding box
    QUERY, // general purpose query
    GROUP_SETUP, // set up a group guid that satisfies general purpose query
    GROUP_LOOKUP; // lookup value of group guid previoulsy set up to satisfy general purpose query
  }
  //
  private final static String ID = "id";
  private final static String KEY = "key";
  private final static String VALUE = "value";
  private final static String OTHERVALUE = "otherValue";
  private final static String QUERY = "query";
  private final static String LNSID = "lnsid";
  private final static String LNSQUERYID = "lnsQueryId";
  private final static String NSID = "nsid";
  private final static String NSQUERYID = "nsQueryId";
  private final static String OPERATION = "operation";
  private final static String GUID = "guid";
  private final static String REFRESH = "refresh";
  //
  private int id;
  private NameRecordKey key;
  private Object value;
  private Object otherValue;
  private String query;
  private int lnsID; // the local name server handling this request
  private int lnsQueryId = -1; // used by the local name server to maintain state
  private int nsID; // the name server handling this request (if this is -1 the packet hasn't made it to the NS yet)
  private int nsQueryId = -1; // used by the name server to maintain state
  private SelectOperation operation;
  // for group guid
  private String guid; // the group GUID we are maintaning or null for simple select
  private int refreshInterval; // minimum time between allowed refreshs of the guid

  /**
   * Constructs a new QueryResponsePacket
   * 
   * @param id
   * @param key
   * @param value
   * @param lns 
   */
  public SelectRequestPacket(int id, int lns, SelectOperation operation, NameRecordKey key, Object value, Object otherValue) {
    this.type = Packet.PacketType.SELECT_REQUEST;
    this.id = id;
    this.key = key;
    this.value = value;
    this.otherValue = otherValue;
    this.lnsID = lns;
    this.nsID = -1;
    this.operation = operation;
    this.query = null;
    this.guid = null;
  }

  private SelectRequestPacket(int id, int lns, SelectOperation operation, String query, String guid, int refreshInterval) {
    this.type = Packet.PacketType.SELECT_REQUEST;
    this.id = id;
    this.query = query;
    this.lnsID = lns;
    this.nsID = -1;
    this.operation = operation;
    this.key = null;
    this.value = null;
    this.otherValue = null;
    this.guid = guid;
    this.refreshInterval = refreshInterval;
  }

  /**
   * Creates a request to search all name servers for GUIDs that match the given query.
   * 
   * @param id
   * @param lns
   * @param query
   * @return 
   */
  public static SelectRequestPacket MakeQueryRequest(int id, int lns, String query) {
    return new SelectRequestPacket(id, lns, SelectOperation.QUERY, query, null, -1);
  }

  /**
   * Just like a MakeQueryRequest except we're creating a new group guid to maintain results.
   * Creates a request to search all name servers for GUIDs that match the given query.
   * 
   * @param id
   * @param lns
   * @param query
   * @param guid
   * @return 
   */
  public static SelectRequestPacket MakeGroupSetupRequest(int id, int lns, String query, String guid, int refreshInterval) {
    return new SelectRequestPacket(id, lns, SelectOperation.GROUP_SETUP, query, guid, refreshInterval);
  }
  
  /**
   * Just like a MakeQueryRequest except we're potentially updating the group guid to maintain results.
   * Creates a request to search all name servers for GUIDs that match the given query.
   * 
   * @param id
   * @param lns
   * @param guid
   * @return 
   */
  public static SelectRequestPacket MakeGroupLookupRequest(int id, int lns, String guid) {
    return new SelectRequestPacket(id, lns, SelectOperation.GROUP_LOOKUP, null, guid, -1);
  }

  /**
   * Constructs new SelectRequestPacket from a JSONObject
   * @param json JSONObject representing this packet
   * @throws org.json.JSONException
   */
  public SelectRequestPacket(JSONObject json) throws JSONException {
    if (Packet.getPacketType(json) != Packet.PacketType.SELECT_REQUEST) {
      Exception e = new Exception("QueryRequestPacket: wrong packet type " + Packet.getPacketType(json));
      return;
    }
    this.type = Packet.getPacketType(json);
    this.id = json.getInt(ID);
    this.key = json.has(KEY) ? NameRecordKey.valueOf(json.getString(KEY)) : null;
    this.value = json.optString(VALUE, null);
    this.otherValue = json.optString(OTHERVALUE, null);
    this.query = json.optString(QUERY, null);
    this.lnsID = json.getInt(LNSID);
    this.lnsQueryId = json.getInt(LNSQUERYID);
    this.nsID = json.getInt(NSID);
    this.nsQueryId = json.getInt(NSQUERYID);
    this.operation = SelectOperation.valueOf(json.getString(OPERATION));
    this.guid = json.optString(GUID, null);
    this.refreshInterval = json.optInt(REFRESH, -1);
  }

  /**
   * Converts a SelectRequestPacket to a JSONObject.
   *
   * @return JSONObject
   * @throws org.json.JSONException
   */
  @Override
  public JSONObject toJSONObject() throws JSONException {
    JSONObject json = new JSONObject();
    addToJSONObject(json);
    return json;
  }

  private void addToJSONObject(JSONObject json) throws JSONException {
    Packet.putPacketType(json, getType());
    json.put(ID, id);
    if (key != null) {
      json.put(KEY, key.getName());
    }
    if (value != null) {
      json.put(VALUE, value);
    }
    if (otherValue != null) {
      json.put(OTHERVALUE, otherValue);
    }
    if (query != null) {
      json.put(QUERY, query);
    }
    json.put(LNSID, lnsID);
    json.put(LNSQUERYID, lnsQueryId);
    json.put(NSID, nsID);
    json.put(NSQUERYID, nsQueryId);
    json.put(OPERATION, operation.name());
    if (guid != null) {
      json.put(GUID, guid);
    }
    if (refreshInterval != -1) {
      json.put(REFRESH, refreshInterval);
    }
  }

  public void setLnsQueryId(int lnsQueryId) {
    this.lnsQueryId = lnsQueryId;
  }

  public void setNsQueryId(int nsQueryId) {
    this.nsQueryId = nsQueryId;
  }

  public void setNsID(int nsID) {
    this.nsID = nsID;
  }

  public int getId() {
    return id;
  }

  public NameRecordKey getKey() {
    return key;
  }

  public Object getValue() {
    return value;
  }

  public int getLnsID() {
    return lnsID;
  }

  public int getLnsQueryId() {
    return lnsQueryId;
  }

  public int getNsID() {
    return nsID;
  }

  public int getNsQueryId() {
    return nsQueryId;
  }
  
  public SelectOperation getOperation() {
    return operation;
  }

  public Object getOtherValue() {
    return otherValue;
  }

  public String getQuery() {
    return query;
  }

  public String getGuid() {
    return guid;
  }

  public int getRefreshInterval() {
    return refreshInterval;
  }
 
}
