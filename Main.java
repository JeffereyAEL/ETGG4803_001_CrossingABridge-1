import java.util.Hashtable;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class BridgeCrossing
{
  /// NESTED CLASSES
  // A class that represents any given state of the given bridge crossing problem
  private class Node
  {
    /// ATTRIBUTES
    private Integer[] left;
    private boolean even;


    /// CONSTRUCTORS
    // Defualt constructor - creates invalid instance of the bridge problem
    public Node() {
        ArrayList<Integer> temp = new ArrayList<Integer>();
        for (Integer i = 0; i < BridgeCrossing.Length; ++i)
        {
          temp.add(-1);
        }
        left = new Integer[temp.size()];
        left = temp.toArray(left);
        even = false;
    }

    // A "copy" constructor 
    public Node(Integer[] l, boolean e) { 
      ArrayList<Integer> temp = new ArrayList<Integer>();
      for (Integer i = 0; i < BridgeCrossing.Length; ++i)
      {
        temp.add(l[i]);
      }
      left = new Integer[temp.size()];
      left = temp.toArray(left);
      even = e;
    }

    // A constructor for starting or final states
    public Node(Integer a, boolean e) { 
      ArrayList<Integer> temp = new ArrayList<Integer>();
      for (Integer i = 0; i < BridgeCrossing.Length; ++i)
      {
        temp.add(a);
      }
      left = new Integer[temp.size()];
      left = temp.toArray(left);
      even = e;
    }
    
    // A constructor to handle transitioning forward across the bridge
    public Node(Integer[] side, Integer p1, Integer p2)
    {
      even = true;
      left = Arrays.copyOf(side, BridgeCrossing.Length);
      left[p1] = 0;
      left[p2] = 0;
    }

    // A constructor to handle transitioning back across the bridge
    public Node(Integer[] side, Integer p1)
    {
      even = false;
      ArrayList<Integer> temp = new ArrayList<Integer>();
      for (Integer i = 0; i < BridgeCrossing.Length; ++i)
      {
        temp.add(-1);
      }
      left = new Integer[temp.size()];
      left = temp.toArray(left);

      for (Integer i = 0; i < BridgeCrossing.Length; ++i)
        if (side[i] == 0)
          left[i] = 1;
        else if (side[i] == 1)
          left[i] = 0;
      left[p1] = 1;
    }

    /// METHODS
    /// GETTERS
    // Returns the left hand side of this instance of the bridge crossing problem
    public Integer[] Left() { 
      ArrayList<Integer> temp = new ArrayList<Integer>();
      for (Integer i = 0; i < BridgeCrossing.Length; ++i)
      {
        temp.add(left[i]);
      }
      Integer t[] = new Integer[temp.size()];
      return temp.toArray(t);
    }

    // Returns the right hand side of this instance of the bridge crossing problem
    public Integer[] Right() { 
      ArrayList<Integer> temp = new ArrayList<Integer>();
      for (Integer i = 0; i < BridgeCrossing.Length; ++i)
      {
        if (left[i] == 0)
          temp.add(1);
        else if (left[i] == 1)
          temp.add(0);
      }
      Integer t[] = new Integer[temp.size()];
      return temp.toArray(t);
    }

    // Returns whether this node is a forward or backwards transition across the bridge
    private String IsEven()
    {
      if(even)
        return "Even";
      else
        return "Odd";
    }

    /// OVERRIDES
    // override for the equals operator
    public boolean equals (Object other)  { 
      if (null == other) return false;
      if (! (other instanceof Node)) return false;
      for (Integer i = 0; i < BridgeCrossing.Length; ++i)
        if (left[i] != ((Node)other).left[i])
          return false;
      if (even != ((Node)other).even) return false;

      return true;
    }

    // overrides the hashcode operator
    public int hashCode()
    {
      return print().hashCode() + IsEven().hashCode();
    }

    /// DEBUG
    // returns a string representation of this state of the bridge crossing problem
    public String print() {
      String s = "";
      Integer[] r = Right();
      for (Integer i = 0; i < BridgeCrossing.Length; ++i)
      {
        s += left[i] + " ";
      }
      s += "| ";
      for (Integer i = 0; i < BridgeCrossing.Length; ++i)
      {
        s += r[i] + " ";
      }
      return s;
    }

    // Returns a deep copy of this node
    public Node copy()
    {
      return new Node(Left(), even);
    }

  }

  // A wrapper class to relate a given node and it's current time and previous node in a hashmap of Node : Stat
  private class Stat
  {
    /// CONSTRUCTORS
    // Defualt constructor
    Stat() {Time = -1; Prev = new Node(); }

    // Full body constructor
    Stat(Integer d, Node n) {Time = d; Prev = n.copy(); }

    /// ATTRIBUTES
    // The time the paired Node (via the hasmap this is a value of) has taken to get to
    public Integer Time;

    // The previous Node the pair Node (via the hasmap this is a value of) was reached from
    public Node Prev;

    /// METHODS
    /// SETTERS
    // Sets the Time attribute of this Stat
    public void SetTime(Integer d) { Time = d; }

    // Sets the Prev attribute of this Stat
    public void SetPrev(Node n) { Prev = n.copy(); }
  }

  // A wrapper to help pass a Node and it's transition time from within a function
  class Package
  {
    /// CONSTRUCTORS
    // Defualt constructor
    Package() {n = new Node(); d = -1; }

    // Full body constructor
    Package(Node node, Integer dist) {n = node.copy(); d = dist; }

    /// ATTRIBUTES
    // The Node this stores
    public Node n;

    // The Time it took to transition to the paired node
    public Integer d;
  }

  /// ATTRIBUTES
  // The list of all "people" - their indice represents their position in any node 
  // and the value of that indice is their travel time
  protected Integer[] party;

  // The amount of people in the party of this instance of the Bridge Crossing Problem
  public static Integer Length;

  // A hashtable of all relevant states of the problem and their best calculated time | previous node
  Hashtable<Node, Stat> crossing;

  // The target start and end states of this Bridge Crossing Problem
  Node start, end;

  /// CONSTRUCTORS
  // Constructs a bridge crossing problem from an Integer array of "people"  (i.e. a list of integers that represent the time it takes that index to cross the bridge)
  BridgeCrossing(Integer[] args) {
    BridgeCrossing.Length = args.length;
    party = Arrays.copyOf(args, BridgeCrossing.Length); // The times to travel across the bridge
                                                        // relative to bitwise position
    start = new Node(1, true); // everyone on the left (1,1,1,1) or 15
    end = new Node(0, true); // everyone on the right (0,0,0,0) or 0

    crossing = new Hashtable<Node, Stat>(); // All the nodes and the best length of time to get there
    crossing.put(start.copy(), new Stat(0, new Node())); // 
  }

  /// METHODS
  // Returns an array of all nodes (and the time that transition takes) that can be reached from the given node and the direction across the bridge people are moving
  public ArrayList<Package> GetAllNextNodes(Node n, boolean forward)
  {
    Integer[] side;
    Integer p1, p2;
    ArrayList<Package> NewNodes = new ArrayList<Package>();

    if (forward)
    {
      side = n.Left();

      for (p1 = BridgeCrossing.Length-1; p1 > -1; --p1)
      {
        if (side[p1] == 1)
        {
          for (p2 = p1-1; p2 > -1; --p2)
          {
            if (side[p2] == 1)
            { 
              Integer dist = Integer.max(party[p1], party[p2]);
              NewNodes.add(new Package(
                              new Node(Arrays.copyOf(side, BridgeCrossing.Length), p1, p2),
                              dist
                            )
                          );
            }
          }
        }
      }
    }
    else
    {
      side = n.Right();
      for (p1 = BridgeCrossing.Length-1; p1 > -1; --p1)
      {
        if (side[p1] == 1)
        {
          Integer dist = party[p1];
          NewNodes.add(new Package(
                          new Node(Arrays.copyOf(side, BridgeCrossing.Length), p1),
                          dist
                        )
                      );
        }
      }
    }

    return NewNodes;
  }

  /// Survey's a node according to Djikstra's algorithm and returns all discoved nodes
  public ArrayList<Node> Eat(Node n, Integer step)
  {
    ArrayList<Package> nextNodes = GetAllNextNodes(n.copy(), step%2==0);

    ArrayList<Node> nodes = new ArrayList<Node>();
    for (Package p : nextNodes)
    {
      Stat CurrStat = crossing.get(n);
      Integer newDist = p.d + CurrStat.Time;

      Stat s = crossing.get(p.n);
      if (s == null)
      {
        crossing.put(p.n.copy(), new Stat(newDist, n.copy()));
      }
      else if (s.Time > newDist)
      {
        crossing.get(p.n).SetTime(newDist);
        crossing.get(p.n).SetPrev(n);
      }

      nodes.add(p.n.copy());
    }

    return nodes;
  }

  /// Iterates from a starting position, finds all next positions given the rules of the BCP and uses Djikstra's algorithm to find the fastest time to travel to the end position of the node
  Integer BruteForceBestTimeAndPathList() {
    if (BridgeCrossing.Length == 1)
    {
      Main.print("Best Time = " + party[0]);
      Main.print("Finished");
      return party[0];
    }

    ArrayList<Node> SurveyedNodes = new ArrayList<Node>();

    ArrayList<Node> NextRound = new ArrayList<Node>();
    NextRound.add(start.copy());
    ArrayList<Node> ThisRound = new ArrayList<Node>();

    boolean NotDone = true;
    Integer step = 0;
    //Main.print("Everything is ready");

    while (NotDone)
    {
      ThisRound.clear();
      for (Node n : NextRound)
        ThisRound.add(n.copy());
      NextRound.clear();
      NotDone = false;


      //Main.print("=== this round ===");
      for (Node CurrNode : ThisRound)
      {
        if (!SurveyedNodes.contains(CurrNode))
        {
          //Main.print("Surveying node " + CurrNode.print() + " | time = " + crossing.get(CurrNode.copy()).Time);
          NextRound.addAll(Eat(CurrNode.copy(), step));

          SurveyedNodes.add(CurrNode.copy());
          NotDone = true;
        }
      }
      //Main.print("==================");

      step++;
    }
    Integer BestTime = crossing.get(end.copy()).Time;
    Main.print("Best Time = " + BestTime);

    Node n = end;
    while (!(n.equals(start)))
    {
      Main.print("Previous node = " + crossing.get(n.copy()).Prev.print());
      n = crossing.get(n.copy()).Prev.copy();
    }

    Main.print("Finished");
    return BestTime;
  }

  // A clean wrapper for the problem this lab was a solution for
  // Includes comment of the given prompt
  public void q2() {
    /*
    2. (15 points) Write a program that takes as input the walking times for each of four people and outputs the shortest time possible for them to all cross the bridge as well as the order in which they should cross. Assume the walking times are all integer values, but do not assume that the walking times are different for each person. The program should give the shortest time for any set of four walking times. (As discussed in class, the shortest solution in the original problem is 17 minutes. What happens if the walking times are one, four, six, and ten minutes?)");
    */
    BruteForceBestTimeAndPathList();
  }
}

// The Main 
class Main {
  // A wrapper for the system print functionality
  public static void print(String s) {
    System.out.println(s);
  }
  
  // The main
  public static void main(String[] args) {
    // ADJUST BRIDGE CROSSING VALUES HERE
    BridgeCrossing alg = new BridgeCrossing(new Integer[]{1,2,5,10});
    alg.q2();
  }
}