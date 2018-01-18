package dsa;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CommonAlgorithms {
	private static <T> void merge(List<T> paths, Comparator<T> comparator, int startIndex, int midIndex, int endIndex) {
		List<T> leftHalf = new ArrayList<T>();
		List<T> rightHalf = new ArrayList<T>();
		for (int i = 0; i < midIndex; i++) {
			leftHalf.add(paths.get(i));
		}
		for (int i = midIndex; i < paths.size(); i++) {
			rightHalf.add(paths.get(i));
		}
		int leftIndex = 0;
		int rightIndex = 0;
		int insertIndex = startIndex;
		while (insertIndex < endIndex && leftIndex < leftHalf.size()
				&& rightIndex < rightHalf.size()) {
			if(comparator.compare(leftHalf.get(leftIndex), rightHalf.get(rightIndex)) < 0) {
				paths.set(insertIndex, leftHalf.get(leftIndex));
			} else {
				paths.set(insertIndex, rightHalf.get(rightIndex));
			}
			insertIndex++;
		}
	}

	private static <T> void mergeSort(List<T> paths, Comparator<T> comparator, int startIndex, int endIndex) {
		if (startIndex < endIndex) {
			int midIndex = (startIndex + endIndex) / 2;
			mergeSort(paths, comparator, startIndex, midIndex);
			mergeSort(paths, comparator, midIndex, endIndex);
			merge(paths, comparator, startIndex, midIndex, endIndex);
		}
	}
	public static <T> void mergeSort(List<T> paths, Comparator<T> comparator) {
		mergeSort(paths, comparator, 0, paths.size());
	}
}
