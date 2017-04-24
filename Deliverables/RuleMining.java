import java.io.*;
import java.util.*;

/**
 * @author Sandeep,Snehal,Poojitha
 * AIM : To Use hash tree and come up with interesting association rules by using Apriori algorithm.
 */
public class RuleMining {
	/** An ArrayList of Integer Arrays which store the stands of a candidate in a binary format*/
	public static ArrayList<int[]> data = new ArrayList<int[]>();
	
	/** A Data Structure which stores all the frequent item sets. At the index 0 we have an Array List of all 1-frequent item sets */
	public static ArrayList<ArrayList<TreeSet<Integer>>> itemsets = new ArrayList<ArrayList<TreeSet<Integer>>>();
	
	/** Representing all the transactions in the form of Array List of Integers */
	public static ArrayList<ArrayList<Integer>> data_attr = new ArrayList<ArrayList<Integer>>();
	
	/** A HashMap which maps all item sets with the item set's Support value  */
	public static HashMap<TreeSet<Integer>, Double> itemWithSupport = new HashMap<TreeSet<Integer>,Double>();
	
	/** A HashMap which contains all the rules which have confidence higher than the threshold */
	public static HashMap<ArrayList<TreeSet<Integer>>, Double> confidentRules = new HashMap<ArrayList<TreeSet<Integer>>, Double>();
	
