
package mining.rule;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.lang.model.type.ArrayType;

/**
 * @author Sandeep,Snehal,Poojitha
 *	AIM : To Use hash tree and come up with interesting association rules by using apriori algorithm.
 */
public class RuleMining {
	/** An ArrayList of Integer Arrays which store the stands of a candidate in a binary format*/
	public static ArrayList<int[]> data = new ArrayList<int[]>(); 
	
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
		
		
//		for(int i = 0;i<17;i++)
//		{
//			int count = 0;
//			for(int j = 0;j<data.size();j++)
//			{
//				int temp[] = data.get(j);
//				if(temp[i]==-1)
//					count++;
//			}
//			System.out.println(i+" "+count);
//		}
		
		DataRef dref = new DataRef();
		/** Contains all the one Frequent Items based on the given minimum Support Value*/
		ArrayList<Integer> oneFreq = oneFrequentItemSet(data,dref,minSupport);
		for(int i=0;i<oneFreq.size();i++)
			System.out.println(dref.attrRef[i]);
		
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
				{
					row[i] = getInt(st.nextToken());
				}
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
		//printArray(row);
		//printArray(ans);
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
	
	public static ArrayList<Integer> oneFrequentItemSet(ArrayList<int []>data,DataRef dref,double minSupport){
		ArrayList<Integer> frequent = new ArrayList<Integer>();
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
		
		/** To check how many values belong to one category*/
		for(int i=0; i<supportValues.length;i++)
			System.out.println(dref.attrRef[i]+" "+supportValues[i]);
		
		System.out.println("******");
		
		
		/** To See how many are missing*/
		for(int i=0;i<supportValues.length;i=i+2)
		{
			System.out.println(dref.attrRef[i].substring(0,dref.attrRef[i].length()-1)+(435-(supportValues[i]+supportValues[i+1])));
		}
		System.out.println("*'*'*'*'");
		for(int i=0; i< supportValues.length;i++)
			supportValues[i] = supportValues[i]/data.size();
		
		for(int i=0;i< supportValues.length;i++)
		{
			if(supportValues[i]>=minSupport)
				frequent.add(i);
		}
		return frequent;
	}
}
