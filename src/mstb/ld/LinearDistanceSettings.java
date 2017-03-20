package mstb.ld;
import ij.Prefs;
import ij.measure.ResultsTable;
import mstb.Tools;

public class LinearDistanceSettings {
	public String[] resultsTable = { "Mean", "Median", "Sum", "Variance", "StDev", "Number" };
	public String[] measurementsTable = { "White X", "White Y", "White X and Y", "Black X", "Black Y", "Black X and Y",
			"Black and White X", "Black and White Y", "All" };
	public Double step;
	public Integer markLength = 5;
	public Boolean doCalibrateStep, doIterateAllImages, doExcludeEdges, doShowOverlay, doAggregate, doApplyCalibration;
	public ResultsTable restable = new ResultsTable();
	public boolean[] doResults, doMeasurements;
	private final String prefix = "LinearDistance.";
	
	public LinearDistanceSettings() {
		load();
	}
	
	public void load() {
		step = Prefs.get("stepSize", 1);
		doApplyCalibration = Prefs.get(prefix + "doApplyCalibration", true);
		doCalibrateStep = Prefs.get(prefix + "doCalibrateStep", false);
		doIterateAllImages = Prefs.get(prefix + "doIterateAllImages", true);
		doExcludeEdges = Prefs.get(prefix + "doExcludeEdges", true);
		doShowOverlay = Prefs.get(prefix + "doShowOverlay", true);
		doAggregate = Prefs.get(prefix + "doAggregate", true);
		doMeasurements = Tools.StringToBoolean(Prefs.get(prefix + "doMeasurements",""), measurementsTable.length);
		doResults = Tools.StringToBoolean(Prefs.get(prefix + "doResults",""), resultsTable.length);
	}
	
	public void save() {	
		Prefs.set(prefix + "doApplyScale", doApplyCalibration);
		Prefs.set(prefix + "stepSize", step);
		Prefs.set(prefix + "doCalibrateStep", doCalibrateStep);
		Prefs.set(prefix + "doIterateAllImages", doIterateAllImages);
		Prefs.set(prefix + "doExcludeEdges", doExcludeEdges);
		Prefs.set(prefix + "doShowOverlay", doShowOverlay);
		Prefs.set(prefix + "doAggregate", doAggregate);
		Prefs.set(prefix + "doMeasurements", Tools.BooleanToString(doMeasurements));
		Prefs.set(prefix + "doResults", Tools.BooleanToString(doResults));
		
		Prefs.savePreferences();
	}
}