	public static void main(String args[]){
	
		try{
			inputHandle("dataFile.txt",data);
		}catch(Exception e){
			System.out.println("\nError In file Reading " + e);
		}

		Scanner in = new Scanner(System.in);
		System.out.println("\nEnter The Value for Minimum Support:\n");
		double minSupport = in.nextDouble();
		System.out.println("\nEnter The value for Minimum Confidence:\n");
		double minConfidence = in.nextDouble();

		long startTime = System.currentTimeMillis();

		DataRef dref = new DataRef(); /** An object of DataRef Class */

		/** Contains all the sets of one Frequent Items based on the given minimum Support Value*/
		ArrayList<TreeSet<Integer>> oneFreq = oneFrequentItemSet(data,dref,minSupport);
		itemsets.add(0,oneFreq);
		RuleMining ref = new RuleMining();
		
		data_attr = attributeRepresentation(data);
		for(int i=2;i<=16;i++)
		{   
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

			boolean ifUpdated = root.updateItemsets(minSupport,data.size());
			
			if(!ifUpdated)
				break;
		}
		
		/*for(Map.Entry<TreeSet<Integer>, Double> e: itemWithSupport.entrySet()){
			System.out.println(e.getKey()+" "+e.getValue());
		}*/
		
		long supportStopTime = System.currentTimeMillis();
		System.out.println("\nThe Time elapsed to find all frequent Item Subsets: " + (supportStopTime-startTime)+" milliseconds\n");


		confidentRuleGen(minConfidence);
				
		long finalStopTime = System.currentTimeMillis();
		System.out.println("The Time elapsed for confidence pruning:  " + (finalStopTime-supportStopTime)+" milliseconds");
		System.out.println("The Total Time for generating all rules: " + (finalStopTime-startTime)+" milliseconds");
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
	 * @param row The row which we created after reading in the data
	 * @return A row with binarized categorical data. The attribute corresponding to the data can be found in DataRef Class
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
		
		for(int i=0; i< supportValues.length;i++){
			supportValues[i] = supportValues[i]/data.size();
		}

		for(int i=0;i< supportValues.length;i++)
		{
			if(supportValues[i]>=minSupport)
			{
				TreeSet<Integer> temp = new TreeSet<Integer>();
				temp.add(i);
				frequent.add(temp);
				itemWithSupport.put(temp,supportValues[i]);
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
	 * This function takes in all the frequent k-1 subsets and generates the candidate k subsets. 
	 * These candidates are then prepuned and passed to the HashTree class so that a tree can be formed out of these.
	 */
	public static void kminus1tok(RuleMining ref,ArrayList<TreeSet<Integer>> sets,int k){
		ArrayList<TreeSet<Integer>> temp = new ArrayList<TreeSet<Integer>>();
		TreeSet<Integer> candidate = new TreeSet<Integer>();
		TreeSet<Integer> toMerge = new TreeSet<Integer>();
		for(int i=0;i<sets.size();i++)
		{
			for(int j=i+1;j<sets.size();j++)
			{
				candidate.clear();
				toMerge.clear();
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
					temp.add(new TreeSet<Integer>(candidate));
				}
			}		
		}

		itemsets.add(k-1,prepruning(ref,temp,k));

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
	 * @param k The size of the subset required.
	 * @return All the possible subsets of size k for a given transaction.
	 * This function creates a temporary arrayList for storing each subset and passes it onto another function breakdown()
	 * along with transaction and structure to store the subsets.
	 */
	public ArrayList<ArrayList<Integer>> tranBreakdown (ArrayList<Integer> transaction, int k){
		ArrayList<ArrayList<Integer>> sub = new ArrayList<ArrayList<Integer>>();
		int len = transaction.size();
		ArrayList<Integer> temp = new ArrayList<Integer>();
		this.breakdown(sub,transaction,temp,0,len-1,0,k);
		return sub;
	}

	/**
	 * @param sub Contains all the possible subsets of the transaction
	 * @param trans Transaction
	 * @param temp Temporary space to store a subset
	 * @param low Starting point 
	 * @param high Ending point
	 * @param point Current position
	 * @param k Size of subset
	 * This function is first called by transBreakdown() and then recursively calls itself till the size of the required
	 * subset is achieved.
	 * It then adds the subset to the larger structure which stores all the subsets.
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

	/**
	 * @param minConf The Confidence threshold below which any rule would be pruned out for not having sufficient confidence
	 * This function checks confidence of only those rules who have the element 'democrat' or 'republican' in them.
	 * It passes such rules to the isConfRule() method
	 */
	public static void confidentRuleGen(double minConf){
		for(int i=1;i<itemsets.size();i++)
		{
			for(int j=0;j<itemsets.get(i).size();j++)
			{
				HashMap<TreeSet<Integer>,TreeSet<Integer>> can = candidatesForConfidence(itemsets.get(i).get(j));
				for(Map.Entry<TreeSet<Integer>, TreeSet<Integer>> e : can.entrySet()){
					isConfRule(e,minConf);
				}
			}
		}
	}

	/**
	 * @param e The Rule in the set format whose confidence is to be tested
	 * @param minConf The Confidence threshold below which any rule would be pruned out for not having sufficient confidence
	 * This function checks the confidence of each rule and adds only those rules to the global variable : <b> confidentRules </b> who have confidence more than the minimum threshold
	 * This function also prints all these rules in sentence format by referencing the DataRef Class
	 */
	public static void isConfRule(Map.Entry<TreeSet<Integer>, TreeSet<Integer>> e, double minConf){
		TreeSet<Integer> lhs = new TreeSet<Integer>(e.getKey());
		TreeSet<Integer> rhs = new TreeSet<Integer>(e.getValue());
 		TreeSet<Integer> union = new TreeSet<Integer>();
 		union.addAll(lhs);
 		union.addAll(rhs);
		double conf = itemWithSupport.get(union)/itemWithSupport.get(lhs); 
		
		DataRef dref = new DataRef();
		
		ArrayList<TreeSet<Integer>> temp = new ArrayList<TreeSet<Integer>>();
		temp.add(lhs);
		temp.add(rhs);
		
		if(conf >= minConf){
			confidentRules.put(temp, conf);
			for(Integer i:temp.get(0)){
		    	System.out.print(dref.attrRef[i]+" ; ");
		    }
			System.out.print(" ---> ");
			for(Integer i:temp.get(1)){
		    	System.out.print(dref.attrRef[i]+" ");
		    }
			System.out.println("\t\t"+conf);
			System.out.println();
		}
	}
	
	/**
	 * @param originalSet The Set of which we have to find all the possible Sub sets
	 * @return All the possible sub sets of the given Set including null set and the set itself
	 */
	public static Set<Set<Integer>> powerSet(Set<Integer> originalSet) {
        Set<Set<Integer>> sets = new HashSet<Set<Integer>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<Integer>());
            return sets;
        }
        List<Integer> list = new ArrayList<Integer>(originalSet);
        Integer head = list.get(0);
        Set<Integer> rest = new HashSet<Integer>(list.subList(1, list.size()));
        
        for (Set<Integer> set : powerSet(rest)) {
            Set<Integer> newSet = new HashSet<Integer>();
            newSet.add(head);
            newSet.addAll(set);
    		sets.add(set);
    		sets.add(newSet);
        }

        return sets;
    }
	
	/**
	 * @param rule The set currently under consideration
	 * @return Returns the possible candidates for confidence pruning. Eliminates Null gives all and vice versa rules.
	 */
	public static HashMap<TreeSet<Integer>,TreeSet<Integer>> candidatesForConfidence(TreeSet<Integer> rule){
		HashMap<TreeSet<Integer>,TreeSet<Integer>> candidates = new HashMap<TreeSet<Integer>,TreeSet<Integer>>();
		Set<Set<Integer>> temp = powerSet(rule);

		for (Set<Integer> s : temp) {
		    if(!(s.size()==0 || s.size()==rule.size()))
		    {
		    	Set<Integer> tempSet = new HashSet<Integer>(rule);
		    	tempSet.removeAll(s);
		    	candidates.put(new TreeSet<Integer>(s),new TreeSet<Integer>(tempSet));
		    }
		}
		return candidates;
	}
}		

class HashTree {
	/** Indicates the depth of this HashTree Node. */
	int depth;
	
