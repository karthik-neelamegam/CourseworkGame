package map;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map.Entry;

import user_interface.Application;

public class SurfacePicker {
	
	private EnumMap<Surface, Double> surfaceRatios;
	private double totalRatio;
	
	//potential to add more surfaces if an enummap instead of storing ratio for each surface in a separate variable
	public SurfacePicker(EnumMap<Surface, Double> surfaceRatios) {
		this.surfaceRatios = surfaceRatios;
		totalRatio = 0;
		for (Entry<Surface, Double> entry : surfaceRatios.entrySet()) {
			double ratio = entry.getValue();
			totalRatio += ratio;
		}
	}
			
	public Surface getRandomSurface() {
		double rand = Application.rng.nextDouble() * totalRatio;
		double cumulativeRatios = 0;
		Iterator<Entry<Surface, Double>> surfaceRatiosIterator = surfaceRatios
				.entrySet().iterator();
		Entry<Surface, Double> entry = null;
		do {
			entry = surfaceRatiosIterator.next();
			cumulativeRatios += entry.getValue();
		} while(cumulativeRatios < rand);
		return entry.getKey();
	}
}
