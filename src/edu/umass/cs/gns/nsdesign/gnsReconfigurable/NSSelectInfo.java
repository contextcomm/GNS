package edu.umass.cs.gns.nsdesign.gnsReconfigurable;

import edu.umass.cs.gns.nsdesign.packet.SelectRequestPacket.SelectOperation;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents a data structure to store information
 * about Select operations performed on the GNS.
 */
public class NSSelectInfo {
  private int id;
  private Set<Integer> serversToBeProcessed; // the list of servers that have yet to be processed
  private ConcurrentHashMap<String, JSONObject> responses;
  private SelectOperation operation;
  private String guid; // the group GUID we are maintaning or null for simple select

  /**
   * 
   * @param id
   * @param serverIds 
   */
  public NSSelectInfo(int id, Set<Integer> serverIds, SelectOperation operation, String guid) {
    this.id = id;
    this.serversToBeProcessed = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
    this.serversToBeProcessed.addAll(serverIds);
    this.responses = new ConcurrentHashMap<String, JSONObject>(10, 0.75f, 3);
    this.operation = operation;
    this.guid = guid;
  }

  /**
   * 
   * @return 
   */
  public int getId() {
    return id;
  }

  /**
   * Removes the server if from the list of servers that have yet to be processed.
   * @param id 
   */
  public void removeServerID(int id) {
    serversToBeProcessed.remove(id);
  }
  /**
   * 
   * @return 
   */
  public Set<Integer> serversYetToRespond() {
    return serversToBeProcessed;
  }
  /**
   * Returns true if all the names servers have responded.
   * 
   * @return 
   */
  public boolean allServersResponded() {
    return serversToBeProcessed.isEmpty();
  }

  /**
   * Adds the result of a query for a particular guid if the guid has not been seen yet.
   * 
   * @param name
   * @param json
   * @return 
   */
  public boolean addResponseIfNotSeenYet(String name, JSONObject json) {
    if (!responses.containsKey(name)) {
      responses.put(name, json);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns that responses that have been see for this query.
   * @return 
   */
  public Set<JSONObject> getResponses() {
    return new HashSet<JSONObject>(responses.values());
  }

  public SelectOperation getOperation() {
    return operation;
  }

  public String getGuid() {
    return guid;
  }

}