	/** A list that stores the hashed itemsets if this node acts as leaf. */
	ArrayList<TreeSet<Integer>> candidate;
	
	/** A list that stores the support count of the corresponding candidate itemsets stored in the ArrayList candidate */
	ArrayList<Integer> supportCount;
	
	/** A list that stores the object references to the children based on the hash value if this node acts as an internal node.  */
	ArrayList<HashTree> children;
	
	/** The value of the hash function  */
	int hash;
	
	/**
	 * @param hash The hash value to be used by the tree 
	 * @param depth Depth of the the node being created 
	 * Constructor assigns appropriate initial values to the class attributes with customized hash and depth values. 
	 */
	HashTree(int hash, int depth){
		this.hash = hash;
		this.depth = depth;
		if(depth!=0){
			candidate = new ArrayList<TreeSet<Integer>>();
			supportCount = new ArrayList<Integer>();
		}
		children = new ArrayList<HashTree>(hash);
		for(int i =0;i<hash;i++){
			children.add(null);
		}
	}
		
	/**
	 * @param set The itemset that is to be hashed into the Hash Tree
	 */
	public void hashItemset(TreeSet<Integer> set){
			HashTree node = this;
			TreeSet<Integer> copy = new TreeSet<Integer>(set);
			TreeSet<Integer> itemset = new TreeSet<Integer>(set);
			while(itemset.size()>0){
				int h = itemset.first();
				itemset.remove(h);
				if(node.children.get(h%hash)==null){
					HashTree temp = new HashTree(this.hash, node.depth+1);
					node.children.set(h%hash,temp);
				}
				node = node.children.get(h%hash);
				if(itemset.size()==0){
					node.candidate.add(copy);
					node.supportCount.add(0);
				}
				else{
					if(node.candidate!=null&&node.candidate.size()<3){
						node.candidate.add(copy);
						node.supportCount.add(0);
						break;
					}
					else{
						if(node.candidate == null && node.depth!=0){
							continue;
						}
						else{
							TreeSet<Integer> toCopy = new TreeSet<Integer>();
							for(int i = 0;i<3;i++){
								toCopy.clear();
								toCopy.addAll(node.candidate.get(i));
								if(toCopy.size()>0){
									for(int j=0;j<node.depth;j++){
										toCopy.remove(toCopy.first());
									}								
									int hCopy = toCopy.first();
									if(node.children.get(hCopy%hash)==null){
										node.children.set(hCopy%hash,new HashTree(hash, node.depth+1));
									}
									node.children.get(hCopy%hash).candidate.add(node.candidate.get(i));
									node.children.get(hCopy%hash).supportCount.add(node.supportCount.get(i));
								}
							}
							node.candidate = null;
						}
					}
				}
			}
			
		
	}
	
	/**
	 * @param itemset Itemset whose support count is to be increased, if it exists in the Hash Tree
	 */
	public void updateSupportCount(ArrayList<Integer> itemset){
		HashTree node = this;
		int flag = 0;
		TreeSet<Integer> setRep = new TreeSet<Integer>();
		TreeSet<Integer> copy = new TreeSet<Integer>();
		for(int i=0;i<itemset.size();i++){
			setRep.add(itemset.get(i));
		}
		for(int i=0;i<itemset.size();i++){
			int enter = 0;
			if(node.candidate == null){
				int h = itemset.get(i)%hash;
				node = node.children.get(h);
				if(node==null){
					break;
				}
				if(node.candidate!=null){
					enter = 1;
				}
			}
			if(enter==1){
				for(int j = 0;j<node.candidate.size();j++){
					if(node.candidate.get(j)!=null){
						copy.clear();
						copy.addAll(setRep);
						copy.addAll(node.candidate.get(j));
						if(copy.size()==setRep.size()){
							
							int sc = node.supportCount.get(j);
							sc++;
							node.supportCount.set(j,sc);
							flag=1;
							break;
						}
					}
				}
				if(flag==1){
					break;
				}
			}
		}
	}
	
