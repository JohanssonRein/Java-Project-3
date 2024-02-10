import java.io.Serializable;
import java.util.ArrayList;
import java.text.*;
import java.lang.Math;

public class DecisionTree implements Serializable {

	DTNode rootDTNode;
	int minSizeDatalist; //minimum number of datapoints that should be present in the dataset so as to initiate a split

	// Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
	public static final long serialVersionUID = 343L;

	public DecisionTree(ArrayList<Datum> datalist , int min) {
		minSizeDatalist = min;
		rootDTNode = (new DTNode()).fillDTNode(datalist);
	}

	class DTNode implements Serializable{
		//Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
		public static final long serialVersionUID = 438L;
		boolean leaf;
		int label = -1;      // only defined if node is a leaf
		int attribute; // only defined if node is not a leaf
		double threshold;  // only defined if node is not a leaf

		DTNode left, right; //the left and right child of a particular node. (null if leaf)

		DTNode() {
			leaf = true;
			threshold = Double.MAX_VALUE;
		}


		// this method takes in a datalist (ArrayList of type datum). It returns the calling DTNode object
		// as the root of a decision tree trained using the datapoints present in the datalist variable and minSizeDatalist.
		// Also, KEEP IN MIND that the left and right child of the node correspond to "less than" and "greater than or equal to" threshold
		DTNode fillDTNode(ArrayList<Datum> datalist) {


			// the labelled data set has at least k (minSizeDatalist) data items
			if (datalist.size() >= minSizeDatalist) {

				// for loop to check the condition for the if statement below
				// this checks if datalist contains datums of only one label
				boolean containOnlyOneLabel = true;
				Datum firstValue = datalist.get(0);
				for (int i = 0; i < datalist.size(); i++) {

					if (datalist.get(i).y != firstValue.y) {
						containOnlyOneLabel = false;
					}
				}

				// all the data items have the same label
				if (containOnlyOneLabel) {
					// create a leaf node with that class label and return it
					DTNode leafNode = new DTNode();
					leafNode.label = firstValue.y; // since all datums have same label any labels is fine
					leafNode.left = null;
					leafNode.right = null;
					return leafNode;
				}

				// create a “best” attribute test question
				else {

					double[] result = findBestSplit(datalist);

					// create a new node and store the attribute test in that node, namely attribute and threshold
					DTNode newNode = new DTNode();
					newNode.leaf = false; // DTNode constructor creates a leaf by default
					newNode.attribute = (int) result[0];
					newNode.threshold = result[1];


                /* split the set of data items into two subsets, data1 and data2,
                according to the answers to the test question (from 1.1) */
					ArrayList<Datum> data1 = new ArrayList<Datum>();
					ArrayList<Datum> data2 = new ArrayList<Datum>();

					for (Datum dataPoint : datalist) {
						if (dataPoint.x[newNode.attribute] < newNode.threshold) {
							data1.add(dataPoint);
						}
						else {
							data2.add(dataPoint);
						}
					}


					if (data1.isEmpty() || data2.isEmpty()) {
						newNode.leaf = true;
						newNode.label = findMajority(datalist);
						newNode.left = null;
						newNode.right = null;
						return newNode;
					}

					// recursion part
					newNode.left = fillDTNode(data1);
					newNode.right = fillDTNode(data2);

					return newNode;
				}
			}
			else {
				// create a leaf node with label equal to the majority of labels and return it
				DTNode leafNode = new DTNode();
				leafNode.label = findMajority(datalist);
				leafNode.left = null;
				leafNode.right = null;
				return leafNode;
			}
		}

		// This is a helper method. Given a datalist, this method returns the label that has the most
		// occurrences. In case of a tie it returns the label with the smallest value (numerically) involved in the tie.
		int findMajority(ArrayList<Datum> datalist) {

			int [] votes = new int[2];

			//loop through the data and count the occurrences of datapoints of each label
			for (Datum data : datalist)
			{
				votes[data.y]+=1;
			}

			if (votes[0] >= votes[1])
				return 0;
			else
				return 1;
		}

		// This method takes in a datapoint (excluding the label) in the form of an array of type double (Datum.x) and
		// returns its corresponding label, as determined by the decision tree
		int classifyAtNode(double[] xQuery) {

			if (this.leaf == true) {
				return this.label;
			}

			else {
				if (xQuery[this.attribute] < this.threshold) {

					return this.left.classifyAtNode(xQuery);
				}
				else {

					return this.right.classifyAtNode(xQuery);
				}
			}

			//return -1; //dummy code.  Update while completing the assignment.
		}

