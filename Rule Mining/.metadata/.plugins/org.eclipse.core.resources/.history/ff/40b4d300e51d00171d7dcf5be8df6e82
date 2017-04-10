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
			System.out.println(itemset.last());
			HashTree node = this;
			TreeSet copy = new TreeSet<Integer>(itemset);
			while(itemset.size()>0){
				int h = itemset.first();
				itemset.remove(h);
				if(node.children.get(h%hash)==null){
					HashTree temp = new HashTree(this.hash, node.depth+1);
					node.children.add(h%hash,temp);
				}
				node = node.children.get(h%hash);
				System.out.println("Hash Value: "+h%hash+" Depth :"+node.depth);
				if(itemset.size()==0){
					System.out.println("Hello");
					node.candidate.add(copy);
					node.supportCount.add(0);
				}
				else{
					if(node.candidate!=null&&node.candidate.size()<3){
						System.out.println("Depth: "+node.depth+" Size: "+node.candidate.size());
						if(node.candidate.size()>0)
						System.out.println(node.candidate.get(0).last());
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
								node.children.add(hCopy%hash,new HashTree(hash, node.depth+1));
							node.children.get(hCopy%hash).candidate.add(node.candidate.get(i));
							System.out.println("Adding test: "+node.children.get(hCopy%hash).candidate.get(i).last());
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