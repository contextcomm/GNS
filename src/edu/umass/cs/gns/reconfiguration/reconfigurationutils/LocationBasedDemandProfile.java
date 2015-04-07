package edu.umass.cs.gns.reconfiguration.reconfigurationutils;

import edu.umass.cs.gns.nsdesign.Config;
import edu.umass.cs.gns.reconfiguration.InterfaceReplicableRequest;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import edu.umass.cs.gns.reconfiguration.InterfaceRequest;
import edu.umass.cs.gns.util.Util;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * This class maintains the demand profile for a single name.
 * It will select active name servers based on location of the demand.
 * It is based on the old LocationBasedReplication class, but doesn't
 * include checks for unreachable active servers or highly loaded servers.
 * 
 *
 * @author Westy
 */
public class LocationBasedDemandProfile extends AbstractDemandProfile {

  private static final Logger LOG = Logger.getLogger(LocationBasedDemandProfile.class.getName());

  public enum Keys {

    SERVICE_NAME, STATS, RATE, NUM_REQUESTS, NUM_TOTAL_REQUESTS, VOTES_MAP, LOOKUP_COUNT, UPDATE_COUNT
  };

  private static final int DEFAULT_NUM_REQUESTS = 1;
  private static final long MIN_RECONFIGURATION_INTERVAL = 000;
  private static final long MIN_REQUESTS_BEFORE_RECONFIGURATION = 1;

  private double interArrivalTime = 0.0;
  private long lastRequestTime = 0;
  private int numRequests = 0;
  private int numTotalRequests = 0;
  private LocationBasedDemandProfile lastReconfiguredProfile = null;
  private VotesMap votesMap = new VotesMap();
  private int lookupCount = 0;
  private int updateCount = 0;

  public LocationBasedDemandProfile(String name) {
    super(name);
  }

  // deep copy constructor
  public LocationBasedDemandProfile(LocationBasedDemandProfile dp) {
    super(dp.name);
    this.interArrivalTime = dp.interArrivalTime;
    this.lastRequestTime = dp.lastRequestTime;
    this.numRequests = dp.numRequests;
    this.numTotalRequests = dp.numTotalRequests;
    this.votesMap = new VotesMap(dp.votesMap);
    this.updateCount = dp.updateCount;
    this.lookupCount = dp.lookupCount;
  }

  public LocationBasedDemandProfile(JSONObject json) throws JSONException {
    super(json.getString(Keys.SERVICE_NAME.toString()));
    this.interArrivalTime = 1.0 / json.getDouble(Keys.RATE.toString());
    this.numRequests = json.getInt(Keys.NUM_REQUESTS.toString());
    this.numTotalRequests = json.getInt(Keys.NUM_TOTAL_REQUESTS.toString());
    this.votesMap = new VotesMap(json.getJSONObject(Keys.VOTES_MAP.toString()));
    this.updateCount = json.getInt(Keys.UPDATE_COUNT.toString());
    this.lookupCount = json.getInt(Keys.LOOKUP_COUNT.toString());
    //LOG.info("%%%%%%%%%%%%%%%%%%%%%%%%%>>> " + this.name + " VOTES MAP AFTER READ: " + this.votesMap);
  }

  @Override
  public JSONObject getStats() {
    //LOG.info("%%%%%%%%%%%%%%%%%%%%%%%%%>>> " + this.name + " VOTES MAP BEFORE GET STATS: " + this.votesMap);
    JSONObject json = new JSONObject();
    try {
      json.put(Keys.SERVICE_NAME.toString(), this.name);
      json.put(Keys.RATE.toString(), getRequestRate());
      json.put(Keys.NUM_REQUESTS.toString(), getNumRequests());
      json.put(Keys.NUM_TOTAL_REQUESTS.toString(), getNumTotalRequests());
      json.put(Keys.VOTES_MAP.toString(), getVotesMap().toJSONObject());
      json.put(Keys.UPDATE_COUNT.toString(), this.lookupCount);
      json.put(Keys.LOOKUP_COUNT.toString(), this.updateCount);
    } catch (JSONException je) {
      je.printStackTrace();
    }
    //LOG.info("%%%%%%%%%%%%%%%%%%%%%%%%%>>> " + this.name + " GET STATS: " + json);
    return json;
  }

