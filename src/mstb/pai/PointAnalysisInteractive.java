package mstb.pai;

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
import mstb.ExclusiveOverlayMenuStrip;

public class PointAnalysisInteractive implements PlugInFilter {
	public PointAnalysisInteractiveSettings settings = new PointAnalysisInteractiveSettings();
	
	public int setup(String arg, ImagePlus imp) {
		if (imp != null && !showDialog())
			return DONE;
		return DOES_ALL;
	}

	boolean showDialog() {
		GenericDialog gd = new GenericDialog("Linear Distances by Simon Klein");
		gd.addMessage("Point Domain Analysis Plugin, created by Simon Klein");
		gd.addMessage("This plug-in allows the interactive assignment of points to a given number of domains.");
		gd.addNumericField("Number of points X / Total number of points if randomized", settings.pointsX, 0);
		gd.addNumericField("Number of points Y", settings.pointsY, 0);
		gd.addNumericField("Number of domains", settings.numDomains, 0);
		gd.addCheckbox("Randomize Points", settings.randomizePoints);
		gd.addChoice("Overlay color", ij.plugin.Colors.colors, "Red");

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		settings.pointsX = (int) Math.max(gd.getNextNumber(), 1d);
		settings.pointsY = (int) Math.max(gd.getNextNumber(), 1d);
		settings.numDomains = (int) Math.max(gd.getNextNumber(), 2d);
		settings.randomizePoints = gd.getNextBoolean();
		settings.overlayColor = gd.getNextChoice();
		
		settings.restable = new ResultsTable();

		settings.save();
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
			if (comp instanceof ExclusiveOverlayMenuStrip) {
				if (!((ExclusiveOverlayMenuStrip) comp).remove())
					return;
			}
		}


		PointAnalysisInteractiveMenuStrip menuStrip = new PointAnalysisInteractiveMenuStrip(iplus, settings);
		iplus.getWindow().add(menuStrip);
		menuStrip.interactionHandler.updateSize();
	}
}
