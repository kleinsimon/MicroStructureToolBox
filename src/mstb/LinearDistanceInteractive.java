package mstb;

//=====================================================
//      Name:           LinearDistance
//      Project:        Measures the linear distance between binary inversions in x and y direction
//      Version:        0.3.2
//      Author:         Simon Klein, simon.klein@simonklein.de
//      Date:           24.11.2015
//      Comment:		Buildfile taken from Patrick Pirrotte       

import java.awt.Component;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class LinearDistanceInteractive implements PlugInFilter {

	public LinearDistanceInteractiveSettings settings = new LinearDistanceInteractiveSettings();

	public int setup(String arg, ImagePlus imp) {
		if (imp != null && !showDialog())
			return DONE;
		return DOES_ALL;
	}

	boolean showDialog() {
		GenericDialog gd = new GenericDialog("Linear Distances by Simon Klein");
		gd.addMessage("Interactive Linear Distances Plugin, created by Simon Klein");
		gd.addMessage("This plug-in allows the interactive measurement of linear distances in X or Y direction.");
		gd.addNumericField("Step distance between measures in pixels/units", settings.step, 1);
		gd.addCheckbox("Step distance in units", settings.doCalibrateStep);
		gd.addCheckbox("Center lines between borders", settings.doCenterLines);
		gd.addNumericField("Minimum margin left and right in pixels/units", settings.offset, 1);
		gd.addCheckbox("Offset distance in units", settings.doCalibrateOffset);
		gd.addChoice("Direction", settings.directions, ((settings.directionY) ? settings.directions[1] : settings.directions[0]));
		gd.addChoice("Overlay color", ij.plugin.Colors.colors, "Red");

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		settings.step = Math.max(gd.getNextNumber(), 1d);
		settings.doCalibrateStep = gd.getNextBoolean();
		settings.doCenterLines = gd.getNextBoolean();
		settings.offset = Math.max(gd.getNextNumber(), 1d);
		settings.doCalibrateOffset = gd.getNextBoolean();
		settings.directionY = gd.getNextChoice() == settings.directions[1];
		settings.overlayColor = gd.getNextChoice();

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

		LinearDistanceInteractiveMenuStrip menuStrip = new LinearDistanceInteractiveMenuStrip(iplus, settings);
		iplus.getWindow().add(menuStrip);
		menuStrip.interactionHandler.updateSize();
	}
}
