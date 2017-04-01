
package mining.rule;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

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
		for(int i = 0;i<17;i++)
		{
			int count = 0;
			for(int j = 0;j<data.size();j++)
			{
				int temp[] = data.get(j);
				if(temp[i]==-1)
					count++;
			}
			System.out.println(i+" "+count);
		}
		handleMissingValues(data);
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
		int flag = 1;
		int lineNum = 0;
		while( (line=br.readLine()) != null) {
			flag = 1;
			lineNum++;
			StringTokenizer st = new StringTokenizer(line,",");
			int row[] = new int[17];
			while(st.hasMoreTokens()){
				for(int i=0;i<17;i++)
				{
					row[i] = getInt(st.nextToken());
				}
			}
			for(int i=0;i<row.length;i++)
			{
				if(row.length!=17)
					System.out.println(lineNum);
			}
			data.add(row);
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
	 * @param data The Array List of Integers in which we have stored data for each candidate
	 * This function replaces the '?' i.e. the missing values, with suitable values
	 */
	public static void handleMissingValues(ArrayList<int[]> data){
	}
}