public class DLB{
Node root;
public DLB(){
  root=null;
}
public boolean add(String k){
  return true;
}
}
class Node{
  char letter;
  Node right;
  Node down;
  public Node(char c){
    letter = c;
    right=null;
    down=null;
  }
}
