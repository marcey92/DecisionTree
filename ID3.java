// ECS629/759 Assignment 2 - ID3 Skeleton Code
// Author: Simon Dixon

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import com.sun.media.jfxmedia.events.NewFrameEvent;

//My imports
import java.util.LinkedList;
import java.util.Random;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

class ID3 {

	/** Each node of the tree contains either the attribute number (for non-leaf
	 *  nodes) or class number (for leaf nodes) in <b>value</b>, and an array of
	 *  tree nodes in <b>children</b> containing each of the children of the
	 *  node (for non-leaf nodes).
	 *  The attribute number corresponds to the column number in the training
	 *  and test files. The children are ordered in the same order as the
	 *  Strings in strings[][]. E.g., if value == 3, then the array of
	 *  children correspond to the branches for attribute 3 (named data[0][3]):
	 *      children[0] is the branch for attribute 3 == strings[3][0]
	 *      children[1] is the branch for attribute 3 == strings[3][1]
	 *      children[2] is the branch for attribute 3 == strings[3][2]
	 *      etc.
	 *  The class number (leaf nodes) also corresponds to the order of classes
	 *  in strings[][]. For example, a leaf with value == 3 corresponds
	 *  to the class label strings[attributes-1][3].
	 **/
	class TreeNode {

		TreeNode[] children;
		int value;

		public TreeNode(TreeNode[] ch, int val) {
			value = val;
			children = ch;
		} // constructor

		public String toString() {
			return toString("");
		} // toString()
		
		String toString(String indent) {
			if (children != null) {
				String s = "";
				for (int i = 0; i < children.length; i++)
					s += indent + data[0][value] + "=" +
							strings[value][i] + "\n" +
							children[i].toString(indent + '\t');
				return s;
			} else
				return indent + "Class: " + strings[attributes-1][value] + "\n";
		} // toString(String)

	} // inner class TreeNode

	private int attributes; 	// Number of attributes (including the class)
	private int examples;		// Number of training examples
	private TreeNode decisionTree;	// Tree learnt in training, used for classifying
	private String[][] data;	// Training data indexed by example, attribute
	private String[][] strings; // Unique strings for each attribute
	private int[] stringCount;  // Number of unique strings for each attribute

	public ID3() {
		attributes = 0;
		examples = 0;
		decisionTree = null;
		data = null;
		strings = null;
		stringCount = null;
	} // constructor
	
	public void printTree() {
		if (decisionTree == null)
			error("Attempted to print null Tree");
		else
			System.out.println(decisionTree);
	} // printTree()

	/** Print error message and exit. **/
	static void error(String msg) {
		System.err.println("Error: " + msg);
		System.exit(1);
	} // error()

	static final double LOG2 = Math.log(2.0);
	
	static double xlogx(double x) {
		return x == 0? 0: x * Math.log(x) / LOG2;
	} // xlogx()


	/*
	*  -10 <- no children at node
	*  
	*/
	public int myclassify(TreeNode node,String[] example){
		// if no children exist then return value
		//System.out.println("node.value <- " +node.value);
		if(node.children == null){
			// end of tree
			return node.value; 
		}
		else{
			int a = node.value;
			String exA = example[a];
			for(int i=0; i<stringCount[a]; i++){
				String compareTo = strings[a][i];
				if(compareTo.equals(exA)){
					//followNode
					TreeNode toExplore = node.children[i];
					return myclassify(toExplore, example);
				}
			}
		}
		return -1;
	}//myclassify()

	/** Execute the decision tree on the given examples in testData, and print
	 *  the resulting class names, one to a line, for each example in testData.
	 **/
	public void classify(String[][] testData) {
		if (decisionTree == null)
			error("Please run training phase before classification");
		//String[][] examples = removeHeader(testData);
		for(int i=1; i<testData.length; i++){
			int ans = myclassify(decisionTree, testData[i]);
			for(int j=0; j<stringCount[attributes-1]; j++){
				if(ans == j)
					System.out.println(strings[attributes-1][j]);
			}
		}
	} // classify()

