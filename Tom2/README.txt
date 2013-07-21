

public class Stack {


int value;
Stack next;
Stack top;

public int getValue() { return value; }

public Stack(int v) {
  value=v;
}

public Stack pop() {

 Stack rv = top.next;
 top = top.next;
 return rv;
}


public void push(int value) {

Stack temp = top==null? null : top.next;


top = new Stack(value);
top.next = temp;


}


public static void main(String []a) {


    Stack stack = new Stack();
    stack.push(3);
    System.err.println(stack.pop().getValue());


}







}