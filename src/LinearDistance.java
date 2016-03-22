
//=====================================================
//      Name:           LinearDistance 2 Phase Binary
//      Project:        Measures the linear distance between binary inversions in x and y direction
//      Version:        0.3.3
//      Author:         Simon Klein, simon.klein@simonklein.de
//      Date:           24.11.2015
//      Comment:		Buildfile taken from Patrick Pirrotte       

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.ImageRoi;
import ij.Prefs;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.Blitter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class LinearDistance implements PlugInFilter {
	private String[] measurementsTable = { "White X", "White Y", "White X and Y", "Black X", "Black Y", "Black X and Y",
			"Black and White X", "Black and White Y", "All" };
	private String[] resultsTable = { "Mean", "Median", "Sum", "Variance", "StDev", "Number" };
	private Double step;
	private boolean doApplyCalibration, doCalibrateStep, doIterateAllImages, doExcludeEdges, doShowOverlay;
	private boolean[] doMeasurements;
	private boolean[] doResults;
	private ResultsTable rt = new ResultsTable();

	public int setup(String arg, ImagePlus imp) {
		if (imp != null && !showDialog())
			return DONE;
		rt.reset();
		return DOES_8G;
	}

	boolean showDialog() {
		step = Prefs.get("LinearDistance.stepSize", 1);
		doApplyCalibration = Prefs.get("LinearDistance.doApplyCalibration", true);
		doCalibrateStep = Prefs.get("LinearDistance.doCalibrateStep", false);
		doIterateAllImages = Prefs.get("LinearDistance.doIterateAllImages", true);
		doExcludeEdges = Prefs.get("LinearDistance.doExcludeEdges", true);
		doShowOverlay = Prefs.get("LinearDistance.doShowOverlay", true);
		doMeasurements = Tools.StringToBoolean(Prefs.get("LinearDistance.doMeasurements",""), measurementsTable.length);
		doResults = Tools.StringToBoolean(Prefs.get("LinearDistance.doResults",""), resultsTable.length);

		GenericDialog gd = new GenericDialog("Linear Distances by Simon Klein");
		gd.addMessage("Linear Distances Plugin, created by Simon Klein");

		gd.addMessage("This plug-in measures the linear distances in x and y directions for binary images.");
		gd.addCheckbox("Apply image calbration", doApplyCalibration);
		gd.addNumericField("Step distance between measures in pixels/units", step, 1);
		gd.addCheckbox("Step distance in units", doCalibrateStep);
		gd.addCheckbox("Measure all opened Images", doIterateAllImages);
		gd.addCheckbox("Exclude stripes cut by Edges", doExcludeEdges);
		gd.addCheckbox("Show measured pixels as overlay", doShowOverlay);

		gd.addMessage("Measurements");
		gd.addCheckboxGroup(2, 5, measurementsTable, doMeasurements);
		gd.addMessage("Results");
		gd.addCheckboxGroup(1, 6, resultsTable, doResults);

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		doApplyCalibration = gd.getNextBoolean();
		step = Math.max(gd.getNextNumber(), 1d);
		doCalibrateStep = gd.getNextBoolean();
		doIterateAllImages = gd.getNextBoolean();
		doExcludeEdges = gd.getNextBoolean();
		doShowOverlay = gd.getNextBoolean();
		for (int i = 0; i < doMeasurements.length; i++) {
			doMeasurements[i] = gd.getNextBoolean();
		}
		for (int i = 0; i < doResults.length; i++) {
			doResults[i] = gd.getNextBoolean();
		}

		Prefs.set("LinearDistance.doApplyScale", doApplyCalibration);
		Prefs.set("LinearDistance.stepSize", step);
		Prefs.set("LinearDistance.doCalibrateStep", doCalibrateStep);
		Prefs.set("LinearDistance.doIterateAllImages", doIterateAllImages);
		Prefs.set("LinearDistance.doExcludeEdges", doExcludeEdges);
		Prefs.set("LinearDistance.doShowOverlay", doShowOverlay);
		Prefs.set("LinearDistance.doMeasurements", Tools.BooleanToString(doMeasurements));
		Prefs.set("LinearDistance.doResults", Tools.BooleanToString(doResults));
		Prefs.savePreferences();
		return true;
	}

	public void run(ImageProcessor ip) {
		if (doIterateAllImages) {
			for (int id : ij.WindowManager.getIDList()) {
				analyzeImage(ij.WindowManager.getImage(id));
			}
		} else {
			analyzeImage(ij.IJ.getImage());
		}
	}

	public void showMessage(String message) {
		ij.gui.MessageDialog e = new ij.gui.MessageDialog(ij.WindowManager.getCurrentWindow(), "", message);
	}

	public void doAnalyzeImage(int[][] pixels, Boolean goX, long step, List<Double> w, List<Double> b,
			ImageProcessor overlay, double calib) {
		int color = (goX) ? Color.RED.getRGB() : Color.GREEN.getRGB();
		int count = 0;
		Boolean now = null;
		Boolean last = null;
		Boolean isLast = null;
		Boolean isFirst = null;
		Boolean onEdge = null;

		for (int x = 0; x < pixels.length; x += step) {
			onEdge = true;
			isFirst = true;
			for (int y = 0; y < pixels[x].length; y++) {
				now = (pixels[x][y] == (255));
				if (y == 0)
					last = now;
				isLast = y == pixels[x].length - 1;
				if (isLast || isFirst)
					onEdge = true;
				else
					onEdge = false;

				if ((now != last || isLast)) {
					if (!(doExcludeEdges && onEdge)) {
						if (last)
							w.add((double) (count + 1) * calib);
						else
							b.add((double) (count + 1) * calib);
						if (doShowOverlay && overlay != null) {
							for (int yi = 0; yi <= count; yi++) {
								int ny = Math.max(0, y - yi - 1);
								if (goX)
									overlay.set(ny, x, color);
								else
									overlay.set(x, ny, color);
							}
						}
					}

					count = 0;
					isFirst = false;
				}
				if ((now == last)) {
					count++;
				}
				last = now;
				now = null;
			}

			last = null;
			count = 0;
		}
	}

	private void analyzeImage(ImagePlus iplus) {
		ImageProcessor ip = iplus.getProcessor();
		ImageProcessor oix = null;
		ImageProcessor oiy = null;
		long lineDistanceX = step.intValue();
		long lineDistanceY = step.intValue();
		if (doShowOverlay) {
			oix = new ColorProcessor(ip.getWidth(), ip.getHeight());
			oiy = new ColorProcessor(ip.getWidth(), ip.getHeight());
			oix.setColor(Color.TRANSLUCENT);
			oiy.setColor(Color.TRANSLUCENT);
			oix.fill();
			oiy.fill();
		}

		int[][] pixels = ip.getIntArray();
		int[][] pixelsRotate = new int[pixels[0].length][pixels.length];

		for (int row = 0; row < pixels.length; row++)
			for (int col = 0; col < pixels[row].length; col++)
				pixelsRotate[col][row] = pixels[row][col];

		List<Double> wdy = new ArrayList<Double>();
		List<Double> bdy = new ArrayList<Double>();
		List<Double> wdx = new ArrayList<Double>();
		List<Double> bdx = new ArrayList<Double>();

		Double calx = 1.0d;
		Double caly = 1.0d;
		String unit = "px";

		Calibration cal = iplus.getCalibration();
		if (doApplyCalibration && cal.scaled()) {
			calx = cal.pixelWidth;
			caly = cal.pixelHeight;

			unit = cal.getXUnit();
			if (unit != cal.getYUnit()) {
				unit += "/" + cal.getYUnit();
			}
		}

		if (doCalibrateStep) {
			lineDistanceX = Math.round((Double) step / calx);
			lineDistanceY = Math.round((Double) step / caly);
		}

		doAnalyzeImage(pixels, false, lineDistanceX, wdy, bdy, oiy, caly);
		doAnalyzeImage(pixelsRotate, true, lineDistanceY, wdx, bdx, oix, calx);

		//{ "White X", "White Y", "White X and Y", "Black X", "Black Y", "Black X and Y", "Black and White X", "Black and White Y", "All" }
		Stat[] Stats = { new Stat(wdx), new Stat(wdy), new Stat(wdx, wdy), new Stat(bdx),
				new Stat(bdy), new Stat(bdx, bdy), new Stat(wdx, bdx), new Stat(wdy, bdy), new Stat(wdx, bdy) };

		if (doShowOverlay) {
			oix.copyBits(oiy, 0, 0, Blitter.ADD);
			ImageRoi roi = new ImageRoi(0, 0, oix);
			roi.setName(iplus.getShortTitle() + " measured stripes");
			roi.setOpacity(0.5d);
			iplus.deleteRoi();
			iplus.setRoi(roi, true);
		}
		rt.incrementCounter();
		int row = rt.getCounter() - 1;

		rt.setValue("Image", row, iplus.getTitle());
		rt.setValue("Unit", row, unit);
		if (doApplyCalibration && cal.scaled()) {
			rt.setValue("Calibration X/Y", row, calx.toString() + ((!calx.equals(caly)) ? "/" + caly.toString() : ""));
		}
		String rName, mName, cName;
		Double rValue;

		for (int mi = 0; mi < doMeasurements.length; mi++) {
			if (!doMeasurements[mi])
				continue;
			mName = measurementsTable[mi];
			for (int ri = 0; ri < doResults.length; ri++) {
				if (!doResults[ri])
					continue;
				rName = resultsTable[ri];
				cName = String.format("%s %s", mName, rName);
				rValue = Stats[mi].getFormattedValue(ri);
				rt.setValue(cName, row, rValue);
			}
		}

		rt.show("Linear Distances Results");
	}

	public void showData(String name, double value, double scaledValue) {
		rt.incrementCounter();
		int row = rt.getCounter() - 1;
		rt.setValue("Name", row, name);
		rt.setValue("Value [px]", row, value);
		rt.setValue("Value [Scale]", row, scaledValue);
	}

	public void showData(String name, double value) {
		showData(name, value, 0);
	}
}
