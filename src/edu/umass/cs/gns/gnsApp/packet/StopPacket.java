package edu.umass.cs.gns.gnsApp.packet;

import edu.umass.cs.reconfiguration.interfaces.InterfaceReconfigurableRequest;
import edu.umass.cs.reconfiguration.interfaces.InterfaceReplicableRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class implements a packet stops things.
 *
 * @author Westy
 */
public class StopPacket extends BasicPacket implements InterfaceReconfigurableRequest,
        InterfaceReplicableRequest {

  private final static String NAME = "name";
  private final static String VERSION = "version";
  /**
   * name for which the proposal is being done.
   */
  private final String name;
  /**
   * ID that is requested to be stopped.
   */
  private final int version;

  /**
   * The stop requests needsCoordination() method must return true by default.
   */
  private boolean needsCoordination = true;

  /**
   * Constructs a new StopPacket.
   *
   * @param name
   * @param version
   */
  public StopPacket(String name, int version) {
    this.type = Packet.PacketType.STOP;
    this.name = name;
    this.version = version;
  }

  /**
   * Constructs new StatusPacket from a JSONObject
   *
   * @param json JSONObject representing this packet
   * @throws org.json.JSONException
   */
  public StopPacket(JSONObject json) throws JSONException {
    if (Packet.getPacketType(json) != Packet.PacketType.STOP) {
      throw new JSONException("STOP: wrong packet type " + Packet.getPacketType(json));
    }
    this.type = Packet.PacketType.STOP;
    this.name = json.getString(NAME);
    this.version = json.getInt(VERSION);
  }

  /**
   * Converts a StatusPacket to a JSONObject.
   *
   * @return JSONObject representing this packet.
   * @throws org.json.JSONException
   */
  @Override
  public JSONObject toJSONObject() throws JSONException {
    JSONObject json = new JSONObject();
    Packet.putPacketType(json, getType());
    json.put(NAME, name);
    json.put(VERSION, version);
    return json;
  }

  @Override
  public int getEpochNumber() {
    return version;
  }

  @Override
  public boolean isStop() {
    return true;
  }

  @Override
  public String getServiceName() {
    return name;
  }

  /**
   * Returns true if the packet needs coordination.
   * 
   * If your app is using PaxosReplicaCoordinator, its stop request
   * (as returned by getStopRequest) must implement InterfaceReplicableRequest
   * and the stop requests needsCoordination method must return true by default
   * (overwriteable by setNeedsCoordination).
   *
   * @return true if the packet needs coordination
   */
  @Override
  public boolean needsCoordination() {
    return needsCoordination;
  }

  /**
   * Overwrite the needsCoordination flag.
   *
   * @param needsCoordination
   */
  @Override
  public void setNeedsCoordination(boolean needsCoordination) {
    this.needsCoordination = needsCoordination;
  }

}