	public boolean isPure(String[][] examples){
		String[] firstEx = examples[0];
		for(String[] ex: examples)
			if(firstEx[attributes-1].equals(ex[attributes-1]) == false)
				return false;
		// for each example
		return true;
	}
	public TreeNode decisionTreeLearning(String[][] examples, HashSet<Integer> attribs, int whenEmpty){
		if(examples.length == 0){
			return new TreeNode(null,whenEmpty);
		}
		
		//do all examples have the same class?
		if(isPure(examples)){
			int mv = majorityValue(examples);
			return new TreeNode(null, mv);
		}
		else if(attribs.isEmpty()){
			int mv = majorityValue(examples);
			return new TreeNode(null, mv);
		}
		else{
			int bestA = bestAttribute(attribs, examples);
			int aLength = stringCount[bestA];
			TreeNode tree = new TreeNode(new TreeNode[aLength], bestA);
			int mv = majorityValue(examples);
			
			attribs.remove(bestA);

			for(int i=0; i< aLength; i++){
				String[][] subArray = subsetByAttrib(examples, bestA, i);
				TreeNode subTree = decisionTreeLearning(subArray, attribs, mv);
				tree.children[i] = subTree;
			}
			attribs.add(bestA);
			return tree;
		}
	}

	public float entropy(String[][] examples){
		//TODO
		float[] classSum = new float[attributes.length];

		for each example{
			
			for class. in classes{
				
				if example[attributes-1] == class.
					classSum ++;

			}
		}

		return 0f;
	}

	public float gain(float[] entropies, float[] weights, float root){
		//TODO
		return 0f;
	}

	public String[][] subsetByAttrib(String[][] examples, int attrib, int clss){
		LinkedList<String[]> subList = new LinkedList<String[]>();
				for(String[] ex: examples){
					String exA = ex[attrib];
					String compareTo = strings[attrib][clss];
					if(exA.equals(compareTo)){
						subList.push(ex);
					}
				}
		return subList.toArray(new String[0][]);
	}

	public int bestAttribute(HashSet<Integer> attribs , String[][] examples){
		
		float root = entropy(examples);

		LinkedList<Float> gainsList = new LinkedList<Float>();
		LinkedList<Integer> attribList = new LinkedList<Integer>();

		float[] gainArr = new float[attribs.size()];

		for(int a: attribs){
			attribList.push(a);
			
			//for one attribute
			float[] weights = new float[stringCount[a]];
			float[] entropies = new float[stringCount[a]];

			for(int j=0; j<stringCount[a]; j++ ){
				
				String[][] subset = subsetByAttrib(examples, a, j);

				weights[j] = subset.length / examples.length;
				
				entropies[j] = entropy(subset);
			}//end for
			
			float gain = gain(entropies, weights, root);
			
			gainsList.push(gain);
		}// end attributes


		Iterator<Integer> attribIter = attribList.iterator();
		Iterator<Float> gainIter = gainsList.iterator();

		int bestAttrib = attribList.peek();
		float bestGain = Float.MIN_VALUE;
		while(attribIter.hasNext() & gainIter.hasNext()){
			int currentAttrib = attribIter.next();
			float currentGain =  gainIter.next();
			if(currentGain>bestGain){
				bestGain = currentGain;
				bestAttrib = currentAttrib;
			}
		}
		return bestAttrib;



		// //CURRENTY RANDOM TODO
		// if(attributes.size() == 1){
		// 	for(int i: attributes){
		// 		return i;
		// 	}
		// }
		// Random r = new Random();
		// int a = r.nextInt(attributes.size()-1); //temp
		// for(int i: attributes){
		// 	if(a-- == 0)
		// 		return i;
		// }
		// return 0;// for now
	}
	public int majorityValue(String[][] examples){
		//TO DO IF MORE THAN ONE CLASS
		int[] classCount = new int[stringCount[attributes-1]];
		for (int c: classCount)
			c = 0;
		
		for(int i=0; i< classCount.length; i++){
			String compareTo = strings[attributes-1][i];
			for(String[] ex: examples){
				String exClass = ex[attributes-1];
				if(compareTo.equals(exClass))
					classCount[i] ++;
			}
		}
		
		return indexOfMax(classCount); // for now
	}

