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
									System.out.println(toCopy);
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
						double support = node.supportCount.get(i)/noOfTransactions;
						RuleMining.itemsets.get(15-hash).add(temp1);
						System.out.println("itemsets size: "+RuleMining.itemsets.get(15-hash).size()+" hash: "+this.hash);
						RuleMining.itemWithSupport.put(temp,support);
						status = true;
					}
				}
			}
		}
		
		/*if(node.candidate == null){
			for(int i=0;i<hash;i++){
				if(node.children.get(i)!=null){
					node.children.get(i).updateItemsets(minsup,noOfTransactions);
				}
			}
		}
		else{
			for(int i = 0;i<node.candidate.size();i++){
				//System.out.println(node.supportCount.get(i));
				if(node.supportCount.get(i)>minsup*noOfTransactions){
					TreeSet<Integer> temp = new TreeSet<Integer>(node.candidate.get(i));
					TreeSet<Integer> temp1 = new TreeSet<Integer>(node.candidate.get(i));
					double support = node.supportCount.get(i)/noOfTransactions;
					RuleMining.itemsets.get(15-hash).add(temp1);
					System.out.println("itemsets size: "+RuleMining.itemsets.get(15-hash).size()+" hash: "+this.hash);
					RuleMining.itemWithSupport.put(temp,support);
					status = true;
				}
			}
		}*/
		return status;
		//System.out.println("Hash value: "+this.hash+" Itemset size : "+itemsets.size());
	}
}