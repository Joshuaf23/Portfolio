//BTree manager to create binary trees, add to binary trees in order, and print binary trees
//Prints in order, pre order, and post order
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Scanner;

//class for binary tree manager
class BTreeManager<T extends Comparable<T>> {
    
	//node class for binary tree
	public static class Node<T> {
    	protected T nodeValue; //value that goes inside the node
    	protected Node<T> left; //pointer to next node
    	protected Node<T> right;

    	//constructor for node
    	public Node(T x) {
        	nodeValue = x; //sets node value to given value
        	left = null; //initially sets left to null
        	right = null; //initially sets left to null
    	}//end of node constructor
	}//end of Node

	protected Node<T> head; //pointer to head of binary tree
	protected int size; //keeps track of size of binary tree
    
	//constructor for binary tree
	public BTreeManager() {
    	Node<T> head = null; //initially sets head to null
    	int size = 0; //initial size is 0
	}//end of BTree manager constructor

	//function to add node to the binary tree
	public void Add (T x) {
    	Node<T> newNode = new Node<T> (x); //creates a node with the given data
    	Node<T> currentNode; //placeholder for nodes when going through binary tree
    	Node<T> previousNode = null; //another placeholder for nodes when going through binary tree

    	if (head == null) { //if nothing is in list, then set head to new node
        	head = newNode;
        	size++; //add one to size of binary tree
    	} else { //need to find where to put node
        	currentNode = head; //starts at head
       	 
        	while (currentNode != null) { //goes through binary tree until it finds spot to put new node
            	if (newNode.nodeValue.compareTo(currentNode.nodeValue) < 0) { //left if less than
                	previousNode = currentNode;
                	currentNode = currentNode.left;
            	} else { //right if greater than
                	previousNode = currentNode;
                	currentNode = currentNode.right;
            	} //end of if
        	}//end of while

        	//places new node on current node and links to previous node
        	if (newNode.nodeValue.compareTo(previousNode.nodeValue) < 0) { //left if less than
            	previousNode.left = newNode;
        	} else { //right if greater than
            	previousNode.right = newNode;
        	} //end of if
        	size++; //add one to size of binary tree
    	}//end of if
	}//end of Add

	//recursive function to print BTree in order (left node right)
	public void printInOrder(Node<T> root, PrintWriter outputf) {
    	if (root == null) {return;} //return point
    	else {
        	//goes to the left
        	printInOrder(root.left, outputf);

        	//prints value, or uses the student print if it is a student object
        	if (root.nodeValue instanceof Student) {
            	((Student) root.nodeValue).StudentPrint();
        	} else {
            	outputf.println(root.nodeValue + " ");
        	} //end of if

        	//goes to the right one, then repeats process
        	printInOrder(root.right, outputf);
    	} //end of if
	} //end of printInOrder

	//recursive function to print BTree post order (left right node)
	public void printPostOrder(Node<T> root, PrintWriter outputf) {
    	if (root == null) {return;}
    	else {
        	//goes to the left
        	printPostOrder(root.left, outputf);

        	//goes to the right one, then continues process
        	printPostOrder(root.right, outputf);

        	//prints value, or uses the student print if it is a student object
        	if (root.nodeValue instanceof Student) {
            	((Student) root.nodeValue).StudentPrint();
        	} else {
            	outputf.println(root.nodeValue + " ");
        	} //end of if
    	} //end of if
	} //end of printPostOrder

	//recursive function to print BTree pre order (node left right)
	public void printPreOrder(Node<T> root, PrintWriter outputf) {
    	if (root == null) {return;}
    	else {
        	//prints value, or uses the student print if it is a student object
        	if (root.nodeValue instanceof Student) {
            	((Student) root.nodeValue).StudentPrint();
        	} else {
            	outputf.println(root.nodeValue + " ");
        	} //end of if
       	 
        	//goes to the left
        	printPreOrder(root.left, outputf);

        	//goes to the right one, then continues process
        	printPreOrder(root.right, outputf);
    	} //end of if
	} //end of printPreOrder
} //end of BTreeManager class

