
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

abstract class Node{
	NonLeafNode parent;
	boolean isLeaf;
	boolean isRoot;
	Node root;
	int nodeNumber;
	String parentNumber;
}
	
class Data{
	int key;
	int value;
	
	public Data(int _key, int _value) {
		this.key = _key;
		this.value = _value;
	}
}

class KeyPoint{
	int key;
	Node leftChild;
	
	public KeyPoint(int _key, Node _leftChild) {
		this.key = _key;
		this.leftChild = _leftChild;
	}
}

class LeafNode extends Node{
	Data[] arr; //<key, value>
	int number;	
	Node sibling;

	public LeafNode(int m) {
		this.number = 0;
		this.sibling = null;
		this.arr = new Data[m];
		for(int i = 0; i < m; i++) {
			this.arr[i] = null;
		}
		this.parent = null;
		this.isLeaf = true;
		this.isRoot = false;
		this.root = this;
	}
}

class NonLeafNode extends Node{
	KeyPoint[] arr;
	int number;
	Node right;

	public NonLeafNode(int m) {
		this.number = 0;
		this.arr = new KeyPoint[m];
		for(int i = 0; i < m; i++) {
			this.arr[i] = null;
		}
		this.right = null;
		this.isLeaf = false;
		this.isRoot = false;
		this.parent = null;
		this.root = this;
	}
}

public class BPlusTree {
	public static ArrayList<Node> nodeArr = new ArrayList<>();
	public static ArrayList<Node> arr = new ArrayList<>();
	static Node first;
	static int m;
	
	public void swap(LeafNode node, int low, int high) {
		Data temp1 = node.arr[low];
		node.arr[low] = node.arr[high];
		node.arr[high] = temp1;
	}
	
	public void swap(NonLeafNode node, int low, int high) {
		KeyPoint temp1 = node.arr[low];
		node.arr[low] = node.arr[high];
		node.arr[high] = temp1;
	}
	
	public int partition(LeafNode node, int low, int high) {
		int pivot = node.arr[(low+high)/2].key;
		while(low <= high) {
			while(pivot > node.arr[low].key) {
				low++;
			}
			while(pivot < node.arr[high].key) {
				high--;
			}
			if(low <= high) {
				swap(node, low, high);
				low++;
				high--;
			}
		}
		return low;
	}
	
	public int partition(NonLeafNode node, int low, int high) {
		int pivot = node.arr[(low+high)/2].key;
		while(low <= high) {
			while(pivot > node.arr[low].key) {
				low++;
			}
			while(pivot < node.arr[high].key) {
				high--;
			}
			if(low <= high) {
				swap(node, low, high);
				low++;
				high--;
			}
		}
		return low;
	}

	public void sort(LeafNode node, int low, int high) {
		if(low >= high)
			return;
		int mid = partition(node, low, high);
		sort(node, low, mid-1);
		sort(node, mid, high);
	}
	
	public void sort(NonLeafNode node, int low, int high) {
		if(low >= high)
			return;
	
		int mid = partition(node, low, high);
		sort(node, low, mid-1);
		sort(node, mid, high);
	}

	public void quickSort(Node node) {
		if(node.isLeaf) {
			LeafNode temp = (LeafNode)node;
			sort(temp, 0, temp.number-1);
		}
		else {
			NonLeafNode temp = (NonLeafNode)node;
			sort(temp, 0, temp.number-1);
		}
	}
	
	public Node splitNode(Node node) {
		int mid = m / 2;
		
		if(node.isLeaf == true) {
			LeafNode temp = (LeafNode)node;
			if(temp.parent == null) { //부모가 없을떄
				LeafNode right = new LeafNode(m);
				for(int i = mid; i < m; i++) {
					right.arr[i-mid] = temp.arr[i];
					right.number++;
				}
							//여기까지 확인완료!!
				NonLeafNode parent = new NonLeafNode(m);
				parent.arr[0] = new KeyPoint(temp.arr[mid].key, temp);
				parent.number++;
				for(int i = mid; i < m; i++) {
					temp.arr[i] = null;
					temp.number--;
				}
				temp.isRoot = false;
				parent.parent = null;
				parent.isRoot = true;
				parent.right = right;
				parent.arr[0].leftChild = temp;
				temp.parent = parent;
				right.parent = parent;
				temp.sibling = right;
				right.sibling = null;
				temp.isLeaf = true;
				right.isLeaf = true;
				temp.isRoot = false;
				right.isRoot = false;
				first = temp;
				parent.root = parent;
				temp.root = parent.root;
				right.root = parent.root;
				return parent;
			}
			else { //이미 부모가 있을때 temp = left;
				LeafNode right = new LeafNode(m);
				for(int i = mid; i < m; i++) {
					right.arr[i-mid] = temp.arr[i];
					right.number++;
				}
				int _key = temp.arr[mid].key;
				for(int i = mid; i < m; i++) {
					temp.arr[i] = null;
					temp.number--;
				}
				KeyPoint parent = new KeyPoint(_key, temp);
				temp.parent.number++;
				temp.parent.arr[temp.parent.number-1] = parent;

				quickSort(temp.parent);
				int i;
				for(i = 0; i < temp.parent.number; i++) {
					if(temp.parent.arr[i].key == parent.key)
						break;
				}
				
				if(i == temp.parent.number-1) {
					//case 1 right most에 올라갈때
					right.sibling = temp.sibling;
					temp.parent.right = right;
					temp.sibling = right;
				}
				else {
					temp.parent.arr[i+1].leftChild = right;
					right.sibling = temp.sibling;
					temp.sibling = right;
				}
				temp.root = temp.parent.root;
				right.root = temp.parent.root;
				temp.isLeaf = true;
				right.isLeaf = true;
				temp.isRoot = false;
				right.isRoot = false;
				right.parent = temp.parent;
			}
			return temp.parent;
		}
		else { //leaf노드가 아닐
			NonLeafNode temp = (NonLeafNode)node;
			if(temp.parent == null) { //부모가 없을때
				NonLeafNode right = new NonLeafNode(m);
				for(int i = mid+1; i < m; i++) {
					right.arr[i-mid-1] = temp.arr[i];
					right.number++;
				}
				right.right = temp.right;
				temp.right.parent = right; 
				temp.right = temp.arr[mid].leftChild;
				
				NonLeafNode parent = new NonLeafNode(m);
				
				//여기에 나눴을때 그걸 부모로 가지고있던 leafNode처리하기
				for(int i = 0; i < right.number; i++) {
					right.arr[i].leftChild.parent = right;
				}
				parent.arr[0] = new KeyPoint(temp.arr[mid].key, temp);
				parent.right = right;
				parent.number++;
				for(int i = mid; i < m; i++) {
					temp.arr[i] = null;
					temp.number--;
				}
				parent.root = parent;
				temp.root = parent.root;
				right.root = parent.root;
				for(int i = 0; i < temp.number; i++) {
					temp.arr[i].leftChild.root = temp.root;
				}
				temp.right.root = temp.root;
				for(int i = 0; i < right.number; i++) {
					right.arr[i].leftChild.root = right.root;
				}
				right.right.root = right.root;
				parent.parent = null;
				parent.isRoot = true;
				temp.isRoot = false;
				right.isRoot = false;
				temp.parent = parent;
				right.parent = parent;
				return parent;
			}
			else { //부모가있을때
				NonLeafNode right = new NonLeafNode(m);
				for(int i = mid+1; i < m; i++) {
					right.arr[i-mid-1] = temp.arr[i];
					right.number++;
				}
				right.right = temp.right;
	
				KeyPoint parent = new KeyPoint(temp.arr[mid].key, temp);
				
				for(int i = mid+1; i < m; i++) {
					temp.arr[i].leftChild.parent = right;
				}
				temp.right.parent = right;
				right.parent = temp.parent;
				temp.right = temp.arr[mid].leftChild;
				temp.right.parent = temp;
				for(int i = mid; i < m; i++) {
					temp.arr[i] = null;
					temp.number--;
				}
				temp.parent.number++;
				temp.parent.arr[temp.parent.number-1] = parent;
				quickSort(temp.parent);
				int i;
				for(i = 0; i < temp.parent.number; i++) {
					if(parent.key == temp.parent.arr[i].key) {
						break;
					}
				}
				if(i == temp.parent.number-1) {
					//case 1 right most에 올라갈때
					temp.parent.right = right;
				}
				else {
					//case 2,3 중앙, 왼쪽끝 올라갈떄
					temp.parent.arr[i+1].leftChild = right;
				}
				temp.root = temp.parent.root;
				right.root = temp.parent.root;
				for(int j = 0; j < temp.number; j++) {
					temp.arr[j].leftChild.root = temp.root;
				}
				temp.right.root = temp.root;
				for(int j = 0; j < right.number; j++) {
					right.arr[j].leftChild.root = right.root;
				}
				right.right.root = right.root;
				temp.isRoot = false;
				right.isRoot = false;
				return temp.parent;
			}
		}
	}
	
	//root부터 leaf까지 내려올때 알맞은 자리 제공
	public Node findLocation(Node node, int key) {
		if(node == null) {
			return node;
		}
		if(node.isLeaf) {
			return node;
		}
		NonLeafNode temp = (NonLeafNode)node;
		if(temp.number == 0 || key >= temp.arr[temp.number-1].key) {
			return temp.right;
		}
		Node target = temp.right;
		for(int i = 0; i < temp.number; i++) {
			if(key < temp.arr[i].key) {
				target = temp.arr[i].leftChild;
				break;
			}
		}
		return target;
	}
	
