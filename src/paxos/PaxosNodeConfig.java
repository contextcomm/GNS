package paxos;

import edu.umass.cs.gnrs.main.GNS;
import edu.umass.cs.gnrs.main.StartNameServer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

/**
 *
 * User: abhigyan
 * Date: 6/29/13
 * Time: 7:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class PaxosNodeConfig extends nio.NodeConfig {

    /**
     * Stores list of nodes, their IP address, and port numbers
     * @param nodeConfigFile
     */
    public PaxosNodeConfig(String nodeConfigFile) {
        readConfigFile(nodeConfigFile);
    }

    private static HashMap<Integer,NodeInfo> nodesInfo;

    private static void readConfigFile(String configFile) {
        nodesInfo = new HashMap<Integer, NodeInfo>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(configFile));

            while (true) {
                String line = br.readLine();
                if (line == null) break;
                String[] tokens = line.split("\\s+");
                nodesInfo.put(Integer.parseInt(tokens[0]),
                        new NodeInfo(Integer.parseInt(tokens[0]),
                                InetAddress.getByName(tokens[1]),
                                Integer.parseInt(tokens[2])));
            }
        } catch (FileNotFoundException e) {
            if (StartNameServer.debugMode) GNS.getLogger().severe(" EXIT: Config file not found.");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            System.exit(2);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    @Override
    public boolean containsNodeInfo(int ID) {
        return  nodesInfo.containsKey(ID);
    }

    @Override
    public int getNodeCount() {
        return nodesInfo.size();
    }

    @Override
    public InetAddress getNodeAddress(int ID) {
        if (containsNodeInfo(ID)) return  nodesInfo.get(ID).getAddress();
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getNodePort(int ID) {
        if (containsNodeInfo(ID)) return  nodesInfo.get(ID).getPort();
        return -1;  //To change body of implemented methods use File | Settings | File Templates.
    }
}



class NodeInfo {
    private InetAddress address;
    private int port;
    private int ID;

    public NodeInfo(int ID, InetAddress address, int port) {
        this.ID = ID;
        this.port = port;
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public int getID() {
        return ID;
    }

    public InetAddress getAddress() {
        return address;
    }

}