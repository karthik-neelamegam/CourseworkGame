package logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
//example of encapsulation (or something similar like cohesiveness) in OOP
public class Immutable2DArrayList<T> {
	
	private ArrayList<ArrayList<T>> matrix;

	
	public Immutable2DArrayList(ArrayList<ArrayList<T>> matrix) {
		this.matrix = matrix;
	}

	public T get(int x, int y) {
		return matrix.get(x).get(y);
	}

	public int width() {
		return matrix.size();
	}

	public int height(int x) {
		return matrix.get(x).size();
	}

}
