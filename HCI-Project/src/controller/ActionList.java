package controller;

import java.util.ArrayList;

import view.MainScreen;

// to perform undo/redo action
public class ActionList {

	public static final int CREATE = 0;
	public static final int DELETE = 1;
	
	ArrayList<Action> list;
	
	int actionIndex;
	
	public ActionList() {
		actionIndex = 0;
		list = new ArrayList<Action>();
		list.add(null);
	}
	
	// remove all actions beyond action index
	public void addAction(Object first, Object second, int mode) {
		int size = list.size();
		for(int index = ++actionIndex; index < size; index++) {
			list.remove(actionIndex);
		}
		
		list.add(new Action(first, second, mode));
		
		// notice buttons that an action was added
		MainScreen.checkButtons();
	}
	
	// set new index and return the last action made
	public Action undo() {
		if(--actionIndex < 0) { actionIndex = 0; return null; }
		else return list.get(actionIndex+1);
	}
	
	// set new index and return the previous undoed action
	public Action redo() {
		if(++actionIndex >= list.size()) { actionIndex = list.size() - 1; return null; }
		else return list.get(actionIndex);
	}
	
	public boolean firstAction() {
		return actionIndex == 0;
	}
	
	public boolean lastAction() {
		return actionIndex == list.size()-1;
	}
	
	public void removeAll(Object object) {
		int index = 0;
		
		while(index < list.size()) {
			Action action = list.get(index);
			
			if(action != null && 
				(action.getFirst().equals(object) || action.getSecond().equals(object))) {
				list.remove(index--);
			}
			index++;
		}
		
		if(actionIndex >= list.size()) { actionIndex = list.size()-1; }
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
