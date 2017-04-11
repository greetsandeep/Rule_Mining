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
		
	public boolean hashItemset(TreeSet<Integer> set){
		if(set.size()!=hash){
			return false;
		}
		else{
			
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
						TreeSet<Integer> toCopy = new TreeSet<Integer>();
						for(int i = 0;i<3;i++){
							toCopy.clear();
							toCopy.addAll(node.candidate.get(i));
							for(int j=0;j<node.depth;j++){
								toCopy.remove(toCopy.first());
							}
							int hCopy = toCopy.first();
							if(node.children.get(hCopy%hash)==null)
								node.children.set(hCopy%hash,new HashTree(hash, node.depth+1));
							node.children.get(hCopy%hash).candidate.add(node.candidate.get(i));
						}
						node.candidate = null;
					}
				}
			}
			return true;
		}
	}
	
	public void updateSupportCount(ArrayList<Integer> itemset){
		HashTree node = this;
		int flag = 0;
		TreeSet<Integer> setRep = new TreeSet<Integer>();
		TreeSet<Integer> copy = new TreeSet<Integer>();
		for(int i=0;i<itemset.size();i++){
			setRep.add(itemset.get(i));
		}
		for(int i=0;i<itemset.size();i++){
			if(node.candidate == null){
				int h = itemset.get(i)%hash;
				node = node.children.get(h);
				if(node==null){
					break;
				}
			}
			else{
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
}