	public int indexOfMax(int[] arr){
		int index = -1; // if length is 0 ... should not be Will Through error so maybe change
		int max = Integer.MIN_VALUE;
		for(int i=0; i<arr.length;i++){
			if(arr[i]>max){
				max=arr[i];
				index = i;
			}
		}
		return index;
	}

	public String[][] removeHeader(String[][] arr){
		String[][] noHeader = new String[arr.length-2][];
		for(int i=0; i<noHeader.length; i++){
			noHeader[i] = arr[i+1];
		}
		return noHeader;
	}
	public void train(String[][] trainingData) {
		indexStrings(trainingData);

		//create attributes
		HashSet<Integer> attribs = new HashSet<Integer>();
		for(int i=0; i<attributes-1; i++)
			attribs.add(i);
		
		String[][] justExamples = removeHeader(trainingData);

		decisionTree = decisionTreeLearning(justExamples, attribs, 0);

	} // train()

	/** Given a 2-dimensional array containing the training data, numbers each
	 *  unique value that each attribute has, and stores these Strings in
	 *  instance variables; for example, for attribute 2, its first value
	 *  would be stored in strings[2][0], its second value in strings[2][1],
	 *  and so on; and the number of different values in stringCount[2].
	 **/
	void indexStrings(String[][] inputData) {
		data = inputData;
		examples = data.length;
		attributes = data[0].length;
		stringCount = new int[attributes];
		strings = new String[attributes][examples];// might not need all columns
		int index = 0;
		for (int attr = 0; attr < attributes; attr++) {
			stringCount[attr] = 0;
			for (int ex = 1; ex < examples; ex++) {
				for (index = 0; index < stringCount[attr]; index++)
					if (data[ex][attr].equals(strings[attr][index]))
						break;	// we've seen this String before
				if (index == stringCount[attr])		// if new String found
					strings[attr][stringCount[attr]++] = data[ex][attr];
			} // for each example
		} // for each attribute
	} // indexStrings()

	/** For debugging: prints the list of attribute values for each attribute
	 *  and their index values.
	 **/
	void printStrings() {
		for (int attr = 0; attr < attributes; attr++)
			for (int index = 0; index < stringCount[attr]; index++)
				System.out.println(data[0][attr] + " value " + index +
									" = " + strings[attr][index]);
	} // printStrings()
		
	/** Reads a text file containing a fixed number of comma-separated values
	 *  on each line, and returns a two dimensional array of these values,
	 *  indexed by line number and position in line.
	 **/
	static String[][] parseCSV(String fileName)
								throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String s = br.readLine();
		int fields = 1;
		int index = 0;
		while ((index = s.indexOf(',', index) + 1) > 0)
			fields++;
		int lines = 1;
		while (br.readLine() != null)
			lines++;
		br.close();
		String[][] data = new String[lines][fields];
		Scanner sc = new Scanner(new File(fileName));
		sc.useDelimiter("[,\n]");
		for (int l = 0; l < lines; l++)
			for (int f = 0; f < fields; f++)
				if (sc.hasNext())
					data[l][f] = sc.next();
				else
					error("Scan error in " + fileName + " at " + l + ":" + f);
		sc.close();
		return data;
	} // parseCSV()

	public static void main(String[] args) throws FileNotFoundException,
												  IOException {
		if (args.length != 2)
			error("Expected 2 arguments: file names of training and test data");
		String[][] trainingData = parseCSV(args[0]);
		String[][] testData = parseCSV(args[1]);
		ID3 classifier = new ID3();
		classifier.train(trainingData);
		//classifier.printStrings();
		classifier.printTree();
		classifier.classify(testData);
	} // main()

} // class ID3