	/**
	 * @param minsup Support threshold 
	 * @param noOfTransactions Total number of transactions
	 * @return boolean value to indicate whether there were any subsets above the support threshold. Returns true if the Set of itemsets were updated and false otherwise. 
	 */
	public boolean updateItemsets(double minsup, int noOfTransactions){
		boolean status = false;
		RuleMining.itemsets.get(15-hash).clear();
		ArrayList<HashTree> queue = new ArrayList<HashTree>();
		queue.add(this);
		HashTree node = this;
		while(!queue.isEmpty()){
			node = queue.get(0);
			queue.remove(0);
			for(int i=0;i<node.children.size();i++){
				if(node.children.get(i)!=null){
					queue.add(node.children.get(i));
				}
			}
			if(node.candidate!=null){
				for(int i=0;i<node.candidate.size();i++){
					if(node.supportCount.get(i)>minsup*noOfTransactions){
						TreeSet<Integer> temp = new TreeSet<Integer>(node.candidate.get(i));
						TreeSet<Integer> temp1 = new TreeSet<Integer>(node.candidate.get(i));
						double support = (double)node.supportCount.get(i)/noOfTransactions;
						RuleMining.itemsets.get(15-hash).add(temp1);
						//System.out.println("itemsets size: "+RuleMining.itemsets.get(15-hash).size()+" hash: "+this.hash);
						RuleMining.itemWithSupport.put(temp,support);
						status = true;
					}
				}
			}
		}
				
		return status;
	}
}

class DataRef {
	/** An array of Strings which store the corresponding preference of the candidate given the index number */
	public String attrRef[] = new String[34];
	/**
	 * The constructor of the class where we declare all the stands that a candidate has taken  
	 * This representation gives the stand taken by the candidate given the number in a set representation 
	 */
	DataRef() {
		attrRef[0]   = "handicapped-infants=n";
		attrRef[1]   = "handicapped-infants=y";
		attrRef[2]   = "water-project-cost-sharing=n";
		attrRef[3]   = "water-project-cost-sharing=y";
		attrRef[4]   = "adoption-of-the-budget-resolution=n";
		attrRef[5]   = "adoption-of-the-budget-resolution=y";
		attrRef[6]   = "physician-fee-freeze=n";
		attrRef[7]   = "physician-fee-freeze=y";
		attrRef[8]   = "el-salvador-aid=n";
		attrRef[9]   = "el-salvador-aid=y";
		attrRef[10]  = "religious-groups-in-schools=n";
		attrRef[11]  = "religious-groups-in-schools=y";
		attrRef[12]  = "anti-satellite-test-ban=n";
		attrRef[13]  = "anti-satellite-test-ban=y";
		attrRef[14]  = "aid-to-nicaraguan-contras=n";
		attrRef[15]  = "aid-to-nicaraguan-contras=y";
		attrRef[16]  = "mx-missile=n";
		attrRef[17]  = "mx-missile=y";
		attrRef[18]  = "immigration=n";
		attrRef[19]  = "immigration=y";
		attrRef[20]  = "synfuels-corporation-cutback=n";
		attrRef[21]  = "synfuels-corporation-cutback=y";
		attrRef[22]  = "education-spending=n";
		attrRef[23]  = "education-spending=y";
		attrRef[24]  = "superfund-right-to-sue=n";
		attrRef[25]  = "superfund-right-to-sue=y";
		attrRef[26]  = "crime=n";
		attrRef[27]  = "crime=y";
		attrRef[28]  = "duty-free-exports=n";
		attrRef[29]  = "duty-free-exports=y";
		attrRef[30]  = "export-administration-act-south-africa=n";
		attrRef[31]  = "export-administration-act-south-africa=y";
		attrRef[32]  = "democrat";
		attrRef[33]  = "republican";
	}
}