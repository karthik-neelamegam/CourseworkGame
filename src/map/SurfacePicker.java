package map;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import user_interface.Application;

public class SurfacePicker {
	
	private HashMap<Surface, Double> surfaceRatios;
	private double totalRatio;

	public SurfacePicker(HashMap<Surface, Double> surfaceRatios) {
		this.surfaceRatios = surfaceRatios;
		totalRatio = 0;
		for (Entry<Surface, Double> entry : surfaceRatios.entrySet()) {
			double ratio = entry.getValue();
			totalRatio += ratio;
		}
	}
	
	public static SurfacePicker getUniformSurfacePicker() {
		HashMap<Surface, Double> surfaceRatios = new HashMap<Surface, Double>();
		for (Surface surface : Surface.values()) { 
			 surfaceRatios.put(surface, 1.0);
		}
		return new SurfacePicker(surfaceRatios);
	}

	public Surface getRandomSurface() {
		double rand = Application.rng.nextDouble() * totalRatio;
		double cumulativeRatios = 0;
		Iterator<Entry<Surface, Double>> surfaceRatiosIterator = surfaceRatios
				.entrySet().iterator();
		Entry<Surface, Double> entry = null;
		do {
			entry = surfaceRatiosIterator.next();
			double ratio = entry.getValue();
			cumulativeRatios += ratio;
		} while(cumulativeRatios < rand);
		return entry.getKey();
	}
}
