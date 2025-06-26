//BTree manager to create binary trees, add to binary trees in order, and print binary trees
//Prints in order, pre order, and post order

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

//generic class to create a heap, place values in the heap, and print the heap
class heapManager <T extends Comparable> {
    protected ArrayList<T> heap = new ArrayList<T>(); //array list for heap
    protected int next; //next value in heap
    protected int size; //size of heap

    //constructor for heapManager
    public heapManager() {
        next = 0;
        size = 0;
    } //end of heapManager constructor

    //function to add a value to the heap
    public void Add(T x) {
        int current; //current place in the heap
        int parent; //place of parent node in the heap
        T save; //saves value in node when swapping

        //adds the value and increments size and next
        heap.add(next, x);
        size++;
        next++;

        //compares with parent value to place value in correct spot
        current = next-1;
        parent = (current-1)/2;
        while(heap.get(current).compareTo(heap.get(parent)) > 0) {
            save = heap.get(current);
            heap.set(current, heap.get(parent));
            heap.set(parent, save);
            current = parent;
            parent = (current-1)/2;
        }
    }

    public void Print(PrintWriter outputf) {
        for (int i = 0; i <= heap.size()-1; i++) {
            if (heap.get(i) instanceof Student) {
                ((Student) heap.get(i)).StudentPrint();
            } else {
                outputf.println(heap.get(i));
            }
        }
    }
} //end of heapManager

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

public class Assignment6 {
    public static void main(String[] args) {
        
        //variables are same as those in Student object
        String id;
        int test1;
        int test2;
        int test3;
        String name;
        int total;
        float gpa;

        //create new heap for students
        heapManager<Student> Student_Heap = new heapManager<Student>();

        //sets up a printwriter to output.txt file
        File myData = new File("output.txt");
        PrintWriter outputf;
        //try-catch for printwriter
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
                    Student_Heap.Add(temp);
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

            //calls function to print heap
            outputf.println("Printing Student Heap");
            outputf.println("------------------------------");
            Student_Heap.Print(outputf);

            outputf.close();

        } catch (FileNotFoundException e) {
            System.err.println("File not found.");
            System.exit(1);
        } //end of try-catch
    } //end of main
} //end of Assignment 5
