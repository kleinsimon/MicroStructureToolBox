
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

public class PointAnalysisInteractive implements PlugInFilter {
	private ResultsTable rt = new ResultsTable();

	public int setup(String arg, ImagePlus imp) {
		if (imp != null && !showDialog())
			return DONE;
		return DOES_ALL;
	}

	boolean showDialog() {
		Double pointsX, pointsY = 20.d, numDomains = 2.0;
		String overlayColor;
		boolean randomizePoints;

		pointsX = Prefs.get("PointAnalysisInteractive.pointsX", 20);
		pointsY = Prefs.get("PointAnalysisInteractive.pointsY", 20);
		numDomains = Prefs.get("PointAnalysisInteractive.numDomains", 2);
		randomizePoints = Prefs.get("PointAnalysisInteractive.randomizePoints", false);
		overlayColor = Prefs.get("PointAnalysisInteractive.overlayColor", "Red");

		GenericDialog gd = new GenericDialog("Linear Distances by Simon Klein");
		gd.addMessage("Point Domain Analysis Plugin, created by Simon Klein");
		gd.addMessage("This plug-in allows the interactive assignment of points to a given number of domains.");
		gd.addNumericField("Number of points X / Total number of points if randomized", pointsX, 0);
		gd.addNumericField("Number of points Y", pointsY, 0);
		gd.addNumericField("Number of domains", numDomains, 0);
		gd.addCheckbox("Randomize Points", randomizePoints);
		gd.addChoice("Overlay color", ij.plugin.Colors.colors, "Red");

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		pointsX = Math.max(gd.getNextNumber(), 1d);
		pointsY = Math.max(gd.getNextNumber(), 1d);
		numDomains = Math.max(gd.getNextNumber(), 2d);
		randomizePoints = gd.getNextBoolean();
		overlayColor = gd.getNextChoice();
		
		
		Prefs.set("PointAnalysisInteractive.pointsX", pointsX);
		Prefs.set("PointAnalysisInteractive.pointsY", pointsY);
		Prefs.set("PointAnalysisInteractive.numDomains", numDomains);
		Prefs.set("PointAnalysisInteractive.randomizePoints", randomizePoints);
		Prefs.set("PointAnalysisInteractive.overlayColor", overlayColor);
		Prefs.set("PointAnalysisInteractive.markLength", 6.0);

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
			if (comp instanceof PointAnalysisInteractiveMenuStrip) {
				if (!((PointAnalysisInteractiveMenuStrip) comp).remove())
					return;
			}
		}


		PointAnalysisInteractiveMenuStrip menuStrip = new PointAnalysisInteractiveMenuStrip(iplus, rt);
		iplus.getWindow().add(menuStrip);
		menuStrip.interactionHandler.updateSize();
	}
}
