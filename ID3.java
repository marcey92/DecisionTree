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
	* Recursive classifier
	*/
	public int classifier(TreeNode node,String[] example){
		if(node.children == null){ /* reached leaf */
			return node.value; 
		}
		else{ 
			int attribute = node.value;
			String exA = example[attribute];
			for(int i=0; i<stringCount[attribute]; i++){
				String compareTo = strings[attribute][i];
				if(compareTo.equals(exA)){ 
					TreeNode toExplore = node.children[i];
					return classifier(toExplore, example);
				}
			}
		}
		return -1;  /* fail statement */
	}//classifier()

	/** Execute the decision tree on the given examples in testData, and print
	 *  the resulting class names, one to a line, for each example in testData.
	 **/
	public void classify(String[][] testData) {
		if (decisionTree == null)
			error("Please run training phase before classification");
		for(int i=1; i<testData.length; i++){
			int ans = classifier(decisionTree, testData[i]);
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
		return true;
	} // isPure()

	/*
	*  My decisionTreeLearning algorithm. Recursive
	*/
	public TreeNode decisionTreeLearning(String[][] examples, HashSet<Integer> attribs, int whenEmpty){
		if(examples.length == 0){ 
			return new TreeNode(null,whenEmpty); /* if no examples return leaf with majority class */
		}
		
		if(isPure(examples)){ 
			int mv = majorityClass(examples);
			return new TreeNode(null, mv);       /* if examples of same class return leaf that class */
		}
		else if(attribs.isEmpty()){
			int mv = majorityClass(examples);
			return new TreeNode(null, mv);       /* if all atributes tested return lead with majorty class */
		}
		else{									 /* otherwise pick nest attribute */
			int bestA = bestAttribute(attribs, examples); 
			int aLength = stringCount[bestA];
			TreeNode tree = new TreeNode(new TreeNode[aLength], bestA); 
			int mv = majorityClass(examples);   
			
			attribs.remove(bestA); /* remove best attribute  */

			/*
			*  for each class of attribute calculate new branch and
			*  add branch to children of tree
			*/
			for(int i=0; i< aLength; i++){
				String[][] subArray = subsetByAttribClass(examples, bestA, i); 
				TreeNode branch = decisionTreeLearning(subArray, attribs, mv); 
				tree.children[i] = branch;  
			}
			attribs.add(bestA);  /* push best attribute  */
			return tree; 
		}
	}

	/*
	*  entropy = (for each class) -(subset/examples)log(subset/examples)
	*/
	public double entropy(String[][] examples){
		double denominator = examples.length;

		double[] classCount = new double[stringCount[attributes-1]];
		for(double c: classCount){
			c = 0d; 					
		}

		for(String[] ex: examples){
			for(int i=0; i<stringCount[attributes-1]; i++){	
				String exA = ex[attributes-1];
				String compareTo = strings[attributes-1][i];
				if(compareTo.equals(exA)){ 	/* if example belongs to class ++ */
					classCount[i]++;
				}
			} // for each class
		} // for each example
		double entropy = 0d;
		for(double numerator: classCount ){
			if(numerator!=0) 				/* do not calculate if 0, resuts in NaN */
				entropy += -xlogx(numerator/denominator);
		}
		return entropy;
	} // entropy()

	/* 
	*  gain = root +  (for each entopy) -weight*entropy 
	*/ 
	public double gain(double[] entropies, double[] weights, double root){		
		double sum = 0d;
		for(int i=0; i<entropies.length; i++){
 			double v = -weights[i]*entropies[i]; 	
			sum +=  v; 
		}
		double gain = root + sum;
		return gain;
	} // gain()

	public String[][] subsetByAttribClass(String[][] examples, int attrib, int clss){
		LinkedList<String[]> subList = new LinkedList<String[]>();
				for(String[] ex: examples){
					String exA = ex[attrib];
					String compareTo = strings[attrib][clss];
					if(exA.equals(compareTo)){
						subList.push(ex);
					}
				}
		return subList.toArray(new String[0][]);
	} // subsetByAttribClass()

	/* 
	*  return the best attribute by calculating entropy and gain
	*/ 
	public int bestAttribute(HashSet<Integer> attribs , String[][] examples){
		
		double root = entropy(examples);

		LinkedList<Double> gainsList = new LinkedList<Double>();
		LinkedList<Integer> attribList = new LinkedList<Integer>();

		/* 
		*  computes gain for each attrbute
		*/ 
		for(int a: attribs){ 
			attribList.push(a); 		   
			
			double[] weights = new double[stringCount[a]];
			double[] entropies = new double[stringCount[a]];

			for(int j=0; j<stringCount[a]; j++ ){
				String[][] subset = subsetByAttribClass(examples, a, j); 		
				weights[j] = (double) subset.length / (double) examples.length; 
				entropies[j] = entropy(subset);									
			} // for each class of attribute
			
			double gain = gain(entropies, weights, root); 
			
			gainsList.push(gain);		    
		}// for each attributes


		Iterator<Integer> attribIter = attribList.iterator();
		Iterator<Double> gainIter = gainsList.iterator();
		int bestAttrib = attribList.peek();
		double bestGain = Double.MIN_VALUE;
		
		/* 
		*  iterate over attibutes and gains to find best attribute
		*/ 
		while(attribIter.hasNext() & gainIter.hasNext()){
			int currentAttrib = attribIter.next();
			double currentGain =  gainIter.next();
			if(currentGain>bestGain){
				bestGain = currentGain;
				bestAttrib = currentAttrib;
			}
		}
		return bestAttrib;
	} // bestAttribute()

	public int majorityClass(String[][] examples){
		int[] classCount = new int[stringCount[attributes-1]];
		for (int c: classCount)
			c = 0;							   
		
		for(int i=0; i< classCount.length; i++){
			String compareTo = strings[attributes-1][i];
			for(String[] ex: examples){
				String exClass = ex[attributes-1];
				if(compareTo.equals(exClass))  /* if match increase count */ 
					classCount[i] ++; 
			} // for each example
		} //for each class
		
		return indexOfMax(classCount); 
	} // majorityClass()

	public int indexOfMax(int[] arr){
		int index = -1;
		int max = Integer.MIN_VALUE;
		for(int i=0; i<arr.length;i++){
			if(arr[i]>max){
				max=arr[i];
				index = i;
			}
		}
		return index;
	} // indexOfMax()

	public String[][] removeHead(String[][] arr){
		String[][] noHeader = new String[arr.length-1][];
		for(int i=0; i<noHeader.length; i++)
			noHeader[i] = arr[i+1];
		return noHeader;
	} // removeHead()

	/*
	*  My train method
	*/
	public void train(String[][] trainingData) {
		indexStrings(trainingData);

		/*
		*  Hashset holds attributes 
		*/
		HashSet<Integer> attribs = new HashSet<Integer>();
		for(int i=0; i<attributes-1; i++)
			attribs.add(i);
		// for each attribute
		
		/*
		*  removes the header from trainingData becuase
		*  it affects the entropy score
		*/
		String[][] justExamples = removeHead(trainingData);

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
