package mining.rule;

import java.util.*;

public class HashTree {
	int depth;
	ArrayList<TreeSet<Integer>> candidate;
	ArrayList<Integer> supportCount;
	ArrayList<HashTree> children;
	int hash;
	
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
		
	public boolean hashItemset(TreeSet<Integer> itemset){
		if(itemset.size()!=hash){
			return false;
		}
		else{
			System.out.println();
			HashTree node = this;
			TreeSet copy = new TreeSet<Integer>(itemset);
			while(itemset.size()>0){
				int h = itemset.first();
				itemset.remove(h);
				if(children.get(h%hash)==null){
					HashTree temp = new HashTree(this.hash, node.depth+1);
					children.add(h%hash,temp);
				}
				node = children.get(h%hash);
				System.out.println("Hash Value: "+h%hash+" Depth :"+node.depth);
				if(itemset.size()==0){
					node.candidate.add(copy);
					node.supportCount.add(0);
				}
				else{
					if(candidate!=null&&candidate.size()<3){
						node.candidate.add(copy);
						node.supportCount.add(0);
					}
					else{
						TreeSet<Integer> toCopy;
						for(int i = 0;i<3;i++){
							toCopy = candidate.get(i);
							for(int j=0;j<node.depth;j++){
								toCopy.remove(toCopy.first());
							}
							int hCopy = toCopy.first();
							node.children.add(hCopy%hash,new HashTree(hash, node.depth+1));
							node.children.get(hCopy%hash).candidate.add(node.candidate.get(i));
						}
						candidate = null;
					}
				}
			}
			return true;
		}
	}
	
	public void printTree(){
		if(children.isEmpty()){
			for(int i=0;i<candidate.size();i++){
				TreeSet<Integer> t = candidate.get(i);
				for(int j=0;j<candidate.get(i).size();j++){
					System.out.print(t.first()+" ");
					t.remove(t.first());
				}
				System.out.println();
			}
		}
		else{
			
		}
	}
}