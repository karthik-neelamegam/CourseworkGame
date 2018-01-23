package map;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map.Entry;

import user_interface.Application;

public class SurfacePicker {
	
	private EnumMap<Surface, Double> surfaceRatios;
	private double totalRatio;

	public SurfacePicker(EnumMap<Surface, Double> surfaceRatios) {
		this.surfaceRatios = surfaceRatios;
		totalRatio = 0;
		for (Entry<Surface, Double> entry : surfaceRatios.entrySet()) {
			double ratio = entry.getValue();
			totalRatio += ratio;
		}
	}
	
	public static SurfacePicker getDefaultSurfacePicker() {
		EnumMap<Surface, Double> surfaceRatios = new EnumMap<Surface, Double>(Surface.class);
		surfaceRatios.put(Surface.SLOW,1d);
		surfaceRatios.put(Surface.NORMAL,20d);
		surfaceRatios.put(Surface.FAST,1d);
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
