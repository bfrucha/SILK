package controller;

import java.util.LinkedList;

// to perform undo/redo action
public class ActionList {

	public static final int CREATE = 0;
	public static final int DELETE = 1;
	
	LinkedList<Action> list;
	
	int actionIndex;
	
	public ActionList() {
		actionIndex = 0;
		list = new LinkedList<Action>();
		list.add(null);
	}
	
	public void addAction(Object first, Object second, int mode) {
		list.add(++actionIndex, new Action(first, second, mode));
		
		int tmpIndex = actionIndex+1;
		while(tmpIndex < list.size()) {
			list.removeLast();
			tmpIndex++;
		}
		System.out.println(actionIndex);
	}
	
	// set new index and return the last action made
	public Action undo() {
		actionIndex--;
		if(actionIndex < 0) { actionIndex = 0; return null; }
		else return list.get(actionIndex+1);
	}
	
	// set new index and return the previous undoed action
	public Action redo() {
		actionIndex++;
		if(actionIndex >= list.size()) { actionIndex = list.size() - 1; return null; }
		else return list.get(actionIndex);
	}
	
	protected class Action {
		
		private Object first;
		private Object second;
		
		private int mode;
		
		public Action(Object first, Object second, int mode) {
			this.first = first;
			this.second = second;
			
			this.mode = mode;
		}
		
		public Object getFirst() {
			return first;
		}
		
		public Object getSecond() {
			return second;
		}
		
		public int getMode() {
			return mode;
		}
	}
	
}