		//given another DTNode object, this method checks if the tree rooted at the calling DTNode is equal to the tree rooted
		//at DTNode object passed as the parameter
		public boolean equals(Object dt2)
		{
			DTNode dtNode2 = (DTNode) dt2;


			if (this == null && dtNode2 == null) {
				return true;
			}

			if (this == null || dtNode2 == null) {
				return false;
			}

			if (this.leaf == true && dtNode2.leaf == true) {
				return this.label == dtNode2.label;
			}

			if (this.leaf == false && !dtNode2.leaf) {
				return this.threshold == dtNode2.threshold && this.attribute == dtNode2.attribute && this.left.equals(dtNode2.left) && this.right.equals(dtNode2.right);

			}

			return false; //dummy code.  Update while completing the assignment.
		}


	}



	//Given a dataset, this returns the entropy of the dataset
	double calcEntropy(ArrayList<Datum> datalist) {
		double entropy = 0;
		double px = 0;
		float [] counter= new float[2];
		if (datalist.size()==0)
			return 0;
		double num0 = 0.00000001,num1 = 0.000000001;

		//calculates the number of points belonging to each of the labels
		for (Datum d : datalist)
		{
			counter[d.y]+=1;
		}
		//calculates the entropy using the formula specified in the document
		for (int i = 0 ; i< counter.length ; i++)
		{
			if (counter[i]>0)
			{
				px = counter[i]/datalist.size();
				entropy -= (px*Math.log(px)/Math.log(2));
			}
		}

		return entropy;
	}


	// given a datapoint (without the label) calls the DTNode.classifyAtNode() on the rootnode of the calling DecisionTree object

	// Given a datapoint (without the label), predicts the label of the datapoint
    /* The only difference between this method and classifyAtNode() is that classifyAtNode()
    does the classification on its member DTNode,
    whereas for classify() the DTNode is the root of the created decision tree. */

	int classify(double[] xQuery ) {
		return this.rootDTNode.classifyAtNode( xQuery );
	}

	String checkPerformance( ArrayList<Datum> datalist) {
		DecimalFormat df = new DecimalFormat("0.000");
		float total = datalist.size();
		float count = 0;

		for (int s = 0 ; s < datalist.size() ; s++) {
			double[] x = datalist.get(s).x;
			int result = datalist.get(s).y;
			if (classify(x) != result) {
				count = count + 1;
			}
		}

		return df.format((count/total));
	}


	//Given two DecisionTree objects, this method checks if both the trees are equal by
	//calling onto the DTNode.equals() method
	public static boolean equals(DecisionTree dt1,  DecisionTree dt2)
	{
		boolean flag = true;
		flag = dt1.rootDTNode.equals(dt2.rootDTNode);
		return flag;
	}

	private double[] findBestSplit(ArrayList<Datum> datalist) {
		double bestAverageEntropy = Double.MAX_VALUE;
		int bestAttribute = -1;
		double bestThreshold = -1;
		for (int i = 0; i < datalist.get(0).x.length; i++) {

			for (int j = 0; j < datalist.size(); j++) {

				// initializing datalist1 and 2
				ArrayList<Datum> dataOnTheLeft = new ArrayList<>();
				ArrayList<Datum> dataOnTheRight = new ArrayList<>();

				// split data
				for (int k = 0; k < datalist.size(); k++) {
					if (datalist.get(k).x[i] < datalist.get(j).x[i]) {
						dataOnTheLeft.add(datalist.get(k));
					}
					else {
						dataOnTheRight.add(datalist.get(k));
					}
				}

				double leftEntropy = calcEntropy(dataOnTheLeft);
				double rightEntropy = calcEntropy(dataOnTheRight);
				double omegaLeft = (double) dataOnTheLeft.size() / datalist.size();
				double omegaRight = (double) dataOnTheRight.size() / datalist.size();
				double currentAverageEntropy = omegaLeft * leftEntropy + omegaRight * rightEntropy;

				if (bestAverageEntropy > currentAverageEntropy) {
					bestAverageEntropy = currentAverageEntropy;
					bestAttribute = i;
					bestThreshold = datalist.get(j).x[i];
				}
			}
		}

		double[] result = {bestAttribute, bestThreshold};
		return result;
	}



}


