package mstb.pai;
import java.awt.Color;
import ij.Prefs;
import ij.measure.ResultsTable;
import mstb.Tools;

public class PointAnalysisInteractiveSettings {
	public Integer pointsX  = 20, pointsY = 20, numDomains = 2;
	public String overlayColor;
	public boolean randomizePoints;
	public ResultsTable restable = new ResultsTable();
	public Integer remtol = 10;
	public Integer markLength = 6; 
	
	private final String prefix = "PointAnalysisInteractive.";
	
	public PointAnalysisInteractiveSettings() {
		load();
	}
	
	public Color getovlColor() {
		return Tools.getColorByName(overlayColor.toUpperCase());
	}
	
	public void load() {
		pointsX = (int) Prefs.get(prefix + "pointsX", 20);
		pointsY = (int) Prefs.get(prefix + "pointsY", 20);
		numDomains = (int) Prefs.get(prefix + "numDomains", 2);
		randomizePoints = Prefs.get(prefix + "randomizePoints", false);
		overlayColor = Prefs.get(prefix + "overlayColor", "Red");
	}
	
	public void save() {
		Prefs.set(prefix + "pointsX", pointsX);
		Prefs.set(prefix + "pointsY", pointsY);
		Prefs.set(prefix + "numDomains", numDomains);
		Prefs.set(prefix + "randomizePoints", randomizePoints);
		Prefs.set(prefix + "overlayColor", overlayColor);
		
		Prefs.savePreferences();
	}
}
