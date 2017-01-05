
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
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class LinearDistanceInteractive implements PlugInFilter {
	private String[] resultsTable = { "Mean", "Median", "Sum", "Variance", "StDev", "Number" };
	private String[] directions = { "Horizontal", "Vertical" };
	private ResultsTable rt = new ResultsTable();
	private String[] colorList = {"Red","Green","Blue","Yellow","Orange","Purple","Black","White"};

	public int setup(String arg, ImagePlus imp) {
		if (imp != null && !showDialog())
			return DONE;
		return DOES_ALL;
	}

	boolean showDialog() {
		double step, offset;
		int markLength = 5;
		boolean doCalibrateStep, doCalibrateOffset, doCenterLines;
		boolean directionY;
		String overlayColor; 
		
		step = Prefs.get("LinearDistanceInteractive.stepSize", 50);
		offset = Prefs.get("LinearDistanceInteractive.offset", 25);
		doCalibrateStep = Prefs.get("LinearDistanceInteractive.doCalibrateStep", false);
		doCalibrateOffset = Prefs.get("LinearDistanceInteractive.doCalibrateOffset", false);
		doCenterLines = Prefs.get("LinearDistanceInteractive.doCenterLines", true);
		directionY = Prefs.get("LinearDistanceInteractive.directionY", false);
		overlayColor = Prefs.get("LinearDistanceInteractive.overlayColor", "Red");

		GenericDialog gd = new GenericDialog("Linear Distances by Simon Klein");
		gd.addMessage("Interactive Linear Distances Plugin, created by Simon Klein");
		gd.addMessage("This plug-in allows the interactive measurement of linear distances in X or Y direction.");
		gd.addNumericField("Step distance between measures in pixels/units", step, 1);
		gd.addCheckbox("Step distance in units", doCalibrateStep);
		gd.addCheckbox("Center lines between borders", doCenterLines);
		gd.addNumericField("Minimum margin left and right in pixels/units", offset, 1);
		gd.addCheckbox("Offset distance in units", doCalibrateOffset);
		gd.addChoice("Direction", directions, ((directionY) ? directions[1] : directions[0]));
		gd.addChoice("Overlay color", colorList, "Red");

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		step = Math.max(gd.getNextNumber(), 1d);
		doCalibrateStep = gd.getNextBoolean();
		doCenterLines = gd.getNextBoolean();
		offset = Math.max(gd.getNextNumber(), 1d);
		doCalibrateOffset = gd.getNextBoolean();
		directionY = gd.getNextChoice() == directions[1];
		overlayColor = gd.getNextChoice();
		
		Prefs.set("LinearDistanceInteractive.stepSize", step);
		Prefs.set("LinearDistanceInteractive.doCalibrateStep", doCalibrateStep);
		Prefs.set("LinearDistanceInteractive.doCenterLines", doCenterLines);
		Prefs.set("LinearDistanceInteractive.offset", offset);
		Prefs.set("LinearDistanceInteractive.directionY", directionY);
		Prefs.set("LinearDistanceInteractive.doCalibrateOffset", doCalibrateOffset);
		Prefs.set("LinearDistanceInteractive.markLength", markLength);
		Prefs.set("LinearDistanceInteractive.overlayColor", overlayColor);

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

		LinearDistanceInteractiveMenuStrip menuStrip = new LinearDistanceInteractiveMenuStrip(iplus, resultsTable, rt);
		iplus.getWindow().add(menuStrip);
		menuStrip.interactionHandler.updateSize();
	}
}
