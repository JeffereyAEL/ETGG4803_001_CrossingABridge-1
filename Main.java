import java.util.Hashtable;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

class BridgeCrossing
{
  // Nested Classes
  class Node
  {
    private Integer[] left;
    boolean even;

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
    
    public Node(Integer[] side, Integer p1, Integer p2)
    {
      even = true;
      left = Arrays.copyOf(side, BridgeCrossing.Length);
      left[p1] = 0;
      left[p2] = 0;
    }

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

    public Integer[] Left() { 
      ArrayList<Integer> temp = new ArrayList<Integer>();
      for (Integer i = 0; i < BridgeCrossing.Length; ++i)
      {
        temp.add(left[i]);
      }
      Integer t[] = new Integer[temp.size()];
      return temp.toArray(t);
    }

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

    public boolean equals (Object other)  { 
      if (null == other) return false;
      if (! (other instanceof Node)) return false;
      for (Integer i = 0; i < BridgeCrossing.Length; ++i)
        if (left[i] != ((Node)other).left[i])
          return false;
      if (even != ((Node)other).even) return false;

      return true;
    }

    private String IsEven()
    {
      if(even)
        return "Even";
      else
        return "Odd";
    }

    public int hashCode()
    {
      return print().hashCode() + IsEven().hashCode();
    }

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

    public Node copy()
    {
      return new Node(Left(), even);
    }

  }

  class Stats
  {
    Stats() {Dist = -1; Prev = new Node(); }
    Stats(Integer d, Node n) {Dist = d; Prev = n.copy(); }
    public Integer Dist;
    public Node Prev;
    public void SetDist(Integer d) { Dist = d; }
    public void SetPrev(Node n) { Prev = n.copy(); }
  }

  class Package
  {
    Package() {n = new Node(); d = -1; }
    Package(Node node, Integer dist) {n = node.copy(); d = dist; }
    public Node n;
    public Integer d;
  }
  /// Attributes
  protected Integer[] party;
  public static Integer Length;

  Hashtable<Node, Stats> crossing;

  Node start, end;

  /// Constructor
  BridgeCrossing(Integer[] args) {
    BridgeCrossing.Length = args.length;
    party = Arrays.copyOf(args, BridgeCrossing.Length); // The times to travel across the bridge relative to bitwise position
    start = new Node(1, true); // everyone on the left (1,1,1,1) or 15
    end = new Node(0, true); // everyone on the right (0,0,0,0) or 0

    crossing = new Hashtable<Node, Stats>(); // All the nodes and the best length of time to get there
    crossing.put(start.copy(), new Stats(0, new Node())); // 
  }

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

  public ArrayList<Node> Eat(Node n, Integer step)
  {
    ArrayList<Package> nextNodes = GetAllNextNodes(n.copy(), step%2==0);

    ArrayList<Node> nodes = new ArrayList<Node>();
    for (Package p : nextNodes)
    {
      Stats CurrStat = crossing.get(n);
      Integer newDist = p.d + CurrStat.Dist;

      //if (p.n.equals(new Node(0,0,0,0,true)) || p.n.equals(new Node(0,0,0,0,false)))
      //  Main.print("End state time = " + newDist);

      Stats s = crossing.get(p.n);
      if (s == null)
      {
        crossing.put(p.n.copy(), new Stats(newDist, n.copy()));
      }
      else if (s.Dist > newDist)
      {
        crossing.get(p.n).SetDist(newDist);
        crossing.get(p.n).SetPrev(n);
        //Main.print("updated old node " + p.n.print() + " : prev = " + n.print() + ", time = " + newDist);
      }
      else
      {
        //Main.print("slower node");
      }
      nodes.add(p.n.copy());
    }

    return nodes;
  }

  Integer CalcBestPath() {
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
          //Main.print("Surveying node " + CurrNode.print() + " | time = " + crossing.get(CurrNode.copy()).Dist);
          NextRound.addAll(Eat(CurrNode.copy(), step));

          SurveyedNodes.add(CurrNode.copy());
          NotDone = true;
        }
      }
      //Main.print("==================");

      step++;
    }
    Integer BestTime = crossing.get(end.copy()).Dist;
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
  public void q2() {
    //Main.print("2. (15 points) Write a program that takes as input the walking times for each of four people and outputs the shortest time possible for them to all cross the bridge as well as the order in which they should cross. Assume the walking times are all integer values, but do not assume that the walking times are different for each person. The program should give the shortest time for any set of four walking times. (As discussed in class, the shortest solution in the original problem is 17 minutes. What happens if the walking times are one, four, six, and ten minutes?)");
    Main.print("------------------------");
    String vals = new String();
    CalcBestPath();
    
    Main.print("------------------------");
    Main.print("\n\n");
  }
}

class Main {
  public static void print(String s) {
    System.out.println(s);
  }
  
  public static void main(String[] args) {
    // ADJUST BRIDGE CROSSING VALUES HERE
    BridgeCrossing alg = new BridgeCrossing(new Integer[]{1,2,5,10});
    alg.q2();
  }
}