	public boolean findKey(Node root, int key) {
		Node target = root;
		while(!target.isLeaf) {
			NonLeafNode temp = (NonLeafNode)target;
			target = findLocation(temp, key);
		}
		if(find((LeafNode)target, key)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public Node insert(Node node, Data data) {
		if(node.isLeaf == false) {
			Node target = findLocation(node, data.key);
			node = insert(target, data);
			
			NonLeafNode temp = (NonLeafNode)node;
			if(temp.number >= m) {
				return splitNode(node);
			}
			if(node.parent != null) {
				return node.parent;
			}
			else {
				return node;
			}
		}
		else {
			LeafNode temp = (LeafNode)node;
			if(find(temp, data.key)) {
				System.out.println("The key exists already!!");
				if(temp.parent != null) {
					return temp.parent;
				}
				else {
					return temp;
				}
			}
			temp.number++;
			temp.arr[temp.number-1] = data;
			quickSort(temp);
			if(temp.number >= m) {
				return splitNode(node);
			}
			else {
				if(temp.parent != null) {
					return temp.parent;
				}
				else {
					return temp;
				}
			}
		}
	}
	
	
	boolean find(LeafNode node, int key) { //leaf노드의 각 array에 해당 key가 있나 확인
		for(int i = 0; i < node.number; i++) {
			if(node.arr[i].key == key) {
				return true;
			}
		}
		return false;
	}
	
	boolean find(NonLeafNode node, int key) {
		if(node.number == 0) {
			return false;
		}
		
		for(int i = 0; i < node.number; i++) {
			if(node.arr[i].key == key) {
				return true;
			}
		}
		return false;
	}
	
	
	public void singleKeySearch(Node root, int key) {
		if(root == null) {
			System.out.println("SingKeySearch Error : root is NULL");
			return ;
		}
		Node target = root;
		while(target.isLeaf != true) {
			NonLeafNode temp = (NonLeafNode)target;
			for(int i = 0; i < temp.number; i++) {
				if(i == temp.number-1) {
					System.out.print(temp.arr[i].key);
				}
				else {
					System.out.print(temp.arr[i].key + ",");
				}
			}
			System.out.println();
			target = findLocation(target, key);
		}
		LeafNode temp = (LeafNode)target;
		for(int i = 0; i < temp.number; i++) {
			if(temp.arr[i].key == key) {
				System.out.println(temp.arr[i].value);
				return;
			}
		}
		System.out.println("NOT FOUND");
	}
	
	public void rangedSearch(Node root, int start, int end) {
		if(root == null) {
			System.out.println("RangedSearch Error : root is NULL");
			return;
		}
		LeafNode temp = (LeafNode)first;
		while(true) {
			if(temp == null) { 
				System.out.println("Error : there's no key in Bound");
				return;
			}
			if(temp.arr[temp.number-1].key < start) {
				Node node = temp.sibling;
				temp = (LeafNode)node;
			}
			else {
				break;
			}
		}
		while(temp.arr[0].key <= end) {
			for(int i = 0; i < temp.number; i++) {
				if(temp.arr[i].key >= start && temp.arr[i].key <= end) {
					System.out.println(temp.arr[i].key + "," + temp.arr[i].value);
				}
			}
			Node node = temp.sibling;
			if(node == null) {
				break;
			}
			temp = (LeafNode)node;
		}
		
	}
	
	
	
	
	public Node delete(Node root, int key) {
		if(root == null) {
			return null;
		}
		//m = 3,4 case일떄 더 신중히 생각해보자
		int underBound; //key의 개수 Not Child
		if(m % 2 == 1) {
			underBound = m/2;
		}
		else {//m이짝수
			underBound = m/2-1;
		}
		if(root.isLeaf == false){ // == parent == null
			NonLeafNode r = (NonLeafNode)root;
			Node target = findLocation(root, key);
			root = delete(target, key);
				//NonLeafNode Delete
			if(root.isLeaf) {
					root.isRoot = true;
					root.parent = null;
					root.root = root;
					return root;
			}
			else { 
				if(root.parent == null) {
					NonLeafNode n = (NonLeafNode)root;
					if(n.number == 0) {
						n.right.root = n.right;
						n.right.isRoot = true;
						n.right.parent = null;
						return n.right;
					}
					else {
						return root;
					}
				}
				else { //부모가 있다. NonLeafNode root가 아니다. 이말은 parent가있따.
				NonLeafNode temp = (NonLeafNode)root;
				if(temp.number < underBound) {
						int i; // i = temp.parent에서 key의 위치
						for(i = 0; i < temp.parent.number; i++) {
							if(temp.parent.arr[i].leftChild == temp) {
								break;
							}
						}
						if(i == 0) {
							NonLeafNode sibling;
							if(temp.parent.number == 1) {
								sibling = (NonLeafNode)temp.parent.right;
							}
							else {
								sibling = (NonLeafNode)temp.parent.arr[i+1].leftChild;
							}
							
							if(sibling.number-1 >= underBound) { //확인++++++++++++++++++++++
								temp.number++;
								temp.arr[temp.number-1] = new KeyPoint(temp.parent.arr[0].key, temp.right);
								temp.arr[temp.number-1].leftChild.parent = temp;
								temp.right = sibling.arr[0].leftChild;
								temp.right.parent = temp;
								temp.parent.arr[0].key = sibling.arr[0].key;
								int l = sibling.number;
								for(int j = 0; j < l-1; j++) {
									sibling.arr[j] = sibling.arr[j+1];
								}
								sibling.number--;
								sibling.arr[sibling.number] = null;
								return temp.parent;
							}
							else {
								// right sibling이랑 병합 -> 여기까지한듯;; 2019.9.22.밤12시
								if(temp.parent.number == 1) {
									temp.arr[temp.number] = new KeyPoint(temp.parent.arr[i].key, temp.right);
									temp.number++;
									temp.arr[temp.number-1].leftChild.parent = temp;
									int l = temp.number;
									int a = sibling.number;
									for(int j = 0; j < a; j++) {
										temp.arr[l+j] = sibling.arr[j];
										temp.arr[l+j].leftChild.parent = temp;
										temp.number++;
									}
									temp.right = sibling.right;
									temp.right.parent = temp;
									temp.parent.number--;
									temp.parent.arr[temp.parent.number] = null;
									sibling = null;

									if(temp.parent.parent == null) {
										temp.isRoot = true;
										temp.parent = null;
										temp.root = temp;
										return temp;
									}
									else {
										temp.parent.right = temp;
										return temp.parent;
									}
								}
								else {
									int b = sibling.number;
									for(int j = b; j > 0; j--) {
										sibling.arr[j] = sibling.arr[j-1];
									}
									sibling.number++;
									sibling.arr[0] = new KeyPoint(sibling.parent.arr[i].key, temp.right);
									sibling.arr[0].leftChild.parent = sibling;
									int l = temp.number;
									int a = sibling.number;
									for(int j = a; j > 0; j--) {
										sibling.arr[j + l - 1] = sibling.arr[j - 1];
									}
									for(int j = 0; j < l; j++) {
										sibling.arr[j] = temp.arr[j];
										sibling.arr[j].leftChild.parent = sibling;
										sibling.number++;
									}
									for(int j = 0; j < sibling.parent.number-1; j++) {
										sibling.parent.arr[j] = sibling.parent.arr[j+1];
									}
									sibling.parent.number--;
									sibling.parent.arr[sibling.parent.number] = null;
									temp = null;
									return sibling.parent;
								}
							}
						}
						else if(i == temp.parent.number) { 
							NonLeafNode sibling = (NonLeafNode)temp.parent.arr[temp.parent.number-1].leftChild;
							if(sibling.number-1 >= underBound) { //잘댐 ㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎ
								//left sibling 재배치47
								int a = temp.number;
								for(int j = a; j > 0; j--) {
									temp.arr[j] = temp.arr[j-1];
								}
								temp.number++;
								temp.arr[0] = new KeyPoint(temp.parent.arr[i-1].key, sibling.right);
								temp.arr[0].leftChild.parent = temp;
								temp.parent.arr[i-1].key = sibling.arr[sibling.number-1].key;
								sibling.right = sibling.arr[sibling.number-1].leftChild;
								sibling.number--;
								sibling.arr[sibling.number] = null;
								return sibling.parent;
							}
							else {
								//left sibling 병합 오케이요
								if(temp.parent.number == 1) {
									sibling.arr[sibling.number] = new KeyPoint(sibling.parent.arr[0].key, sibling.right);
									sibling.number++;
									sibling.arr[sibling.number-1].leftChild.parent = sibling;
									int l = sibling.number;
									int a = temp.number;
									for(int j = 0; j < a; j++) {
										sibling.arr[l+j] = temp.arr[j];
										sibling.arr[l+j].leftChild.parent = sibling;
										sibling.number++;
									}
									sibling.right = temp.right;
									sibling.right.parent = sibling;
									temp = null;
									sibling.parent.number--;
									sibling.parent.arr[sibling.parent.number] = null;
									if(sibling.parent.parent == null) {
										sibling.isRoot = true;
										sibling.parent = null;
										sibling.root = sibling;
										return sibling;
									}
									else {
										sibling.parent.right = sibling;
										return sibling.parent;
									}
								}
								else {
									int b = temp.number;
									for(int j = b; j > 0; j--) {
										temp.arr[j] = temp.arr[j-1];
									}
									temp.number++;
									temp.arr[0] = new KeyPoint(temp.parent.arr[i-1].key, sibling.right);
									temp.arr[0].leftChild.parent = temp;
									int l = sibling.number;
									int a = temp.number;
									for(int j = a; j > 0; j--) {
										temp.arr[l+j-1] = temp.arr[j-1];
									}
									for(int j = 0; j < l; j++) {
										temp.arr[j] = sibling.arr[j];
										temp.arr[j].leftChild.parent = temp;
										temp.number++;
									}
									sibling = null;
									temp.parent.number--;
									temp.parent.arr[temp.parent.number] = null;
									return temp.parent;
								}
							}
						}
						else if(i == temp.parent.number-1) { 
							NonLeafNode sibling = (NonLeafNode)temp.parent.arr[i-1].leftChild;
							if(sibling.number-1 >= underBound) { // 확인해써용!!!!ㅎㅎㅎㅎㅎㅎㅎㅎㅎㅎ
								//left sibling 이랑 재배치
								int b = temp.number;
								for(int j = b; j > 0; j--) { 
									temp.arr[j] = temp.arr[j-1];
								}
								temp.number++;
								temp.arr[0] = new KeyPoint(temp.parent.arr[i-1].key, sibling.right);
								temp.arr[0].leftChild.parent = temp;
								temp.parent.arr[i-1].key = sibling.arr[sibling.number-1].key;
								
								sibling.right = sibling.arr[sibling.number-1].leftChild;
								sibling.number--;
								sibling.arr[sibling.number] = null;
								return temp.parent;
							}
							else { //확인해야댐
								sibling = (NonLeafNode)temp.parent.right;
								if(sibling.number-1 >= underBound) { // 오예에!!!!!!++++++++++++++++++++++
									//right sibling이랑 재배치
									temp.number++;
									temp.arr[temp.number-1] = new KeyPoint(temp.parent.arr[i].key, temp.right);
									temp.arr[temp.number-1].leftChild.parent = temp;
									temp.right = sibling.arr[0].leftChild;
									temp.right.parent = temp;
									temp.parent.arr[i].key = sibling.arr[0].key;
									sibling.arr[0] = null;
									for(int j = 0; j < sibling.number-1; j++) {
										sibling.arr[j] = sibling.arr[j+1];
									}
									sibling.number--;
									sibling.arr[sibling.number] = null;
									return temp.parent;
								}
								else { // case찾아서 확인해야댐 했당꺠롱 ㅎㅎㅎㅎ
									if(temp.parent.number == 1) { //i == 0; //right sibling이랑 병합
										sibling = (NonLeafNode)temp.parent.right;
										temp.arr[temp.number] = new KeyPoint(temp.parent.arr[i].key, temp.right);
										temp.arr[temp.number].leftChild.parent = temp;
										temp.number++;
										int l = temp.number;
										int a = sibling.number;
										for(int j = 0; j < a; j++) {
											temp.arr[l+j] = sibling.arr[j];
											temp.arr[l+j].leftChild.parent = temp;
											temp.number++;
										}
										temp.right = sibling.right;
										temp.right.parent = temp;
										temp.parent.number--;
										sibling = null;
										if(temp.parent.parent == null) {
											temp.isRoot = true;
											temp.parent = null;
											temp.root = temp;
											return temp;
										}
										temp.parent.right = temp;
										return temp.parent;
									}
									else { //temp.parent.number != 1;
										//left sibling이랑  병합
										sibling = (NonLeafNode)temp.parent.arr[i-1].leftChild;
										for(int j = temp.number; j > 0; j--) {
											temp.arr[j] = temp.arr[j-1];
										}
										temp.arr[0] = new KeyPoint(temp.parent.arr[i-1].key, sibling.right);
										temp.arr[0].leftChild.parent = temp;
										temp.number++;
										int l = sibling.number;
										int a = temp.number;
										for(int j = a; j > 0; j--) {
											temp.arr[l+j-1] =  temp.arr[j-1];
										}
										for(int j = 0; j < l; j++) {
											temp.arr[j] = sibling.arr[j];
											temp.arr[j].leftChild.parent = temp;
											temp.number++;
										}
										
										sibling = null;
										temp.parent.number--;
										temp.parent.arr[temp.parent.number-1] = temp.parent.arr[temp.parent.number];
										temp.parent.arr[temp.parent.number] = null;
										return temp.parent;
									}
								}
							}
						}
						else { //i != 0 , i가 temp.parent.number-1, temp.parent.number아닐떄
							NonLeafNode sibling = (NonLeafNode)temp.parent.arr[i-1].leftChild;
							if(sibling.number-1 >= underBound) { // OK!!!!!!!!!!{+++++++++++++++
								//left sibling이랑 재배치
								int l = temp.number;
								for(int j = l; j > 0; j--) {
									temp.arr[j] = temp.arr[j-1];
								}
								temp.number++;
								temp.arr[0] = new KeyPoint(temp.parent.arr[i-1].key, sibling.right);
								temp.arr[0].leftChild.parent = temp;
								temp.parent.arr[i-1].key = sibling.arr[sibling.number-1].key;
								sibling.right = sibling.arr[sibling.number-1].leftChild;
								sibling.number--;
								sibling.arr[sibling.number] = null;
								return temp.parent;
							}
							else {
								sibling = (NonLeafNode)temp.parent.arr[i+1].leftChild;
								if(sibling.number-1 >= underBound) { // case찾아서 확인해야댐 -> 됐당 ㅎㅎㅎㅎㅎㅎ헤헤
									//right sibling이랑 재배치
									temp.number++;
									temp.arr[temp.number-1] = new KeyPoint(temp.parent.arr[i].key, temp.right);
									temp.arr[temp.number-1].leftChild.parent = temp;
									temp.right = sibling.arr[0].leftChild;
									temp.right.parent = temp;
									
									temp.parent.arr[i].key = sibling.arr[0].key;
									sibling.arr[0] = null;
									for(int j = 0; j < sibling.number-1; j++) {
										sibling.arr[j] = sibling.arr[j+1];
									}
									sibling.number--;
									sibling.arr[sibling.number] = null;
									return temp.parent;
								}
								else { // case찾아서 확인해야댐
									sibling = (NonLeafNode)temp.parent.arr[i-1].leftChild;
									//left sibling 이랑 병합
									for(int j = temp.number; j > 0; j--) {
										temp.arr[j] = temp.arr[j-1];
									}
									temp.number++;
									temp.arr[0] = new KeyPoint(temp.parent.arr[i-1].key, sibling.right);
									temp.arr[0].leftChild.parent = temp;
									int l = sibling.number;
									int a = temp.number;
									for(int j = a; j > 0; j--) {
										temp.arr[l+j-1] = temp.arr[j-1];
									}
									for(int j = 0; j < l; j++) {
										temp.arr[j] = sibling.arr[j];
										temp.arr[j].leftChild.parent = temp;
										temp.number++;
									}
									for(int j = i-1; j < temp.parent.number-1; j++) {
										temp.parent.arr[j] = temp.parent.arr[j+1];
									}
									temp.parent.number--;
									temp.parent.arr[temp.parent.number] = null;
									sibling = null;
									return temp.parent;
								}
							}
							
						}
						
				}
		
				else { //temp.number >= underBound
						return temp.parent;
				}}
			}
		}
		
		
		//LeafNodeDelete
		else {
			LeafNode temp = (LeafNode)root;
			if(find(temp, key)) {
				if(temp.parent == null) {
					if(find(temp, key)) {
						int i;
						for(i = 0; i < temp.number; i++) {
							if(temp.arr[i].key == key) {
								break;
							}
						}
						temp.number--;
						if(temp.number == 0) {
							return null;
						}
						for(int j = i; j < temp.number; j++) {
							temp.arr[j] = temp.arr[j+1];
						}
						temp.arr[temp.number] = null;
						return temp;
						
					}
					else {
						System.out.println("Deletion Error : NotFound");
						return temp;
					}
				}
				else { //부모가있다.
					if(temp.number-1 < underBound) {
						int i; //temp.parent의 해당하는 key위치 찾기
						for(i = 0; i < temp.parent.number; i++) {
							if(temp.parent.arr[i].leftChild == temp) {
								break;
							}
						}
						if(i == 0) { //첫번째 child에서 발생
							//확인
							LeafNode sibling;
							if(temp.parent.number == 1) {
								sibling = (LeafNode)temp.parent.right;
							}
							else {
								sibling = (LeafNode)temp.parent.arr[i+1].leftChild;
							}
							if(sibling.number-1 >= underBound) { //오른쪽 sibling
							
								//재배치
								int k; //Leaf에서 key locate
								for(k = 0; k < temp.number; k++) {
									if(temp.arr[k].key == key) {
										break;
									}
								}
								for(int j = k; j < temp.number-1; j++) {
									temp.arr[j] = temp.arr[j+1];
								}
								Data data = sibling.arr[0];
								for(int j = 1; j < sibling.number; j++) {
									sibling.arr[j-1] = sibling.arr[j];
								}
								sibling.number--;
								sibling.arr[sibling.number] = null;
								temp.arr[temp.number-1] = data;
								temp.parent.arr[i].key = sibling.arr[0].key;
								
								Node forRoot = temp;
								while(!forRoot.isRoot) {
									forRoot = forRoot.parent;
								}
								root.root = forRoot;
								if(forRoot.isLeaf) {
									return forRoot;
								}
								NonLeafNode change = (NonLeafNode)forRoot; //=>이거처럼 다고쳐야댐
								
								while(!find(change, key)) {
									Node t = findLocation(change, key);
									if(t.isLeaf) {
										return temp.parent;
									}
									change = (NonLeafNode)t;
								}
								int h;
								for(h = 0; h < change.number; h++) {
									if(change.arr[h].key == key) {
										break;
									}
								}
								int _key;
								if(h == change.number-1) {
									Node t = change.right;
									while(!t.isLeaf) {
										NonLeafNode target1 = (NonLeafNode)t;
										if(target1.number == 0) {
											t = target1.right;
										}
										else {
											t = target1.arr[0].leftChild;
										}
									}
									LeafNode min = (LeafNode)t;
									_key = min.arr[0].key;
								}
								else {
									Node t = change.arr[h+1].leftChild;
									while(!t.isLeaf) {
										NonLeafNode target1 = (NonLeafNode)t;
										if(target1.number == 0) {
											t = target1.right;
										}
										else {
											t = target1.arr[0].leftChild;
										}
									}
									LeafNode min = (LeafNode)t;
									_key = min.arr[0].key;
								} 
								change.arr[h].key = _key;
								
								return temp.parent; 
							}
							else {
								//병합
								//확인
								if(temp.parent.number == 1) {
									int k;
									for(k = 0; k < temp.number; k++) {
										if(temp.arr[k].key == key) {
											break;
										}
									}
									for(int j = k; j < temp.number-1; j++) {
										temp.arr[j] = temp.arr[j+1];
									}
									temp.number--;
									int l = temp.number;
									int a = sibling.number;
									for(int j = 0; j < a; j++) {
										temp.arr[l+j] = sibling.arr[j];
										temp.number++;
									}
									temp.sibling = sibling.sibling;
									temp.parent.number--;
									sibling = null;
									if(temp.parent.parent == null) {
										temp.isRoot = true;
										temp.parent = null;
										temp.root = temp;
										return temp;
									}
									temp.parent.right = temp;
									Node forRoot = temp;
									while(!forRoot.isRoot) {
										forRoot = forRoot.parent;
									}
									root.root = forRoot;
									if(forRoot.isLeaf) {
										return forRoot;
									}
									NonLeafNode change = (NonLeafNode)forRoot; //=>이거처럼 다고쳐야댐
									
									while(!find(change, key)) {
										Node t = findLocation(change, key);
										if(t.isLeaf) {
											if(t.parent.isRoot) {
												t.isRoot = true;
												t.root = temp;
												t.parent = null;
												return t;
											}
											else {
												temp.parent.right = temp;
												return temp.parent;
											}
										}
										change = (NonLeafNode)t;
									}
									int h;
									for(h = 0; h < change.number; h++) {
										if(change.arr[h].key == key) {
											break;
										}
									}
									int _key;
									if(h == change.number-1) {
										Node t = change.right;
										while(!t.isLeaf) {
											NonLeafNode target1 = (NonLeafNode)t;
											if(target1.number == 0) {
												t = target1.right;
											}
											else {
												t = target1.arr[0].leftChild;
											}
										}
										LeafNode min = (LeafNode)t;
										_key = min.arr[0].key;
									}
									else {
										Node t = change.arr[h+1].leftChild;
										while(!t.isLeaf) {
											NonLeafNode target1 = (NonLeafNode)t;
											if(target1.number == 0) {
												t = target1.right;
											}
											else {
												t = target1.arr[0].leftChild;
											}
										}
										LeafNode min = (LeafNode)t;
										_key = min.arr[0].key;
									} 
									change.arr[h].key = _key;
									return temp.parent;
								}
								else {
									int k; //temp에 들어있는 key의 위치 == k 찾기
									for(k = 0;k < temp.number; k++) {
										if(temp.arr[k].key == key) {
											break;
										}
									}
									
									for(int j = k; j < temp.number-1; j++) {
										temp.arr[j] = temp.arr[j+1];
									}
									temp.number--;
									int l = temp.number;
									int a = sibling.number;
									for(int j = 0; j < a; j++) {
										temp.arr[j+l] = sibling.arr[j];
										temp.number++;
									}
									temp.sibling = sibling.sibling;
									sibling = null;
									temp.parent.arr[1].leftChild = temp;
									for(int j = 0; j < temp.parent.number-1; j++) {
										temp.parent.arr[j] = temp.parent.arr[j+1];
									}
									temp.parent.number--;
									temp.parent.arr[temp.parent.number] = null;
									Node forRoot = temp;
									while(!forRoot.isRoot) {
										forRoot = forRoot.parent;
									}
									root.root = forRoot;
									if(forRoot.isLeaf) {
										return forRoot;
									}
									NonLeafNode change = (NonLeafNode)forRoot;
									
									while(!find(change, key)) {
										Node t = findLocation(change, key);
										if(t.isLeaf) {
											return temp.parent;
										}
										change = (NonLeafNode)t;
									}
									int h;
									for(h = 0; h < change.number; h++) {
										if(change.arr[h].key == key) {
											break;
										}
									}
									int _key;
									if(h == change.number-1) {
										Node t = change.right;
										while(!t.isLeaf) {
											NonLeafNode target1 = (NonLeafNode)t;
											if(target1.number == 0) {
												t = target1.right;
											}
											else {
												t = target1.arr[0].leftChild;
											}
										}
										
										LeafNode min = (LeafNode)t;
										_key = min.arr[0].key;
									}
									else {
										Node t = change.arr[h+1].leftChild;
										
										while(!t.isLeaf) {
											NonLeafNode target1 = (NonLeafNode)t;
											if(target1.number == 0) {
												t = target1.right;
											}
											else {
												t = target1.arr[0].leftChild;
											}
										}
										LeafNode min = (LeafNode)t;
										_key = min.arr[0].key;
									} 
									change.arr[h].key = _key;
									return temp.parent;
									}
								}
							}
							else if(i == 1) {
								LeafNode sibling = (LeafNode)temp.parent.arr[i-1].leftChild;
								if(sibling.number-1 >= underBound) {
									//sibling과 재배치
									Data data = sibling.arr[sibling.number-1];
									sibling.arr[sibling.number-1] = null;
									sibling.number--;
									int k;
									for(k = 0; k < temp.number; k++) {
										if(temp.arr[k].key == key) {
											break;
										}
									}
									for(int j = k; j > 0; j--) {
										temp.arr[j] = temp.arr[j-1];
									}
									temp.arr[0] = data;
									temp.parent.arr[i-1].key = temp.arr[0].key;
									Node forRoot = temp;
									while(!forRoot.isRoot) {
										forRoot = forRoot.parent;
									}
									root.root = forRoot;
									if(forRoot.isLeaf) {
										return forRoot;
									}
									NonLeafNode change = (NonLeafNode)forRoot;
									
									while(!find(change, key)) {
										Node t = findLocation(change, key);
										if(t.isLeaf) {
											return temp.parent;
										}
										change = (NonLeafNode)t;
									}
									int h;
									for(h = 0; h < change.number; h++) {
										if(change.arr[h].key == key) {
											break;
										}
									}
									int _key;
									if(h == change.number-1) {
										Node t = change.right;
										while(!t.isLeaf) {
											NonLeafNode target1 = (NonLeafNode)t;
											if(target1.number == 0) {
												t = target1.right;
											}
											else {
												t = target1.arr[0].leftChild;
											}
										}
										LeafNode min = (LeafNode)t;
										_key = min.arr[0].key;
									}
									else {
										Node t = change.arr[h+1].leftChild;
										while(!t.isLeaf) {
											NonLeafNode target1 = (NonLeafNode)t;
											if(target1.number == 0) {
												t = target1.right;
											}
											else {
												t = target1.arr[0].leftChild;
											}
										}
										LeafNode min = (LeafNode)t;
										_key = min.arr[0].key;
									} 
									change.arr[h].key = _key;
									
									return temp.parent;
								}
								else {
									if(temp.parent.number == 1) {
										//sibling과 병합	
										int k;
										for(k = 0; k < temp.number; k++) {
											if(temp.arr[k].key == key) {
												break;
											}
										}
										for(int j = k; j < temp.number-1; j++) {
											temp.arr[j] = temp.arr[j+1];
										}
										temp.number--;
										int l = sibling.number;
										int a = temp.number;
										for(int j = 0; j < a; j++) {
											sibling.arr[l+j] = temp.arr[j];
											sibling.number++;
										}
										sibling.sibling = temp.sibling;
										if(temp.parent.parent == null) {
											temp = null;
											sibling.isRoot = true;
											sibling.parent = null;
											sibling.root = sibling;
											return sibling;
										}
										temp = null;
										sibling.parent.number--;
										sibling.parent.right = sibling;
										
										Node forRoot = sibling;
										while(!forRoot.isRoot) {
											forRoot = forRoot.parent;
										}
										root.root = forRoot;
										if(forRoot.isLeaf) {
											return forRoot;
										}
										NonLeafNode change = (NonLeafNode)forRoot;
										
										while(!find(change, key)) {
											Node t = findLocation(change, key);
											if(t.isLeaf) {
												if(t.parent.isRoot) {
													t.isRoot = true;
													t.root = t;
													t.parent = null;
													return t;
												}	
												else {
													sibling.parent.right = sibling;
													return sibling.parent;
												}
											}
											change = (NonLeafNode)t;
										}
										int h;
										for(h = 0; h < change.number; h++) {
											if(change.arr[h].key == key) {
												break;
											}
										}
										int _key;
										if(h == change.number-1) {
											Node t = change.right;
											while(!t.isLeaf) {
												NonLeafNode target1 = (NonLeafNode)t;
												if(target1.number == 0) {
													t = target1.right;
												}
												else {
													t = target1.arr[0].leftChild;
												}
											}
											LeafNode min = (LeafNode)t;
											_key = min.arr[0].key;
										}
										else {
											Node t = change.arr[h+1].leftChild;
											while(!t.isLeaf) {
												NonLeafNode target1 = (NonLeafNode)t;
												if(target1.number == 0) {
													t = target1.right;
												}
												else {
													t = target1.arr[0].leftChild;
												}
											}
											LeafNode min = (LeafNode)t;
											_key = min.arr[0].key;
										} 
										change.arr[h].key = _key;
										return sibling.parent;
									}
									else { //temp.parent.number != 1
										LeafNode sibling1 = (LeafNode)temp.sibling;
										if(sibling1.number-1 >= underBound) {
											//sibling1과 재배치
											int k; //Leaf에서 key locate
											for(k = 0; k < temp.number; k++) {
												if(temp.arr[k].key == key) {
													break;
												}
											}
											for(int j = k; j < temp.number-1; j++) {
												temp.arr[j] = temp.arr[j+1];
											}
											Data data = sibling1.arr[0];
											for(int j = 1; j < sibling1.number; j++) {
												sibling1.arr[j-1] = sibling1.arr[j];
											}
											sibling1.number--;
											sibling1.arr[sibling1.number] = null;
											temp.arr[temp.number-1] = data;
											temp.parent.arr[i].key = sibling1.arr[0].key;
											
											Node forRoot = temp;
											while(!forRoot.isRoot) {
												forRoot = forRoot.parent;
											}
											root.root = forRoot;
											if(forRoot.isLeaf) {
												return forRoot;
											}
											NonLeafNode change = (NonLeafNode)forRoot;
											
											while(!find(change, key)) {
												Node t = findLocation(change, key);
												if(t.isLeaf) {
													return temp.parent;
												}
												change = (NonLeafNode)t;
											}
											int h;
											for(h = 0; h < change.number; h++) {
												if(change.arr[h].key == key) {
													break;
												}
											}
											int _key;
											if(h == change.number-1) {
												Node t = change.right;
												while(!t.isLeaf) {
													NonLeafNode target1 = (NonLeafNode)t;
													if(target1.number == 0) {
														t = target1.right;
													}
													else {
														t = target1.arr[0].leftChild;
													}
												}
												LeafNode min = (LeafNode)t;
												_key = min.arr[0].key;
											}
											else {
												Node t = change.arr[h+1].leftChild;
												while(!t.isLeaf) {
													NonLeafNode target1 = (NonLeafNode)t;
													if(target1.number == 0) {
														t = target1.right;
													}
													else {
														t = target1.arr[0].leftChild;
													}
												}
												LeafNode min = (LeafNode)t;
												_key = min.arr[0].key;
											} 
											change.arr[h].key = _key;
											
											return temp.parent;
										}
										else {
											//sibling과 병합
											int k;
											for(k = 0; k < temp.number; k++) {
												if(temp.arr[k].key == key) {
													break;
												}
											}
											for(int j = k; j < temp.number-1; j++) {
												temp.arr[j] = temp.arr[j+1];
											}
											temp.number--;
											int l = sibling.number;
											int a = temp.number;
											for(int j = 0; j < a; j++) {
												sibling.arr[l+j] = temp.arr[j];
												sibling.number++;
											}
											
											sibling.sibling = temp.sibling;
											temp = null;
											
											sibling.parent.arr[i].leftChild = sibling; //error
											sibling.parent.number--;
											for(int j = 0; j < sibling.parent.number; j++) {
												sibling.parent.arr[j] = sibling.parent.arr[j+1];
											}
											sibling.parent.arr[sibling.parent.number] = null;
											
											Node forRoot = sibling;
											while(!forRoot.isRoot) {
												forRoot = forRoot.parent;
											}
											root.root = forRoot;
											if(forRoot.isLeaf) {
												return forRoot;
											}
											NonLeafNode change = (NonLeafNode)forRoot;
											
											while(!find(change, key)) {
												Node t = findLocation(change, key);
												if(t.isLeaf) {
													return sibling.parent;
												}
												change = (NonLeafNode)t;
											}
											int h;
											for(h = 0; h < change.number; h++) {
												if(change.arr[h].key == key) {
													break;
												}
											}
											int _key;
											if(h == change.number-1) {
												Node t = change.right;
												while(!t.isLeaf) {
													NonLeafNode target1 = (NonLeafNode)t;
													if(target1.number == 0) {
														t = target1.right;
													}
													else {
														t = target1.arr[0].leftChild;
													}
												}
												LeafNode min = (LeafNode)t;
												_key = min.arr[0].key;
											}
											else {
												Node t = change.arr[h+1].leftChild;
												while(!t.isLeaf) {
													NonLeafNode target1 = (NonLeafNode)t;
													if(target1.number == 0) {
													t = target1.right;
													}
													else {
														t = target1.arr[0].leftChild;
													}
												}
												LeafNode min = (LeafNode)t;
												_key = min.arr[0].key;
											} 
											change.arr[h].key = _key;
											return sibling.parent;
											
										}
									}
								}
							}
							else if(i == temp.parent.number) {
								//여긴데
								LeafNode sibling = (LeafNode)temp.parent.arr[i-1].leftChild;
								if(sibling.number-1 >= underBound) {
									Data data = sibling.arr[sibling.number-1];
									sibling.arr[sibling.number-1] = null;
									sibling.number--;
									int k;
									for(k = 0; k < temp.number; k++) {
										if(temp.arr[k].key == key) {
											break;
										}
									}
									for(int j = k; j > 0; j--) {
										temp.arr[j] = temp.arr[j-1];
									}
									temp.arr[0] = data;
									temp.parent.arr[i-1].key = temp.arr[0].key;
									
									Node forRoot = temp;
									while(!forRoot.isRoot) {
										forRoot = forRoot.parent;
									}
									root.root = forRoot;
									if(forRoot.isLeaf) {
										return forRoot;
									}
									NonLeafNode change = (NonLeafNode)forRoot;
									
									while(!find(change, key)) {
										Node t = findLocation(change, key);
										if(t.isLeaf) {
											return temp.parent;
										}
										change = (NonLeafNode)t;
									}
									int h;
									for(h = 0; h < change.number; h++) {
										if(change.arr[h].key == key) {
											break;
										}
									}
									int _key;
									if(h == change.number-1) {
										Node t = change.right;
										while(!t.isLeaf) {
											NonLeafNode target1 = (NonLeafNode)t;
											if(target1.number == 0) {
												t = target1.right;
											}
											else {
												t = target1.arr[0].leftChild;
											}
										}
										LeafNode min = (LeafNode)t;
										_key = min.arr[0].key;
									}
									else {
										Node t = change.arr[h+1].leftChild;
										while(!t.isLeaf) {
											NonLeafNode target1 = (NonLeafNode)t;
											if(target1.number == 0) {
												t = target1.right;
											}
											else {
												t = target1.arr[0].leftChild;
											}
										}
										LeafNode min = (LeafNode)t;
										_key = min.arr[0].key;
									} 
									change.arr[h].key = _key;
									
									return temp.parent;
								}
								else { //병합
									if(temp.parent.number == 1) { //i==1
										int k;
										for(k = 0; k < temp.number; k++) {
											if(temp.arr[k].key == key) {
												break;
											}
										}
										for(int j = k; j < temp.number-1; j++) {
											temp.arr[j] = temp.arr[j+1];
										}
										temp.number--;
										int l = sibling.number;
										int a = temp.number;
										for(int j = 0; j < a; j++) {
											sibling.arr[l+j] = temp.arr[j];
											sibling.number++;
										}
										sibling.sibling = temp.sibling;
										temp = null;
										sibling.parent.number--;
										sibling.parent.arr[sibling.parent.number] = null;
										if(sibling.parent.parent == null) {
											sibling.isRoot = true;
											sibling.parent = null;
											sibling.root = sibling;
											return sibling;
										}
										sibling.parent.right = sibling;
										Node forRoot = sibling;
										root.root = forRoot;
										while(!forRoot.isRoot) {
											forRoot = forRoot.parent;
										}
										if(forRoot.isLeaf) {
											return forRoot;
										}
										NonLeafNode change = (NonLeafNode)forRoot;
										
										while(!find(change, key)) {
											Node t = findLocation(change, key);
											if(t.isLeaf) {
												if(t.parent.isRoot) {
													t.isRoot = true;
													t.root = t;
													t.parent = null;
													return t;
												}	
												else {
													sibling.parent.right = sibling;
													return sibling.parent;
												}
											}
											change = (NonLeafNode)t;
										}
										int h;
										for(h = 0; h < change.number; h++) {
											if(change.arr[h].key == key) {
												break;
											}
										}
										change.arr[h].key = sibling.arr[0].key;
										return sibling.parent;
										
									}
									else {
										int k;
										for(k = 0; k < temp.number; k++) {
											if(temp.arr[k].key == key) {
												break;
											}
										}
										for(int j = k; j < temp.number-1; j++) {
											temp.arr[j] = temp.arr[j+1];
										}
										temp.number--;
										temp.arr[temp.number] = null;
										int l = sibling.number;
										int a = temp.number;
										for(int j = 0; j < a; j++) {
											sibling.arr[l+j] = temp.arr[j];
											sibling.number++;
										}
										sibling.sibling = temp.sibling;
										//temp.sibling = null;
										sibling.parent.right = sibling;
										sibling.parent.number--;
										sibling.parent.arr[sibling.parent.number] = null;
										temp = null;
										
										Node forRoot = sibling;
										while(!forRoot.isRoot) {
											forRoot = forRoot.parent;
										}
										root.root = forRoot;
										if(forRoot.isLeaf) {
											return forRoot;
										}
										NonLeafNode change = (NonLeafNode)forRoot;
										
										while(!find(change, key)) {
											Node t = findLocation(change, key);
											if(t.isLeaf) {
												return sibling.parent;
											}
											change = (NonLeafNode)t;
										}
										int h;
										for(h = 0; h < change.number; h++) {
											if(change.arr[h].key == key) {
												break;
											}
										}
										int _key;
										if(h == change.number-1) {
											Node t = change.right;
											while(!t.isLeaf) {
												NonLeafNode target1 = (NonLeafNode)t;
												if(target1.number == 0) {
													t = target1.right;
												}
												else {
													t = target1.arr[0].leftChild;
												}
											}
											LeafNode min = (LeafNode)t;
											_key = min.arr[0].key;
										}
										else {
											Node t = change.arr[h+1].leftChild;
											while(!t.isLeaf) {
												NonLeafNode target1 = (NonLeafNode)t;
												if(target1.number == 0) {
													t = target1.right;
												}
												else {
													t = target1.arr[0].leftChild;
												}
											}
											LeafNode min = (LeafNode)t;
											_key = min.arr[0].key;
										} 
										change.arr[h].key = _key;
										return sibling.parent;
									}
								}
						}
						else { // i != 0, 1, temp.parent.number;
							LeafNode sibling = (LeafNode)temp.parent.arr[i-1].leftChild; //빌려올곳 ->이거 체크해야댐
							//확인
							if(sibling.number-1 >= underBound) { //왼쪽 sibling
								//재배치
								Data data = sibling.arr[sibling.number-1];
								sibling.arr[sibling.number-1] = null;
								sibling.number--;
								int k;
								for(k = 0; k < temp.number; k++) {
									if(temp.arr[k].key == key) {
										break;
									}
								}
								for(int j = k; j < temp.number-1; j++) {
									temp.arr[j] = temp.arr[j+1];
								}
								for(int j = temp.number-1; j > 0; j--) {
									temp.arr[j] = temp.arr[j-1];
								}
								temp.arr[0] = data;
								temp.parent.arr[i-1].key = temp.arr[0].key;
								
								Node forRoot = temp;
								while(!forRoot.isRoot) {
									forRoot = forRoot.parent;
								}
								root.root = forRoot;
								if(forRoot.isLeaf) {
									return forRoot;
								}
								NonLeafNode change = (NonLeafNode)forRoot;
								
								while(!find(change, key)) {
									Node t = findLocation(change, key);
									if(t.isLeaf) {
										return temp.parent;
									}
									change = (NonLeafNode)t;
								}
								int h;
								for(h = 0; h < change.number; h++) {
									if(change.arr[h].key == key) {
										break;
									}
								}
								int _key;
								if(h == change.number-1) {
									Node t = change.right;
									while(!t.isLeaf) {
										NonLeafNode target1 = (NonLeafNode)t;
										t = target1.arr[0].leftChild;
									}
									LeafNode min = (LeafNode)t;
									_key = min.arr[0].key;
								}
								else {
									Node t = change.arr[h+1].leftChild;
									while(!t.isLeaf) {
										NonLeafNode target1 = (NonLeafNode)t;
										t = target1.arr[0].leftChild;
									}
									LeafNode min = (LeafNode)t;
									_key = min.arr[0].key;
								} 
								change.arr[h].key = _key;
								
								return temp.parent;
							}
							else {
								
								if(temp.sibling != null && temp.sibling.parent == temp.parent) {
									LeafNode sibling1 = (LeafNode)temp.sibling;
									//확인
									if(sibling1.number-1 >= underBound) { //오른쪽 sibling
										//재배치
										
											int k; //Leaf에서 key locate
											for(k = 0; k < temp.number; k++) {
												if(temp.arr[k].key == key) {
													break;
												}
											}
											for(int j = k; j < temp.number-1; j++) {
												temp.arr[j] = temp.arr[j+1];
											}
											Data data = sibling1.arr[0];
											for(int j = 1; j < sibling1.number; j++) {
												sibling1.arr[j-1] = sibling1.arr[j];
											}
											sibling1.number--;
											sibling1.arr[sibling1.number] = null;
											temp.arr[temp.number-1] = data;
											temp.parent.arr[i].key = sibling1.arr[0].key;
											
											Node forRoot = temp;
											while(!forRoot.isRoot) {
												forRoot = forRoot.parent;
											}
											root.root = forRoot;
											if(forRoot.isLeaf) {
												return forRoot;
											}
											NonLeafNode change = (NonLeafNode)forRoot;
											
											while(!find(change, key)) {
												Node t = findLocation(change, key);
												if(t.isLeaf) {
													return temp.parent;
												}
												change = (NonLeafNode)t;
											}
											int h;
											for(h = 0; h < change.number; h++) {
												if(change.arr[h].key == key) {
													break;
												}
											}
											int _key;
											if(h == change.number-1) {
												Node t = change.right;
												while(!t.isLeaf) {
													NonLeafNode target1 = (NonLeafNode)t;
													if(target1.number == 0) {
														t = target1.right;
													}
													else {
														t = target1.arr[0].leftChild;
													}
												}
												LeafNode min = (LeafNode)t;
												_key = min.arr[0].key;
											}
											else {
												Node t = change.arr[h+1].leftChild;
												while(!t.isLeaf) {
													NonLeafNode target1 = (NonLeafNode)t;
													if(target1.number == 0) {
														t = target1.right;
													}
													else {
														t = target1.arr[0].leftChild;
													}
												}
												LeafNode min = (LeafNode)t;
												_key = min.arr[0].key;
											} 
											change.arr[h].key = _key;
											
											return temp.parent;
									}
								}
								//왼쪽 sibling이랑 병합
								if(temp.parent.number == 1) {
									int k;
									for(k = 0; k < temp.number; k++) {
										if(temp.arr[k].key == key) {
											break;
										}
									}
									for(int j = k; j < temp.number-1; j++) {
										temp.arr[j] = temp.arr[j+1];
									}
									temp.number--;
									int l = sibling.number;
									int a = temp.number;
									for(int j = 0; j < a; j++) {
										sibling.arr[l+j] = temp.arr[j];
										sibling.number++;
									}
									sibling.sibling = temp.sibling;
									temp = null;
									sibling.parent.number--;
									
									if(sibling.parent.parent == null) {
										sibling.isRoot = true;
										sibling.parent = null;
										sibling.root = sibling;
										return sibling;
									}
									sibling.parent.right = sibling;
									Node forRoot = sibling;
									root.root = forRoot;
									while(!forRoot.isRoot) {
										forRoot = forRoot.parent;
									}
									if(forRoot.isLeaf) {
										return forRoot;
									}
									NonLeafNode change = (NonLeafNode)forRoot;
									
									while(!find(change, key)) {
										Node t = findLocation(change, key);
										if(t.isLeaf) {
											if(t.parent.isRoot) {
												t.isRoot = true;
												t.root = t;
												t.parent = null;
												return t;
											}	
											else {
												sibling.parent.right = sibling;
												return sibling.parent;
											}
										}
										change = (NonLeafNode)t;
									}
									int h;
									for(h = 0; h < change.number; h++) {
										if(change.arr[h].key == key) {
											break;
										}
									}
									int _key;
									if(h == change.number-1) {
										Node t = change.right;
										while(!t.isLeaf) {
											NonLeafNode target1 = (NonLeafNode)t;
											if(target1.number == 0) {
												t = target1.right;
											}
											else {
												t = target1.arr[0].leftChild;
											}
										}
										LeafNode min = (LeafNode)t;
										_key = min.arr[0].key;
									}
									else {
										Node t = change.arr[h+1].leftChild;
										while(!t.isLeaf) {
											NonLeafNode target1 = (NonLeafNode)t;
											if(target1.number == 0) {
												t = target1.right;
											}
											else {
												t = target1.arr[0].leftChild;
											}
										}
										LeafNode min = (LeafNode)t;
										_key = min.arr[0].key;
									} 
									change.arr[h].key = _key;
									return sibling.parent;
									
								}
								else {
									int k;
									for(k = 0; k < temp.number; k++) {
										if(temp.arr[k].key == key) {
											break;
										}
									}
									for(int j = k; j < temp.number-1; j++) {
										temp.arr[j] = temp.arr[j+1];
									}
									temp.number--;
									int l = sibling.number;
									int a = temp.number;
									for(int j = 0; j < a; j++) {
										sibling.arr[l+j] = temp.arr[j];
										sibling.number++;
									}
								
									sibling.sibling = temp.sibling;
									temp.parent.arr[i].leftChild = sibling; //error
									temp.parent.number--;
									for(int j = i-1; j < temp.parent.number; j++) {
										temp.parent.arr[j] = temp.parent.arr[j+1];
									}
									temp.parent.arr[temp.parent.number] = null;
									temp = null;
									Node forRoot = sibling;
									while(!forRoot.isRoot) {
										if(forRoot.parent == null) {
											break;
										}
										forRoot = forRoot.parent;
									}
									root.root = forRoot;
									if(forRoot.isLeaf) {
										return forRoot;
									}
									NonLeafNode change = (NonLeafNode)forRoot;
									
									while(!find(change, key)) {
										Node t = findLocation(change, key);
										if(t.isLeaf) {
											return sibling.parent;
										}
										change = (NonLeafNode)t;
									}
									int h;
									for(h = 0; h < change.number; h++) {
										if(change.arr[h].key == key) {
											break;
										}
									}
									int _key;
									if(h == change.number-1) {
										Node t = change.right;
										while(!t.isLeaf) {
											NonLeafNode target1 = (NonLeafNode)t;
											if(target1.number == 0) {
												t = target1.right;
											}
											else {
												t = target1.arr[0].leftChild;
											}
										}
										LeafNode min = (LeafNode)t;
										_key = min.arr[0].key;
									}
									else {
										Node t = change.arr[h+1].leftChild;
										while(!t.isLeaf) {
											NonLeafNode target1 = (NonLeafNode)t;
											if(target1.number == 0) {
												t = target1.right;
											}
											else {
												t = target1.arr[0].leftChild;
											}
										}
										LeafNode min = (LeafNode)t;
										_key = min.arr[0].key;
									} 
									change.arr[h].key = _key;
									return sibling.parent;
								}
							}
						}
					
					
					}
					else { //그냥 삭제하고 parent의 값만 바꿔주면 댐
						int i;
						for(i = 0; i < temp.number; i++) {
							if(temp.arr[i].key == key) {
								break;
							}
						}
						for(int j = i; j < temp.number; j++) {
							Data data = temp.arr[j+1];
							temp.arr[j] = data;
						}
						temp.number--;
						temp.arr[temp.number] = null;
						
						Node forRoot = temp;
						while(!forRoot.isRoot) {
							forRoot = forRoot.parent;
						}
						if(forRoot.isLeaf) {
							return forRoot;
						}
						root.root = forRoot;
						NonLeafNode change = (NonLeafNode)forRoot;
						
						while(!find(change, key)) {
							Node t = findLocation(change, key);
							if(t.isLeaf) {
								return temp.parent;
							}
							change = (NonLeafNode)t;
						}
						int h;
						for(h = 0; h < change.number; h++) {
							if(change.arr[h].key == key) {
								break;
							}
						}
						int _key;
						if(h == change.number-1) {
							Node t = change.right;
							while(!t.isLeaf) {
								NonLeafNode target1 = (NonLeafNode)t;
								if(target1.number == 0) {
									t = target1.right;
								}
								else {
									t = target1.arr[0].leftChild;
								}
							}
							LeafNode min = (LeafNode)t;
							_key = min.arr[0].key;
						}
						else {
							Node t = change.arr[h+1].leftChild;
							while(!t.isLeaf) {
								NonLeafNode target1 = (NonLeafNode)t;
								if(target1.number == 0) {
									t = target1.right;
								}
								else {
									t = target1.arr[0].leftChild;
								}
							}
							LeafNode min = (LeafNode)t;
							_key = min.arr[0].key;
						} 
						change.arr[h].key = _key;
						
						return temp.parent;
					}
				}
			}
			else {
				System.out.println("Deletion Error : NotFound");
				if(root.parent == null) {
					return root;
				}
				else {
					return root.parent;
				}
			}
		}		
	}
	
	public void setNodeNum(Node root) {
		arr.add(root);
		int _index = 0;
		if(root == null) {
			return;
		}
		arr.get(arr.size()-1).nodeNumber = arr.size()-1;
		if(root.isLeaf) {
			return;
		}
		while(true) {
			if(arr.get(_index).isLeaf) {
				break;
			}
			NonLeafNode target = (NonLeafNode)arr.get(_index);
			for(int i = 0; i < target.number; i++) {
				arr.add(target.arr[i].leftChild);
				arr.get(arr.size()-1).nodeNumber = arr.size()-1;
			}
			arr.add(target.right);
			arr.get(arr.size()-1).nodeNumber = arr.size()-1;
			_index++;
		}
	}
	public String saveTree(Node temp) {
		String line = "";
		if(temp == null) {
			return line;
		}
		
		String pNum;
		if(temp.parent == null) {
			pNum = "null";
		}
		else {
			pNum = temp.parent.nodeNumber + "";
		}
		if(temp.isLeaf) {
			LeafNode target = (LeafNode)temp;
			int selfCount = target.number;
			line += target.nodeNumber + " " + target.isLeaf + " " + pNum + " " + selfCount + " ";
			for(int j = 0; j < target.number; j++) {
				if(j == target.number-1) {
					line += target.arr[j].key + "," + target.arr[j].value;
				}
				else {
					line += target.arr[j].key + "," + target.arr[j].value + "/";
				}
			}
			line += "\n";
		}
		else { //NonLeafNode
			NonLeafNode target = (NonLeafNode)temp;
			int selfCount = target.number;
			line += target.nodeNumber + " " + target.isLeaf + " " + pNum + " " + selfCount + " ";
			for(int j = 0; j < target.number; j++) {
				if(j == target.number-1) {
					line += target.arr[j].key;
				}
				else {
					line += target.arr[j].key + "/";
				}
			}
			line += "\n";
		}
		return line;
	}
	
	public static void main(String[] args) {
		try {
			BPlusTree BT = new BPlusTree();
			Node root = null;
			first = root;
			File index = new File(args[1]);
			FileWriter indexWriter = new FileWriter(index, true);
			FileReader indexReader = new FileReader(index);
			BufferedReader br = new BufferedReader(indexReader);
			if(args[0].equals("-c")) {
				FileWriter indexWriter1 = new FileWriter(index);
				BufferedWriter bw1 = new BufferedWriter(indexWriter1); //덮어쓰기 아마 다 덮어쓰기일듯
				bw1.write(args[2] + "");
				bw1.newLine();
				bw1.close();
				indexWriter1.close();
			}
			else if(args[0].equals("-i")) {
				File input = new File(args[2]);
				BufferedReader inputBr = new BufferedReader(new FileReader(input));
				String line1;
				m = Integer.parseInt(br.readLine());
				while((line1 = br.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(line1, " ");
					int nodeNum = Integer.parseInt(st.nextToken()); //nodeNum
					boolean isL = Boolean.parseBoolean(st.nextToken()); //isLeaf
					String pNum = st.nextToken();

					int selfCount = Integer.parseInt(st.nextToken()); //자신의 key개수
					StringTokenizer st1 = new StringTokenizer(st.nextToken(), "/");
					Node target;
					if(isL) { //LeafNode일떄
						LeafNode temp = new LeafNode(m);
						int i = 0;
						while(st1.hasMoreTokens()) {
							StringTokenizer st2 = new StringTokenizer(st1.nextToken(), ",");
							int key = Integer.parseInt(st2.nextToken());
							int value = Integer.parseInt(st2.nextToken());
							Data data = new Data(key , value);
							temp.arr[i] = data;
							i++;
						}
						temp.number = selfCount;
						target = temp;
						target.parentNumber = pNum;
					}
					else { //NonLeafNode일때
						NonLeafNode temp = new NonLeafNode(m);
						int i = 0;
						while(st1.hasMoreTokens()) {
							temp.arr[i] = new KeyPoint(Integer.parseInt(st1.nextToken()), null);
							i++;
						}
						temp.number = selfCount;
						target = temp;
						target.parentNumber = pNum;
					}
					target.isLeaf = isL;
					target.nodeNumber = nodeNum;
					//여기까지 하나의 node를 만들었다.
					nodeArr.add(target.nodeNumber, target);
				}
				//nodeArr에 다 넣었다.
				for(Node temp1 : nodeArr) {
					Node target = temp1;
					if(target.parentNumber.equals("null")) {
						root = target;
						root.isRoot = true;
					}
					else {
						NonLeafNode parent = (NonLeafNode)nodeArr.get(Integer.parseInt(target.parentNumber));
						if(target.isLeaf) {
							LeafNode temp = (LeafNode)target;
							int key = temp.arr[0].key;
							int j;
							for(j = 0; j < parent.number; j++) {
								if(parent.arr[j].key > key) {
									break;
								}
							}
							if(j == parent.number) {
								parent.right = temp;
							}
							else {
								parent.arr[j].leftChild = temp;
							}
							temp.parent = parent;
						}
						else {
							NonLeafNode temp = (NonLeafNode)target;
							int key = temp.arr[0].key;
							int j;
							for(j = 0; j < parent.number; j++) {
								if(parent.arr[j].key > key) {
									break;
								}
							}
							if(j == parent.number) {
								parent.right = temp;
							}
							else {
								parent.arr[j].leftChild = temp;
							}
							temp.parent = parent;
						}	
					}
				}
				Node t = root;
				if(t != null) {
					while(!t.isLeaf) {
						NonLeafNode target = (NonLeafNode)t;
						t = target.arr[0].leftChild;
					}
					//t가 제일 왼쪽 LeafNode에 왔을떄
					first = t;
					int index_ = t.nodeNumber;
					for(int i = index_; i < nodeArr.size(); i++) {
						if(i == nodeArr.size()-1) {
							LeafNode temp1 = (LeafNode)nodeArr.get(i);
							temp1.sibling = null;
						}
						else {
							LeafNode temp1 = (LeafNode)nodeArr.get(i);
							LeafNode temp2 = (LeafNode)nodeArr.get(i+1);
							temp1.sibling = temp2;
						}
					}
				}
				//tree 생성완료!!;
				if(root == null) {
					LeafNode temp = new LeafNode(m);
					root = temp;
				}
				while((line1 = inputBr.readLine()) != null) {
					int key, value;
					StringTokenizer st = new StringTokenizer(line1, ",");
					key = Integer.parseInt(st.nextToken());
					value = Integer.parseInt(st.nextToken());
					Data data = new Data(key, value);
					root = BT.insert(root, data);
				}
				BT.setNodeNum(root);
				//tree의 정보를 index파일에 담기
				FileWriter indexWriter1 = new FileWriter(index);
				BufferedWriter bw1 = new BufferedWriter(indexWriter1); //덮어쓰기 아마 다 덮어쓰기일듯n
				bw1.write(m + "\n");
				bw1.flush();
				for(Node temp1 : arr) {
					String l = BT.saveTree(temp1);
					bw1.write(l);
					bw1.flush();
				}
				inputBr.close();
				indexWriter1.close();
				bw1.close();
			}
			else if(args[0].equals("-d")) {
				File input = new File(args[2]);
				BufferedReader inputBr = new BufferedReader(new FileReader(input));
				String line1;
				m = Integer.parseInt(br.readLine());
				while((line1 = br.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(line1, " ");
					int nodeNum = Integer.parseInt(st.nextToken()); //nodeNum
					boolean isL = Boolean.parseBoolean(st.nextToken()); //isLeaf
					String pNum = st.nextToken();

					int selfCount = Integer.parseInt(st.nextToken()); //자신의 key개수
					StringTokenizer st1 = new StringTokenizer(st.nextToken(), "/");
					Node target;
					if(isL) { //LeafNode일떄
						LeafNode temp = new LeafNode(m);
						int i = 0;
						while(st1.hasMoreTokens()) {
							StringTokenizer st2 = new StringTokenizer(st1.nextToken(), ",");
							int key = Integer.parseInt(st2.nextToken());
							int value = Integer.parseInt(st2.nextToken());
							Data data = new Data(key , value);
							temp.arr[i] = data;
							i++;
						}
						temp.number = selfCount;
						target = temp;
						target.parentNumber = pNum;
					}
					else { //NonLeafNode일때
						NonLeafNode temp = new NonLeafNode(m);
						int i = 0;
						while(st1.hasMoreTokens()) {
							temp.arr[i] = new KeyPoint(Integer.parseInt(st1.nextToken()), null);
							i++;
						}
						temp.number = selfCount;
						target = temp;
						target.parentNumber = pNum;
					}
					target.isLeaf = isL;
					target.nodeNumber = nodeNum;
					//여기까지 하나의 node를 만들었다.
					nodeArr.add(target.nodeNumber, target);
				}
				//nodeArr에 다 넣었다.
				for(Node temp1 : nodeArr) {
					Node target = temp1;
					if(target.parentNumber.equals("null")) {
						root = target;
						root.isRoot = true;
					}
					else {
						NonLeafNode parent = (NonLeafNode)nodeArr.get(Integer.parseInt(target.parentNumber));
						if(target.isLeaf) {
							LeafNode temp = (LeafNode)target;
							int key = temp.arr[0].key;
							int j;
							for(j = 0; j < parent.number; j++) {
								if(parent.arr[j].key > key) {
									break;
								}
							}
							if(j == parent.number) {
								parent.right = temp;
							}
							else {
								parent.arr[j].leftChild = temp;
							}
							temp.parent = parent;
						}
						else {
							NonLeafNode temp = (NonLeafNode)target;
							int key = temp.arr[0].key;
							int j;
							for(j = 0; j < parent.number; j++) {
								if(parent.arr[j].key > key) {
									break;
								}
							}
							if(j == parent.number) {
								parent.right = temp;
							}
							else {
								parent.arr[j].leftChild = temp;
							}
							temp.parent = parent;
						}	
					}
				}
				Node t = root;
				if(t != null) {
					while(!t.isLeaf) {
						NonLeafNode target = (NonLeafNode)t;
						t = target.arr[0].leftChild;
					}
					//t가 제일 왼쪽 LeafNode에 왔을떄
					first = t;
					int index_ = t.nodeNumber;
					for(int i = index_; i < nodeArr.size(); i++) {
						if(i == nodeArr.size()-1) {
							LeafNode temp1 = (LeafNode)nodeArr.get(i);
							temp1.sibling = null;
						}
						else {
							LeafNode temp1 = (LeafNode)nodeArr.get(i);
							LeafNode temp2 = (LeafNode)nodeArr.get(i+1);
							temp1.sibling = temp2;
						}
					}
				}
				//tree 생성완료!!;
				while((line1 = inputBr.readLine()) != null) {
					int key;
					key = Integer.parseInt(line1);
					root = BT.delete(root, key);
				}
				BT.setNodeNum(root);
				FileWriter indexWriter1 = new FileWriter(index);
				BufferedWriter bw1 = new BufferedWriter(indexWriter1); //덮어쓰기 아마 다 덮어쓰기일듯
				bw1.write(m + "\n");
				bw1.flush();
				for(Node temp1 : arr) {
					String l = BT.saveTree(temp1);
					bw1.write(l);
					bw1.flush();
				}
				inputBr.close();
				indexWriter1.close();
				bw1.close();
			}
			else if(args[0].equals("-s")) {
				int key_ = Integer.parseInt(args[2]);
				String line1;
				m = Integer.parseInt(br.readLine());
				while((line1 = br.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(line1, " ");
					int nodeNum = Integer.parseInt(st.nextToken()); //nodeNum
					boolean isL = Boolean.parseBoolean(st.nextToken()); //isLeaf
					String pNum = st.nextToken();

					int selfCount = Integer.parseInt(st.nextToken()); //자신의 key개수
					StringTokenizer st1 = new StringTokenizer(st.nextToken(), "/");
					Node target;
					if(isL) { //LeafNode일떄
						LeafNode temp = new LeafNode(m);
						int i = 0;
						while(st1.hasMoreTokens()) {
							StringTokenizer st2 = new StringTokenizer(st1.nextToken(), ",");
							int key = Integer.parseInt(st2.nextToken());
							int value = Integer.parseInt(st2.nextToken());
							Data data = new Data(key , value);
							temp.arr[i] = data;
							i++;
						}
						temp.number = selfCount;
						target = temp;
						target.parentNumber = pNum;
					}
					else { //NonLeafNode일때
						NonLeafNode temp = new NonLeafNode(m);
						int i = 0;
						while(st1.hasMoreTokens()) {
							temp.arr[i] = new KeyPoint(Integer.parseInt(st1.nextToken()), null);
							i++;
						}
						temp.number = selfCount;
						target = temp;
						target.parentNumber = pNum;
					}
					target.isLeaf = isL;
					target.nodeNumber = nodeNum;
					//여기까지 하나의 node를 만들었다.
					nodeArr.add(target.nodeNumber, target);
				}
				//nodeArr에 다 넣었다.
				for(Node temp1 : nodeArr) {
					Node target = temp1;
					if(target.parentNumber.equals("null")) {
						root = target;
						root.isRoot = true;
					}
					else {
						NonLeafNode parent = (NonLeafNode)nodeArr.get(Integer.parseInt(target.parentNumber));
						if(target.isLeaf) {
							LeafNode temp = (LeafNode)target;
							int key = temp.arr[0].key;
							int j;
							for(j = 0; j < parent.number; j++) {
								if(parent.arr[j].key > key) {
									break;
								}
							}
							if(j == parent.number) {
								parent.right = temp;
							}
							else {
								parent.arr[j].leftChild = temp;
							}
							temp.parent = parent;
						}
						else {
							NonLeafNode temp = (NonLeafNode)target;
							int key = temp.arr[0].key;
							int j;
							for(j = 0; j < parent.number; j++) {
								if(parent.arr[j].key > key) {
									break;
								}
							}
							if(j == parent.number) {
								parent.right = temp;
							}
							else {
								parent.arr[j].leftChild = temp;
							}
							temp.parent = parent;
						}	
					}
				}
				Node t = root;
				if(t != null) {
					while(!t.isLeaf) {
						NonLeafNode target = (NonLeafNode)t;
						t = target.arr[0].leftChild;
					}
					//t가 제일 왼쪽 LeafNode에 왔을떄
					first = t;
					int index_ = t.nodeNumber;
					for(int i = index_; i < nodeArr.size(); i++) {
						if(i == nodeArr.size()-1) {
							LeafNode temp1 = (LeafNode)nodeArr.get(i);
							temp1.sibling = null;
						}
						else {
							LeafNode temp1 = (LeafNode)nodeArr.get(i);
							LeafNode temp2 = (LeafNode)nodeArr.get(i+1);
							temp1.sibling = temp2;
						}
					}
				}
				//tree 생성완료!!;
				BT.singleKeySearch(root, key_);
			}
			else if(args[0].equals("-r")) {
				int from = Integer.parseInt(args[2]);
				int to = Integer.parseInt(args[3]);
				String line1;
				m = Integer.parseInt(br.readLine());
				while((line1 = br.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(line1, " ");
					int nodeNum = Integer.parseInt(st.nextToken()); //nodeNum
					boolean isL = Boolean.parseBoolean(st.nextToken()); //isLeaf
					String pNum = st.nextToken();

					int selfCount = Integer.parseInt(st.nextToken()); //자신의 key개수
					StringTokenizer st1 = new StringTokenizer(st.nextToken(), "/");
					Node target;
					if(isL) { //LeafNode일떄
						LeafNode temp = new LeafNode(m);
						int i = 0;
						while(st1.hasMoreTokens()) {
							StringTokenizer st2 = new StringTokenizer(st1.nextToken(), ",");
							int key = Integer.parseInt(st2.nextToken());
							int value = Integer.parseInt(st2.nextToken());
							Data data = new Data(key , value);
							temp.arr[i] = data;
							i++;
						}
						temp.number = selfCount;
						target = temp;
						target.parentNumber = pNum;
					}
					else { //NonLeafNode일때
						NonLeafNode temp = new NonLeafNode(m);
						int i = 0;
						while(st1.hasMoreTokens()) {
							temp.arr[i] = new KeyPoint(Integer.parseInt(st1.nextToken()), null);
							i++;
						}
						temp.number = selfCount;
						target = temp;
						target.parentNumber = pNum;
					}
					target.isLeaf = isL;
					target.nodeNumber = nodeNum;
					//여기까지 하나의 node를 만들었다.
					nodeArr.add(target.nodeNumber, target);
				}
				//nodeArr에 다 넣었다.
				for(Node temp1 : nodeArr) {
					Node target = temp1;
					if(target.parentNumber.equals("null")) {
						root = target;
						root.isRoot = true;
					}
					else {
						NonLeafNode parent = (NonLeafNode)nodeArr.get(Integer.parseInt(target.parentNumber));
						if(target.isLeaf) {
							LeafNode temp = (LeafNode)target;
							int key = temp.arr[0].key;
							int j;
							for(j = 0; j < parent.number; j++) {
								if(parent.arr[j].key > key) {
									break;
								}
							}
							if(j == parent.number) {
								parent.right = temp;
							}
							else {
								parent.arr[j].leftChild = temp;
							}
							temp.parent = parent;
						}
						else {
							NonLeafNode temp = (NonLeafNode)target;
							int key = temp.arr[0].key;
							int j;
							for(j = 0; j < parent.number; j++) {
								if(parent.arr[j].key > key) {
									break;
								}
							}
							if(j == parent.number) {
								parent.right = temp;
							}
							else {
								parent.arr[j].leftChild = temp;
							}
							temp.parent = parent;
						}	
					}
				}
				Node t = root;
				if(t != null) {
					while(!t.isLeaf) {
						NonLeafNode target = (NonLeafNode)t;
						t = target.arr[0].leftChild;
					}
					//t가 제일 왼쪽 LeafNode에 왔을떄
					first = t;
					int index_ = t.nodeNumber;
					for(int i = index_; i < nodeArr.size(); i++) {
						if(i == nodeArr.size()-1) {
							LeafNode temp1 = (LeafNode)nodeArr.get(i);
							temp1.sibling = null;
						}
						else {
							LeafNode temp1 = (LeafNode)nodeArr.get(i);
							LeafNode temp2 = (LeafNode)nodeArr.get(i+1);
							temp1.sibling = temp2;
						}
					}
				}
				//tree 생성완료!!;
				BT.rangedSearch(root, from, to);
			}
			else {
				System.out.println("Command Error : try again");
				return ;
			}
			br.close();
			indexReader.close();
		}catch(IOException e) {
				System.out.println("File is not open!!");
		}
	}
}

