package edu.umass.cs.gnrs.httpserver;

/**
 *
 * @author westy
 */
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import edu.umass.cs.gnrs.client.GroupAccess;
import edu.umass.cs.gnrs.client.Intercessor;
import edu.umass.cs.gnrs.client.RecordAccess;
import edu.umass.cs.gnrs.client.AccountAccess;
import edu.umass.cs.gnrs.main.GNS;
import static edu.umass.cs.gnrs.main.StartLocalNameServer.debugMode;
import edu.umass.cs.gnrs.util.ConfigFileInfo;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author westy
 */
public class GnsHttpServer {

  public static String Version = "$Revision: 629 $";
  private static Protocol protocol = new Protocol();
  protected static String GNSPATH = "GNS";
  public static int address = 80;
  public static int addressNoPriv = 8080;
  public static String hostName = "127.0.0.1";
  private static int localNameServerID;

  public static void runServer() {
    debugMode = true;
    if (!tryPort(address)) {
      tryPort(addressNoPriv);
    }
  }

  public static boolean tryPort(int address) {
    try {
      InetSocketAddress addr = new InetSocketAddress(address);
      HttpServer server = HttpServer.create(addr, 0);

      server.createContext("/", new EchoHandler());
      server.createContext("/" + GNSPATH, new DefaultHandler());
      server.setExecutor(Executors.newCachedThreadPool());
      server.start();
      GNS.getLogger().info("HTTP server is listening on port " + address);
      return true;
    } catch (IOException e) {
      GNS.getLogger().info("HTTP server failed to start on port " + address + " due to " + e);
      return false;
    }
  }
  private static Options commandLineOptions;

  private static CommandLine initializeOptions(String[] args) throws ParseException {
    Option nsFile = OptionBuilder.withArgName("file").hasArg().withDescription("Name server file").create("nsfile");
    Option lnsid = OptionBuilder.withArgName("lnsid").hasArg().withDescription("Local name server id").create("lnsid");
    //Option local = new Option("local", "all servers are on this machine");
    commandLineOptions = new Options();
    commandLineOptions.addOption(nsFile);
    commandLineOptions.addOption(lnsid);
    //commandLineOptions.addOption(local);
    CommandLineParser parser = new GnuParser();
    return parser.parse(commandLineOptions, args);
  }

  public static void main(String[] args) throws IOException {
    String nsFile = "";
    localNameServerID = 0;
    try {
      CommandLine parser = initializeOptions(args);
      nsFile = parser.getOptionValue("nsfile");
      localNameServerID = Integer.parseInt(parser.getOptionValue("lnsid"));
      ConfigFileInfo.readHostInfo(nsFile, 0);
    } catch (Exception e1) {
      e1.printStackTrace();
      System.exit(1);
    }

    if (localNameServerID != -1) {
      // tell the Intercessor what local name server to contact to
      Intercessor.getInstance().setLocalServerID(localNameServerID);
    }
    runServer();
  }

  private static class DefaultHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) {
      try {

        if (localNameServerID == -1) {
          // pick a random local name server - ASSUMES THERE IS AN LNS RUNNING AT EVERY HOST
          Intercessor.getInstance().setLocalServerID(new ArrayList<Integer>(ConfigFileInfo.getAllHostIDs()).get(new Random().nextInt(ConfigFileInfo.getAllHostIDs().size())));
        }

        String requestMethod = exchange.getRequestMethod();
        if (requestMethod.equalsIgnoreCase("GET")) {
          Headers requestHeaders = exchange.getRequestHeaders();
          String host = requestHeaders.getFirst("Host");
          Headers responseHeaders = exchange.getResponseHeaders();
          responseHeaders.set("Content-Type", "text/plain");
          exchange.sendResponseHeaders(200, 0);

          OutputStream responseBody = exchange.getResponseBody();

          URI uri = exchange.getRequestURI();
          String path = uri.getPath();
          String query = uri.getQuery() != null ? uri.getQuery() : ""; // stupidly it returns null for empty query

          String action = path.replaceFirst("/" + GNSPATH + "/", "");

          String response;
          if (!action.isEmpty()) {
            GNS.getLogger().fine("Action: " + action + " Query:" + query);
            response = protocol.processQuery(host, action, query);
          } else {
            response = Protocol.BADRESPONSE + " " + Protocol.NOACTIONFOUND;
          }
          GNS.getLogger().fine("Response: " + response);
          responseBody.write(response.getBytes());
          responseBody.close();
        }
      } catch (Exception e) {
        GNS.getLogger().severe("Error: " + e);
        e.printStackTrace();
        try {
          String response = Protocol.BADRESPONSE + " " + Protocol.QUERYPROCESSINGERROR + " " + e;
          OutputStream responseBody = exchange.getResponseBody();
          responseBody.write(response.getBytes());
          responseBody.close();
        } catch (Exception f) {
          // at this point screw it
        }
      }
    }
  }

// EXAMPLE THAT JUST RETURNS HEADERS SENT
  private static class EchoHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      String requestMethod = exchange.getRequestMethod();
      if (requestMethod.equalsIgnoreCase("GET")) {
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(200, 0);

        OutputStream responseBody = exchange.getResponseBody();
        Headers requestHeaders = exchange.getRequestHeaders();
        Set<String> keySet = requestHeaders.keySet();
        Iterator<String> iter = keySet.iterator();

        String serverVersionInfo = "Server Version: " + Version.replaceFirst(Matcher.quoteReplacement("$Revision:"), "").replaceFirst(Matcher.quoteReplacement("$"), "") + "\n";
        String protocolVersionInfo = "Protocol Version: " + Protocol.Version.replaceFirst(Matcher.quoteReplacement("$Revision:"), "").replaceFirst(Matcher.quoteReplacement("$"), "") + "\n";
        String recordVersionInfo = "Record Version: " + RecordAccess.Version.replaceFirst(Matcher.quoteReplacement("$Revision:"), "").replaceFirst(Matcher.quoteReplacement("$"), "") + "\n";
        String aclVersionInfo = "ACL Version: " + AccountAccess.Version.replaceFirst(Matcher.quoteReplacement("$Revision:"), "").replaceFirst(Matcher.quoteReplacement("$"), "") + "\n";
        String groupsVersionInfo = "Groups Version: " + GroupAccess.Version.replaceFirst(Matcher.quoteReplacement("$Revision:"), "").replaceFirst(Matcher.quoteReplacement("$"), "") + "\n\n";
        
        String serverLocalNameServerID = "Name Server ID: " + localNameServerID + "\n\n";
        
        responseBody.write(serverVersionInfo.getBytes());
        responseBody.write(protocolVersionInfo.getBytes());
        responseBody.write(recordVersionInfo.getBytes());
        responseBody.write(aclVersionInfo.getBytes());
        responseBody.write(groupsVersionInfo.getBytes());
        responseBody.write(serverLocalNameServerID.getBytes());
        while (iter.hasNext()) {
          String key = iter.next();
          List values = requestHeaders.get(key);
          String s = key + " = " + values.toString() + "\n";
          responseBody.write(s.getBytes());
        }
        responseBody.close();
      }
    }
  }
  
  private static String GNRS_IP = "23.21.120.250";
  
  private static class IPHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      String requestMethod = exchange.getRequestMethod();
      if (requestMethod.equalsIgnoreCase("GET")) {
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(200, 0);

        OutputStream responseBody = exchange.getResponseBody();

        responseBody.write(GNRS_IP.getBytes());
        responseBody.close();
      }
    }
  }
}