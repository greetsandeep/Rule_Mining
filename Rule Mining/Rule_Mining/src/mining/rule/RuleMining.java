package mining.rule;

import java.io.*;
import java.util.*;

/**
 * @author Sandeep,Snehal,Poojitha
 *	AIM : To Use hash tree and come up with interesting association rules by using apriori algorithm.
 */
public class RuleMining {
	/** An ArrayList of Integer Arrays which store the stands of a candidate in a binary format*/
	public static ArrayList<int[]> data = new ArrayList<int[]>(); 
	public static ArrayList<ArrayList<TreeSet<Integer>>> itemsets = new ArrayList<ArrayList<TreeSet<Integer>>>();
	public static ArrayList<ArrayList<Integer>> data_attr = new ArrayList<ArrayList<Integer>>();
	public static void main(String args[]){
		try{
			inputHandle("dataFile.txt",data);
		}catch(Exception e){
			System.out.println("Error In file Reading " + e);
		}

		Scanner in = new Scanner(System.in);
		System.out.println("Enter The Value for Minimum Support:\n");
		double minSupport = in.nextDouble();
		System.out.println("Enter The value for Minimum Confidence:\n");
		double minConfidence = in.nextDouble();

		long startTime = System.currentTimeMillis();

		DataRef dref = new DataRef();

		/** Contains all the sets of one Frequent Items based on the given minimum Support Value*/
		ArrayList<TreeSet<Integer>> oneFreq = oneFrequentItemSet(data,dref,minSupport);
		itemsets.add(0,oneFreq);

		RuleMining ref = new RuleMining();

		data_attr = attributeRepresentation(data);
		for(int i=2;i<=16;i++)
		{   
			System.out.println(i);
			HashTree root = new HashTree(16-i,0);
			kminus1tok(ref,itemsets.get(i-2),i);
			for(int j=0;j<itemsets.get(i-1).size();j++)
				root.hashItemset(itemsets.get(i-1).get(j));
			
			for(int j=0;j<data_attr.size();j++)
			{
				ArrayList<ArrayList<Integer>> temp =  new ArrayList<ArrayList<Integer>>();
				temp = ref.tranBreakdown(data_attr.get(j),i);
				for(int p=0;p<temp.size();p++)
					root.updateSupportCount(temp.get(p));
			}
			ArrayList<TreeSet<Integer>> Kfreq = root.updateItemsets(minSupport,data.size());
			if(Kfreq.size()>0)
				itemsets.set(i-1,Kfreq);
			else
				break;
		}
		
		for(int i=0;i<itemsets.size();i++)
		{
			for(int j=0;j<itemsets.get(i).size();j++)
				System.out.println(itemsets.get(i).get(j));
		}
		long supportStopTime = System.currentTimeMillis();
		System.out.println("The Time elapsed to find all frequent Item Subsets: " + (supportStopTime-startTime)+" seconds");

		/** All Confidence related stuff to be done here*/

		long finalStopTime = System.currentTimeMillis();
		System.out.println("The Time elapsed for confidence pruning:  " + (finalStopTime-supportStopTime)+" seconds");
		System.out.println("The Total Time for generating all rules: " + (finalStopTime-startTime)+" seconds");
		in.close();
	}

