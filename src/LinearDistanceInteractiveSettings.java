import java.awt.Color;
import java.lang.reflect.Field;

import ij.Prefs;
import ij.measure.ResultsTable;

public class LinearDistanceInteractiveSettings {
	public String[] resultsTable = { "Mean", "Median", "Sum", "Variance", "StDev", "Number" };
	public String[] directions = { "Horizontal", "Vertical" };
	public Double step, offset;
	public Integer markLength = 5;
	public Boolean doCalibrateStep, doCalibrateOffset, doCenterLines;
	public Boolean directionY;
	public String overlayColor;
	public ResultsTable restable = new ResultsTable();
	public Integer remtol = 10;
	public boolean[] doResults;
	public boolean doApplyCalibration;
	private final String prefix = "LinearDistanceInteractive.";
	
	public LinearDistanceInteractiveSettings() {
		load();
	}
	
	public Color getovlColor() {
		try {
		    Field field = Color.class.getField(overlayColor.toUpperCase());
		    return (Color)field.get(null);
		} catch (Exception e) {
			return Color.RED;
		}
	}
	
	public void load() {
		step = Prefs.get(prefix + "stepSize", 50);
		offset = Prefs.get(prefix + "offset", 25);
		markLength = Tools.getRoundedInt(Prefs.get(prefix + "markLength", 5));
		doCalibrateStep = Prefs.get(prefix + "doCalibrateStep", false);
		doCalibrateOffset = Prefs.get(prefix + "doCalibrateOffset", false);
		doCenterLines = Prefs.get(prefix + "doCenterLines", true);
		directionY = Prefs.get(prefix + "directionY", false);
		overlayColor=Prefs.get(prefix + "overlayColor", "Red");
		doResults = Tools.StringToBoolean(Prefs.get(prefix + "doResults", ""), resultsTable.length);
		doApplyCalibration = Prefs.get(prefix + "doApplyCalibration", true);
	}
	
	public void save() {
		Prefs.set(prefix + "stepSize", step);
		Prefs.set(prefix + "doCalibrateStep", doCalibrateStep);
		Prefs.set(prefix + "doCenterLines", doCenterLines);
		Prefs.set(prefix + "offset", offset);
		Prefs.set(prefix + "directionY", directionY);
		Prefs.set(prefix + "doCalibrateOffset", doCalibrateOffset);
		Prefs.set(prefix + "markLength", markLength);
		Prefs.set(prefix + "overlayColor", overlayColor);
		Prefs.set(prefix + "doResults", Tools.BooleanToString(doResults));
		Prefs.set(prefix + "doApplyCalibration", doApplyCalibration);
		
		Prefs.savePreferences();
	}
}