//Student object that also has gpa, total hours, year rank, current gpa, and cumulative gpa
class Student implements Comparable<Student> {
	protected int test1; //score for test 1
	protected int test2; //score for test 2
	protected int test3; //score for test 3
	protected int total; //total score
	protected String name; //student name
	protected String id; //student id
	protected PrintWriter outputf; //printwriter to write output to a file
	protected float gpa; //gpa

	//constructor for student
	public Student (String id, String name, int test1, int test2, int test3, int total, float gpa, PrintWriter outputf) {
    	this.test1 = test1;
    	this.test2 = test2;
    	this.test3 = test3;
    	this.id = id;
    	this.name = name;
    	this.outputf = outputf;
    	this.total = total;
    	this.gpa = gpa;
	}//end of Student constructor

	//print function that writes to output file
	public void StudentPrint() {
    	outputf.printf("Name: %s, ID: %s, Test 1: %d, Test 2: %d Test 3: %d, Total: %d, HrGPA: %.2f\n", name, id, test1, test2, test3, total, gpa);
	}//end of StudentPrint

	//compareTo function for placing in BTree
	public int compareTo(Student other) {
    	double difference = this.gpa - other.gpa;
    	int result = 0;
    	if (difference < 0) {
        	result = -1;
    	} else if (difference > 0) {
        	result = 1;
    	} //end of if
    	return result;
	}//end of compareTo
}//end of Student

public class Assignment5 {
	public static void main(String[] args) {
   	 
    	//variables are same as those in Student object
    	String id;
    	int test1;
    	int test2;
    	int test3;
    	String name;
    	int total;
    	float gpa;

    	//create new BTree for students
    	BTreeManager<Student> Student_BTree = new BTreeManager<Student>();

    	//sets up a printwriter to output.txt file
    	File myData = new File("output.txt");
    	PrintWriter outputf;
    	try {
        	outputf = new PrintWriter(myData);
   	 

        	//try-catch for a scanner for input.txt
        	try {
            	//sets up scanner pointing to input.txt file
            	Scanner input = new Scanner(new File("input.txt"));
            	input.nextLine(); //first line is unnecessary because of output format
            	//goes through input file and transfers data to student object, then stores object in BTree
            	while (input.hasNext()) {
                	id = input.next();
                	name = input.next();
                	test1 = input.nextInt();
                	test2 = input.nextInt();
                	test3 = input.nextInt();
                	total = input.nextInt();
                	gpa = input.nextFloat();
                	Student temp = new Student(id, name, test1, test2, test3, total, gpa, outputf);
                	Student_BTree.Add(temp);
            	} //end of while
            	input.close();
        	} catch (FileNotFoundException e) {
            	System.err.println("File not found.");
            	System.exit(1);
        	} catch (NoSuchElementException elementException) {
            	System.err.println("File improperly formatted.");
            	System.exit(1);
        	} catch (IllegalStateException stateException) {
            	System.err.println("Error reading from file.");
            	System.exit(1);
        	} //end of try-catch

        	//calls function to print BTree in order
        	outputf.println("Printing Student BTree in order");
        	outputf.println("------------------------------");
        	Student_BTree.printInOrder(Student_BTree.head, outputf);
        	//calls function to print BTree pre order
        	outputf.println("\nPrinting Student BTree pre order");
        	outputf.println("------------------------------");
        	Student_BTree.printPreOrder(Student_BTree.head, outputf);
        	//calls function to print BTree post order
        	outputf.println("\nPrinting Student BTree post order");
        	outputf.println("------------------------------");
        	Student_BTree.printPostOrder(Student_BTree.head, outputf);

        	outputf.close();

    	} catch (FileNotFoundException e) {
        	System.err.println("File not found.");
        	System.exit(1);
    	} //end of try-catch
	} //end of main
} //end of Assignment 5