	/**
	 * @param filename : the filename to be opened
	 * @param data : The ArrayList of data objects which is to be populated on the basis of data
	 * @throws IOException : To handle File not found error.
	 * @see IOException
	 */
	public static void inputHandle(String filename,ArrayList<int[]> data)throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line=null;
		while( (line=br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line,",");
			int row[] = new int[17];
			while(st.hasMoreTokens()){
				for(int i=0;i<17;i++)
					row[i] = getInt(st.nextToken());
			}
			data.add(expand(row));
		}       
		br.close();
	}

	/**
	 * @param dataField The string from the input. Can be a 'y','n','republican','democrat', or ?
	 * @return An integer corresponding to the the input. y : 1 \n n : 1\n republican: 1\n democrat: 0\n ?: -1 
	 */
	public static int getInt(String dataField){
		if(dataField.equals("y"))
			return 1;
		else if(dataField.equals("n"))
			return 0;
		else if(dataField.equals("?"))
			return -1;
		else if(dataField.equals("republican"))
			return 1;
		else 
			return 0;
	}

	/**
	 * @param row the row which we created after reading in the data
	 * @return a row with binarized categorical data. The attribute corresponding to the data can be found in DataRef Class
	 */
	public static int[] expand(int row[]){
		int ans[] = new int[34];
		int j = 0;
		for(int i=0;i<row.length;i++)
		{
			if(row[i]==1)
			{
				ans[j]   = 0;
				ans[j+1] = 1;
			}else if(row[i]==0){
				ans[j]   = 1;
				ans[j+1] = 0;
			}
			j = j+2;
		}
		return ans;
	}

	/**
	 * @param row the row which we want to print
	 * An utility function that prints the passed array
	 */
	public static void printArray(int []row){
		for(int i=0;i<row.length;i++)
			System.out.print(row[i]+" ");
		System.out.println();
	}
	public static void printArray(double []row){
		for(int i=0;i<row.length;i++)
			System.out.print(row[i]+" ");
		System.out.println();
	}

	/**
	 * @param data The data in the binary format
	 * @param dref The class which stores the text corresponding to the the index value
	 * @param minSupport The Support Threshold
	 * @return The ArrayList which contains all sets of 1-frequent items corresponding to the minimum Support Threshold
	 */
	public static ArrayList<TreeSet<Integer>> oneFrequentItemSet(ArrayList<int []>data,DataRef dref,double minSupport){
		ArrayList<TreeSet<Integer>> frequent = new ArrayList<TreeSet<Integer>>();
		int vectorLen = data.get(0).length;
		double supportValues[] = new double[vectorLen];
		for(int i = 0; i < data.size();i++)
		{
			for(int j = 0;j<vectorLen;j++)
			{
				if(data.get(i)[j]==1)
					supportValues[j]++;
			}
		}

		for(int i=0; i< supportValues.length;i++)
			supportValues[i] = supportValues[i]/data.size();

		for(int i=0;i< supportValues.length;i++)
		{
			if(supportValues[i]>=minSupport)
			{
				TreeSet<Integer> temp = new TreeSet<Integer>();
				temp.add(i);
				frequent.add(temp);
			}
		}
		return frequent;
	}

	/**
	 * @param data The data in the binary format
	 * @return The set representation of the voting patterns.
	 */
	public static ArrayList<ArrayList<Integer>> attributeRepresentation(ArrayList<int[]> data){
		ArrayList<ArrayList<Integer>> data_rept = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<data.size();i++)
		{
			ArrayList<Integer> temp = new ArrayList<Integer>();
			for(int j=0;j<data.get(0).length;j++)
			{
				if(data.get(i)[j]==1)					
					temp.add(j);
			}
			data_rept.add(temp);
		}
		return data_rept;
	}

	/**
	 * @param sets All K-1 dimension sets
	 * @param k Current cardinality of subsets
	 */
	public static void kminus1tok(RuleMining ref,ArrayList<TreeSet<Integer>> sets,int k){

		ArrayList<TreeSet<Integer>> temp = new ArrayList<TreeSet<Integer>>();
		TreeSet<Integer> candidate = new TreeSet<Integer>();
		TreeSet<Integer> toMerge = new TreeSet<Integer>();
		for(int i=0;i<sets.size();i++)
		{
			for(int j=i+1;j<sets.size();j++)
			{
				candidate = new TreeSet<Integer>();
				candidate.addAll(sets.get(i));
				int lastI = candidate.last();
				candidate.remove(lastI);
				toMerge.addAll(sets.get(j));
				int lastJ = toMerge.last();
				toMerge.remove(lastJ);
				candidate.addAll(toMerge);
				if(candidate.size()==toMerge.size())
				{
					candidate.add(lastI);
					candidate.add(lastJ);
					temp.add(candidate);
				}
			}		
		}
		//System.out.println("Temp Size : "+temp.size());
		itemsets.add(k-1,prepruning(ref,temp,k));
		//System.out.println("K-1: "+(k-1)+"   "+itemsets.get(k-1).size());
	}

	/**
	 * @param ref The object of class RuleMining
	 * @param kfrequent The item sets of length K which are yet to be pruned further
	 * @param k The length of item sets 
	 * @return The set of K frequent item sets after removing sets which contain elements in which both stances for a particular cause has been taken. Also if it's k-1 subsets are not frequent then removed.  
	 */
	public static ArrayList<TreeSet<Integer>> prepruning(RuleMining ref,ArrayList<TreeSet<Integer>> kfrequent,int k){
		ArrayList<TreeSet<Integer>> finalKfrequent = new ArrayList<TreeSet<Integer>>();
		ArrayList<TreeSet<Integer>> kminus1 = itemsets.get(k-2);
		for(int i=0;i<kfrequent.size();i++)
		{
			int tem[] = toInt(kfrequent.get(i));
			int flag = 1;
			for(int j=0;j<tem.length-1;j++)
			{
				if(tem[j]%2==0 && tem[j+1] == tem[j]+1)
				{
					flag = 0;
					break;
				}
			}

			if(flag==1)
			{
				ArrayList<Integer> al_kfruent = new ArrayList<Integer>();
				for (int index = 0; index < tem.length; index++)
					al_kfruent.add(tem[index]);

				ArrayList<ArrayList<Integer>> kminus1cand = ref.tranBreakdown(al_kfruent,k-1);
				//System.out.println("kminus1cand + "+kminus1cand.size());
				for(int p=0;p<kminus1cand.size();p++)
				{
					TreeSet<Integer> tempSet = new TreeSet<Integer>(kminus1cand.get(p));
					if(!kminus1.contains(tempSet))
					{
						flag = 0;
						break;
					}
				}
			}
			if(flag==1)
			{
				TreeSet<Integer> toBeConsidered = new TreeSet<Integer>();
				for(int s=0;s<tem.length;s++)
					toBeConsidered.add(tem[s]);
				finalKfrequent.add(toBeConsidered);
			}
		}
		return finalKfrequent;
	}

	/**
	 * @param set The Set of integers which we want to convert to an integer array representation for processing purposes
	 * @return an integer array representation of the Set of Integers
	 */
	public static int[] toInt(Set<Integer> set) {
		int[] a = new int[set.size()];
		int i = 0;
		for (Integer val : set) a[i++] = val;
		return a;
	}

	/**
	 * @param transaction
	 * @param k
	 * @return
	 * Poojitha iska documentation karegi :)
	 */
	public ArrayList<ArrayList<Integer>> tranBreakdown (ArrayList<Integer> transaction, int k){
		ArrayList<ArrayList<Integer>> sub = new ArrayList<ArrayList<Integer>>();
		int len = transaction.size();
		ArrayList<Integer> temp = new ArrayList<Integer>();
		this.breakdown(sub,transaction,temp,0,len-1,0,k);
		return sub;
	}

	/**
	 * @param sub
	 * @param trans
	 * @param temp
	 * @param low
	 * @param high
	 * @param point
	 * @param k
	 * iska bhi :D
	 */
	public void breakdown(ArrayList<ArrayList<Integer>> sub, ArrayList<Integer> trans, ArrayList<Integer> temp, int low, int high, int point, int k){
		if(point==k){
			ArrayList<Integer> copy = new ArrayList<Integer>();
			for(int i = 0;i<temp.size();i++){
				copy.add(temp.get(i));
			}
			sub.add(copy);
			return;
		}	
		for(int i=low; (i <= high) && (high-i+1 >= k-point); i++ ){
			temp.add(point,trans.get(i));
			this.breakdown(sub, trans,temp,i+1,high,point+1,k);
			temp.remove(point);
		}
	}
}		