  public static LocationBasedDemandProfile createDemandProfile(String name) {
    return new LocationBasedDemandProfile(name);
  }

  @Override
  public void register(InterfaceRequest request, InetAddress sender) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  //@Override
  public void registerNew(InterfaceRequest request, InetSocketAddress sender) {

    if (!request.getServiceName().equals(this.name)) {
      return;
    }
    this.numRequests++;
    this.numTotalRequests++;
    long iaTime = 0;
    if (lastRequestTime > 0) {
      iaTime = System.currentTimeMillis() - this.lastRequestTime;
      this.interArrivalTime = Util.movingAverage(iaTime, interArrivalTime);
    } else {
      lastRequestTime = System.currentTimeMillis(); // initialization
    }
    this.votesMap.increment(sender);

    if (request instanceof InterfaceReplicableRequest
            && ((InterfaceReplicableRequest) request).needsCoordination()) {
      updateCount++;
    } else {
      lookupCount++;
    }
    //LOG.info("%%%%%%%%%%%%%%%%%%%%%%%%%>>> " + this.name + " VOTES MAP: " + this.votesMap);
  }

  @Override
  public void reset() {
    this.interArrivalTime = 0.0;
    this.lastRequestTime = 0;
    this.numRequests = 0;
    this.votesMap = new VotesMap();
    this.updateCount = 0;
    this.lookupCount = 0;
  }

  @Override
  public LocationBasedDemandProfile clone() {
    return new LocationBasedDemandProfile(this);
  }

  @Override
  public void combine(AbstractDemandProfile dp) {
    LocationBasedDemandProfile update = (LocationBasedDemandProfile) dp;
    this.lastRequestTime = Math.max(this.lastRequestTime,
            update.lastRequestTime);
    this.interArrivalTime = Util.movingAverage(update.interArrivalTime,
            this.interArrivalTime, update.getNumRequests());
    this.numRequests += update.numRequests; // this number is not meaningful at RC
    this.numTotalRequests += update.numTotalRequests;
    this.updateCount += update.updateCount;
    this.lookupCount += update.lookupCount;
    this.votesMap.combine(update.getVotesMap());
    //LOG.info("%%%%%%%%%%%%%%%%%%%%%%%%%>>> " + this.name + " VOTES MAP AFTER COMBINE: " + this.votesMap);
  }

  @Override
  public boolean shouldReport() {
    if (getNumRequests() >= DEFAULT_NUM_REQUESTS) {
      return true;
    }
    return false;
  }

  @Override
  public void justReconfigured() {
    this.lastReconfiguredProfile = this.clone();
  }

  
  @Override
  public ArrayList<InetAddress> shouldReconfigure(ArrayList<InetAddress> curActives, ConsistentReconfigurableNodeConfig nodeConfig) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
  //@Override
  public ArrayList<InetSocketAddress> shouldReconfigureNew(ArrayList<InetSocketAddress> curActives,
          ConsistentReconfigurableNodeConfig nodeConfig) {
    if (this.lastReconfiguredProfile != null) {
      if (System.currentTimeMillis()
              - this.lastReconfiguredProfile.lastRequestTime < MIN_RECONFIGURATION_INTERVAL) {
        return null;
      }
      if (this.numTotalRequests
              - this.lastReconfiguredProfile.numTotalRequests < MIN_REQUESTS_BEFORE_RECONFIGURATION) {
        return null;
      }
    }
    int numberOfReplicas = computeNumberOfReplicas(lookupCount, updateCount, nodeConfig.getActiveReplicas().size());

    LOG.info("%%%%%%%%%%%%%%%%%%%%%%%%%>>> " + this.name + " VOTES MAP: " + this.votesMap
            + " TOP: " + this.votesMap.getTopN(numberOfReplicas) + " Lookup: " + lookupCount
            + " Update: " + updateCount + " ReplicaCount: " + numberOfReplicas);

    return pickNewActiveReplicas(numberOfReplicas, curActives,
            this.votesMap.getTopNIPSocket(numberOfReplicas),
            nodeConfig.getNodeIPs(nodeConfig.getActiveReplicas()));
  }

