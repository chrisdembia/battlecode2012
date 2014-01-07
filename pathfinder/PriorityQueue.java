package team140.pathfinder;

import battlecode.common.Direction;

/**
 * heap-based priority queue, hardcoded to operate on XYs
 * and a backed by a fixed-size array
 */
public class PriorityQueue {

  final XY[] xys;
  int count = 0;
  
  public PriorityQueue(int maxSize) {
    xys = new XY[maxSize];
  }
  
  public void add(XY xy) {
    xys[count] = xy;
    float dist = xy.approxTotalDist;
    int k = count++;
    
    //keep swapping index with parent until parent is smaller
    while (k > 0 && xys[parent(k)].approxTotalDist > dist) {
      //swap
      XY tmp = xys[parent(k)];
      xys[parent(k)] = xys[k];
      xys[k] = tmp;
      k = parent(k);
    }
  }
  
  //unchecked.  don't call it on an empty queue.
  public XY removeMin() {    
    XY min = xys[0];
    
    if (--count == 0) return min;
    
    //replace min with the last element
    xys[0] = xys[count];
    float dist = xys[0].approxTotalDist;
    
    //keep swapping with lower child until both children are higher
    int k = 0;
    boolean is_right = false;
    float dist_left = 0f, dist_right = 0f;
    while (  (           lchild(k) < count && (dist_left = xys[lchild(k)].approxTotalDist) < dist)
           | (is_right = rchild(k) < count && (dist_right = xys[rchild(k)].approxTotalDist) < dist)) 
    {
      int swap = !is_right || dist_left < dist_right ? lchild(k) : rchild(k);
      XY tmp = xys[swap];
      xys[swap] = xys[k];
      xys[k] = tmp;
      k = swap;
    }
    return min;
  }
  
  public int size() { 
    return count;
  }

  private static int lchild(int i) { return (i<<1) + 1; }
  private static int rchild(int i) { return (i<<1) + 2; }
  private static int parent(int i) { return ((i+1)>>1) - 1; }
  
  
  public static void test() {
    
    System.out.println("Testing priority queue...");
    
    PriorityQueue Q = new PriorityQueue(10);
    XY[] input = new XY[10];
    XY[] output = new XY[10];
    XY target = new XY(0,0);
    
    //make a bunch of XYs in order of approxTotalDist
    for (int i=0; i<10; i++) {
      input[i] = new XY(0, 0, Direction.OMNI, i, target);
    }
    
    //insert 5 in a weird order
    Q.add(input[3]);
    Q.add(input[2]);
    Q.add(input[9]);
    Q.add(input[0]);
    Q.add(input[1]);
    
    // pop 3
    for (int i=0; i<3; i++) {
      output[i] = Q.removeMin();
    }
    
    //insert remaining 5
    Q.add(input[6]);
    Q.add(input[4]);
    Q.add(input[5]);
    Q.add(input[8]);
    Q.add(input[7]);
    
    // pop remaining 7
    for (int i=3; i<10; i++) {
      output[i] = Q.removeMin();
    }
    
    //assert that the output is in sorted order
    boolean failed = false;
    for (int i=0; i<9; i++) {
      if (output[i].approxTotalDist >= output[i+1].approxTotalDist) {
        failed = true;
      }
    }
    if (failed) {
      System.out.println("failed.  input: " + input + " output: " + output);
    } else {
      System.out.println("Succeeded!");
    }
    
  }
}
