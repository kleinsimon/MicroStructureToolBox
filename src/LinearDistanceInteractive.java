
//=====================================================
//      Name:           LinearDistance
//      Project:        Measures the linear distance between binary inversions in x and y direction
//      Version:        0.3.2
//      Author:         Simon Klein, simon.klein@simonklein.de
//      Date:           24.11.2015
//      Comment:		Buildfile taken from Patrick Pirrotte       

import java.awt.Component;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class LinearDistanceInteractive implements PlugInFilter {
	private String[] resultsTable = { "Mean", "Median", "Sum", "Variance", "StDev", "Number" };
	private String[] directions = { "Horizontal", "Vertical" };

	private Double step = 50.d;
	private Double offset = 25.d;

	private int markLength = 5;
	private boolean doCalibrateStep, doCalibrateOffset;
	private boolean directionY = false;
	private boolean[] doResults;
	private ResultsTable rt = new ResultsTable();

	public int setup(String arg, ImagePlus imp) {
		if (imp != null && !showDialog())
			return DONE;
		return DOES_ALL;
	}

	boolean showDialog() {
		step = Prefs.get("LinearDistanceInteractive.stepSize", 50);
		offset = Prefs.get("LinearDistanceInteractive.stepSize", 25);
		doCalibrateStep = Prefs.get("LinearDistanceInteractive.doCalibrateStep", false);
		doCalibrateOffset = Prefs.get("LinearDistanceInteractive.doCalibrateStep", false);
		directionY = Prefs.get("LinearDistanceInteractive.directionY", false);

		GenericDialog gd = new GenericDialog("Linear Distances by Simon Klein");
		gd.addMessage("Interactive Linear Distances Plugin, created by Simon Klein");
		gd.addMessage("This plug-in allows the interactive measurement of linear distances in X or Y direction.");
		gd.addNumericField("Step distance between measures in pixels/units", step, 1);
		gd.addCheckbox("Step distance in units", doCalibrateStep);
		gd.addNumericField("Step distance between measures in pixels/units", offset, 1);
		gd.addCheckbox("Step distance in units", doCalibrateOffset);
		gd.addChoice("Direction", directions, ((directionY) ? directions[1] : directions[0]));

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		step = Math.max(gd.getNextNumber(), 1d);
		doCalibrateStep = gd.getNextBoolean();
		offset = Math.max(gd.getNextNumber(), 1d);
		doCalibrateOffset = gd.getNextBoolean();
		directionY = gd.getNextChoice() == directions[1];

		Prefs.set("LinearDistanceInteractive.stepSize", step);
		Prefs.set("LinearDistanceInteractive.doCalibrateStep", doCalibrateStep);
		Prefs.set("LinearDistanceInteractive.stepSize", offset);
		Prefs.set("LinearDistanceInteractive.directionY", directionY);
		Prefs.set("LinearDistanceInteractive.doCalibrateOffset", doCalibrateStep);

		Prefs.savePreferences();
		return true;
	}

	public void run(ImageProcessor ip) {
		analyzeImage(ij.IJ.getImage());
	}

	public void showMessage(String message) {
		new ij.gui.MessageDialog(ij.WindowManager.getCurrentWindow(), "", message);
	}

	public void analyzeImage(ImagePlus iplus) {
		for (Component comp : iplus.getWindow().getComponents()) {
			if (comp instanceof LinearDistanceInteractiveMenuStrip) {
				if (!((LinearDistanceInteractiveMenuStrip) comp).remove())
					return;
			}
		}

		int lineDistancepx = step.intValue();
		int offsetpx = offset.intValue();

		Calibration cal = iplus.getCalibration();

		if (doCalibrateStep) {
			lineDistancepx = Tools.getRoundedInt((Double) step / ((directionY) ? cal.pixelWidth : cal.pixelHeight));
			offsetpx = Tools.getRoundedInt((Double) offset / ((directionY) ? cal.pixelWidth : cal.pixelHeight));
		}

		LinearDistanceInteractiveMenuStrip menuStrip = new LinearDistanceInteractiveMenuStrip(lineDistancepx, offsetpx,
				markLength, directionY, iplus, resultsTable, doResults, rt);
		iplus.getWindow().add(menuStrip);
		menuStrip.interactionHandler.updateSize();
	}
}