  //
  // Routines below for computing new actives list
  //
  /**
   * Returns a list of new active replicas based on read / write load.
   * Returns InetSocketAddresses based on the number needed (which is calculated in computeNumberOfReplicas
   * using read / write load), the current ones and the entire list available from nodeCOnfig.
   *
   * @param numReplica
   * @param curActives
   * @param nodeConfig
   * @return a list of InetSocketAddress
   */
  private ArrayList<InetSocketAddress> pickNewActiveReplicas(int numReplica, ArrayList<InetSocketAddress> curActives,
          ArrayList<InetSocketAddress> topN, ArrayList<InetSocketAddress> allActives) {

    // If we need more replicas than we have just return them all
    if (numReplica >= allActives.size()) {
      LOG.info("%%%%%%%%%%%%%%%%%%%%%%%%%>>> RETURNING ALL");
      return allActives;
    }

    // Otherwise get the topN from the votes list
    ArrayList<InetSocketAddress> newActives = new ArrayList(topN);
    LOG.info("%%%%%%%%%%%%%%%%%%%%%%%%%>>> TOP N: " + newActives);

    // If we have too many in top N then remove some from the end and return this list.
    // BTW: This is the only case that could maybe use some work because it 
    // totally ignores the current active set.
    if (numReplica < newActives.size()) {
      return new ArrayList(newActives.subList(0, numReplica));
    }
    
    // Otherwise we need more than is contained in the top n.
    // Tack on some extras starting with current actives.
    if (newActives.size() < numReplica) {
      for (InetSocketAddress current : curActives) {
        if (newActives.size() >= numReplica) {
          break;
        }
        if (!newActives.contains(current)) {
          newActives.add(current);
        }
      }
    }

    LOG.info("%%%%%%%%%%%%%%%%%%%%%%%%%>>> WITH CURRENT ADDED: " + newActives);

    // If we still need more add some random ones from the active replicas list
    if (newActives.size() < numReplica) {
      // Note here that since for testing (operationally will this be allowed?) we currently 
      // allow multiple actives to reside on the same
      // host differentiated by ports this loop might get the same ip multiple times. Nothing bad will
      // happen because of the contains below, but still it's kind of dumb.
      for (InetSocketAddress ip : (ArrayList<InetSocketAddress>) allActives) {
        if (newActives.size() >= numReplica) {
          break;
        }
        if (!newActives.contains(ip)) {
          newActives.add(ip);
        }
      }
    }
    LOG.info("%%%%%%%%%%%%%%%%%%%%%%%%%>>> WITH RANDOM ADDED: " + newActives);
    return newActives;
  }

  /**
   * Returns the size of active replica set that should exist for this name record.
   * Depends on the read and update rate of this name record.
   * There are two special cases:
   * (1) if there are no lookups or updates for this name, it returns 0.
   * (2) if (numberReplicaControllers == 1), then the system is un-replicated, therefore it always returns 1;
   *
   * Otherwise returns a value in the range {@link edu.umass.cs.gns.nsdesign.Config#minReplica} and
   * {@link edu.umass.cs.gns.nsdesign.Config#maxReplica}.
   */
  private int computeNumberOfReplicas(int lookupCount, int updateCount, int actualReplicasCount) {
    if (Config.singleNS) {
      return 1; // this seems straightforward
    } else if (updateCount == 0) {
      // no updates, replicate everywhere.
      return Math.min(actualReplicasCount, Config.maxReplica);
    } else {
      // Can't be bigger than the number of actual replicas or the max configured amount
      return Math.min(Math.min(actualReplicasCount, Config.maxReplica),
              // Or smaller than the min configured amount
              Math.max(Config.minReplica,
                      (int) StrictMath.round(((double) lookupCount
                              / ((double) updateCount * Config.normalizingConstant)))));
    }
  }

  //
  // Accessors
  //
  public double getRequestRate() {
    return this.interArrivalTime > 0 ? 1.0 / this.interArrivalTime
            : 1.0 / (this.interArrivalTime + 1000);
  }

