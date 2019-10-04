package Client;

import java.rmi.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

import Shared.*;

public class Dijkstra {
    
    //constructor
    public Dijkstra(Graph myTestCase, String hostName, String[] ports) throws Exception {
        //declarations
        serversCount = ports.length;
        workerNodesCount = new int[serversCount];
        workerFromNodes = new int[serversCount];
        workerServers = new IServer[serversCount];
        visited = new HashSet<>();
        
        this.myTestcase = myTestCase;
        int[] portsCount = new int[serversCount];
             
        for(int i = 0; i< serversCount; ++i) {
           portsCount[i] = i+1;
            //System.setProperty("java.rmi.server.hostname", hostName);
            //This is item A.1 in the RMI FAQ. 
            //System.setProperty("java.rmi.activation.port");
            
            //System.out.println("debug 1");
            Registry reg = LocateRegistry.getRegistry(hostName, Integer.parseInt(ports[i]));
            //System.out.println("debug 2");
            workerServers[i] = (IServer) reg.lookup("server");
            //workerServers[i] = (IServer) reg.lookup("server" + String.valueOf(portsCount[i]));
            System.out.println("debug 3");
            
            //Returns a reference, a stub, for the remote object associated with the specified name.
            //local host 127.0.0.1
            //taurus host 127.0.1.1
        }
        //Creates a thread pool that reuses a fixed number of threads operating off a shared unbounded queue
       
        executor = Executors.newFixedThreadPool(1);
        //executor = Executors.newFixedThreadPool(serversCount);
       System.out.println("debug 4");
    }
    
    //declarations
    private int[] workerNodesCount;
    private int[] workerFromNodes;
    private IServer[] workerServers;
    private Graph myTestcase;
    private int serversCount;
    private ExecutorService executor;
    private HashSet<Integer> visited;// nodes already visited
    
    public void run() throws InterruptedException, RemoteException {
        System.out.println("The beginning of the 'run' method.");
        
        final int[][] G = myTestcase.getWeights();// maximum size of the graph
        int nodesCount = myTestcase.getNodesCount();
        
        int[] dist = new int[nodesCount];// distance, cost to reach the node from start point
        int[] pred = new int[nodesCount];// predecessors we came to this node from
        
        for(int i=0; i<nodesCount; ++i)
            dist[i] = pred[i] = 9999;// big enough number, bigger than any possible pred
        
        int initialNode = 0; 
        PriorityQueue<Integer> nodesToVisitQ = new PriorityQueue<>();
        nodesToVisitQ.add(initialNode);

        System.out.println("Sending weights to workers...");
        List<Callable<Object>> calls = new ArrayList<>();
        
        for(int i = 0; i< serversCount; ++i) {
            final int workerId = i;
            calls.add(Executors.callable(() -> {
                System.out.println("Sending weights to worker " + workerId);
                try {
                    int[] nodeRanges = calculateWorkerNodeRanges(workerId);
                    int prevVertex = nodeRanges[0];
                    int nextVertex = nodeRanges[1];
                    workerNodesCount[workerId] = nextVertex - prevVertex + 1;
                    workerFromNodes[workerId] = prevVertex;
                    workerServers[workerId].initialData(workerId, nodesCount, nodeRanges, G);
                }
                catch(RemoteException e) {
                    e.printStackTrace();
                }
            }));
        }
        executor.invokeAll(calls);

        dist[initialNode] = 0;
        visited.add(initialNode);
        
        while(nodesToVisitQ.size() != 0) {
            Integer currentNode = nodesToVisitQ.poll();
            System.out.println("The node '" + currentNode + "' is in the path.");
            
            calls = new ArrayList<>();
            for(int i = 0; i< serversCount; ++i) {
                final int workerId = i;
                calls.add(Executors.callable(() -> {
                    System.out.println("Sending weight values to worker '" + workerId+"'.");
                    try {
                        int[] workerDistances = workerServers[workerId].calculateDistances(currentNode, dist[currentNode]);
                        System.arraycopy(workerDistances, 0, dist, workerFromNodes[workerId], workerNodesCount[workerId]);
                    }
                    catch(RemoteException e) {
                        e.printStackTrace();
                    }
                }));
            }
            executor.invokeAll(calls);
            
            for(int node=0; node<nodesCount; ++node)
                if (visited.contains(node) == false && connectionNodesExists(currentNode, node)) {
                    nodesToVisitQ.add(node);
                    visited.add(node);
                }
        }
        
        calls = new ArrayList<>();
        
        for(int i = 0; i< serversCount; ++i) {
            final int workerId = i;
            calls.add(Executors.callable(() -> {
                try {
                    int[] workerPrevNodes = workerServers[workerId].getWorkerPrevNodesPart();
                    System.out.println(workerId + ", prevVertex =" + workerFromNodes[workerId] + ", count=" + workerNodesCount[workerId]);
                    System.arraycopy(workerPrevNodes, 0, pred, workerFromNodes[workerId], workerNodesCount[workerId]);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }));
        }
        executor.invokeAll(calls);

        System.out.println("The result of the dijkstr algorithm implementation:\n");
        System.out.println("Started from node index = " + initialNode);
        System.out.print("\nDist (- means no path) = [");
        for(int node=0; node<nodesCount; ++node) {
            if (dist[node] == 9999)
                System.out.print("-, ");
            else
                System.out.print(dist[node] + ", ");
        }
        System.out.println("\b\b]");
        
        System.out.print("Pred (X means initialNode) = [");
        for(int node=0; node<nodesCount; ++node) {
            if (node == initialNode)
                System.out.print("X, ");
            else
                System.out.print(pred[node] + ", ");
        }
        System.out.println("\b\b]\n");
        
        executor.shutdown();
        System.out.println("End of the 'run' Dijkstra method.");
    }
    
    private boolean connectionNodesExists(int prevVertex, int nextVertex) {
        return this.myTestcase.getWeights()[prevVertex][nextVertex] != -1;
    }
    
    private int[] calculateWorkerNodeRanges(int workerServerId){
        
        int nodesCount = myTestcase.getNodesCount();
        int[] boundWeights = new int[2];
        
        int prevVertex = workerServerId * nodesCount / serversCount ;
        int nextVertex = (workerServerId + 1) * nodesCount / serversCount - 1;
        
        int restNodes = nodesCount % serversCount;
        
        if (workerServerId < restNodes)
        {
            prevVertex += workerServerId;
            nextVertex += workerServerId + 1;
        }
        else
        {
            prevVertex += restNodes;
            nextVertex += restNodes;
        }
        
        boundWeights[0] = prevVertex;
        boundWeights[1] = nextVertex;
        
        return boundWeights;
    }
}