  public double getNumRequests() {
    return this.numRequests;
  }

  public double getNumTotalRequests() {
    return this.numTotalRequests;
  }

  public VotesMap getVotesMap() {
    return votesMap;
  }

  public static void main(String[] args) throws UnknownHostException {
    LocationBasedDemandProfile dp = new LocationBasedDemandProfile("Frank");
    LOG.info("0,0,3 = " + dp.computeNumberOfReplicas(0, 0, 3));
    LOG.info("20,0,3 = " + dp.computeNumberOfReplicas(20, 0, 3));
    LOG.info("0,20,3 = " + dp.computeNumberOfReplicas(0, 20, 3));
    LOG.info("20,20,3 = " + dp.computeNumberOfReplicas(20, 20, 3));
    LOG.info("2000,100,3 = " + dp.computeNumberOfReplicas(2000, 100, 3));
    LOG.info("100,200,3 = " + dp.computeNumberOfReplicas(100, 2000, 3));
    LOG.info("0,0,200 = " + dp.computeNumberOfReplicas(0, 0, 200));
    LOG.info("20,0,200 = " + dp.computeNumberOfReplicas(20, 0, 200));
    LOG.info("0,20,200 = " + dp.computeNumberOfReplicas(0, 20, 200));
    LOG.info("20,20,200 = " + dp.computeNumberOfReplicas(20, 20, 200));
    LOG.info("2000,100,200 = " + dp.computeNumberOfReplicas(2000, 100, 200));
    LOG.info("100,200,200 = " + dp.computeNumberOfReplicas(100, 2000, 200));
    LOG.info("10000,200,200 = " + dp.computeNumberOfReplicas(10000, 200, 200));

    // All the actives
    ArrayList<InetSocketAddress> allActives = new ArrayList(Arrays.asList(
            new InetSocketAddress(InetAddress.getByName("128.119.1.1"),1000),
            new InetSocketAddress(InetAddress.getByName("128.119.1.2"),1000),
            new InetSocketAddress(InetAddress.getByName("128.119.1.3"),1000),
            new InetSocketAddress(InetAddress.getByName("128.119.1.4"),1000),
            new InetSocketAddress(InetAddress.getByName("128.119.1.5"),1000),
            new InetSocketAddress(InetAddress.getByName("128.119.1.6"),1000),
            new InetSocketAddress(InetAddress.getByName("128.119.1.7"),1000),
            new InetSocketAddress(InetAddress.getByName("128.119.1.8"),1000),
            new InetSocketAddress(InetAddress.getByName("128.119.1.9"),1000),
            new InetSocketAddress(InetAddress.getByName("128.119.1.10"), 1000)));

    // The list of current actives
    ArrayList<InetSocketAddress> curActives = new ArrayList(Arrays.asList(
            new InetSocketAddress(InetAddress.getByName("128.119.1.1"),1000),
            new InetSocketAddress(InetAddress.getByName("128.119.1.2"),1000),
            new InetSocketAddress(InetAddress.getByName("128.119.1.3"),1000)));

    // Simulates the top N vote getters
    ArrayList<InetSocketAddress> topN = new ArrayList(Arrays.asList(
            new InetSocketAddress(InetAddress.getByName("128.119.1.2"),1000),
            new InetSocketAddress(InetAddress.getByName("128.119.1.3"),1000),
            new InetSocketAddress(InetAddress.getByName("128.119.1.4"),1000),
            new InetSocketAddress(InetAddress.getByName("128.119.1.5"), 1000)));

    // Try it with various numbers of active replicas needed
    LOG.info("need 3: " + dp.pickNewActiveReplicas(3, curActives, topN, allActives));

    LOG.info("need 4: " + dp.pickNewActiveReplicas(4, curActives, topN, allActives));

    LOG.info("need 5: " + dp.pickNewActiveReplicas(5, curActives, topN, allActives));

    LOG.info("need 6: " + dp.pickNewActiveReplicas(6, curActives, topN, allActives));

    LOG.info("need 7: " + dp.pickNewActiveReplicas(7, curActives, topN, allActives));

  }
}
