package mstb.ld;

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
import mstb.Stat;
import mstb.Tools;

public class LinearDistance implements PlugInFilter {
	private LinearDistanceSettings settings = new LinearDistanceSettings();
	
	public int setup(String arg, ImagePlus imp) {
		if (imp != null && !showDialog())
			return DONE;
		return DOES_8G;
	}

	boolean showDialog() {
		settings.load();

		GenericDialog gd = new GenericDialog("Linear Distances by Simon Klein");
		gd.addMessage("Linear Distances Plugin, created by Simon Klein");

		gd.addMessage("This plug-in measures the linear distances in x and y directions for binary images.");
		gd.addCheckbox("Apply image calbration", settings.doApplyCalibration);
		gd.addNumericField("Step distance between measures in pixels/units", settings.step, 1);
		gd.addCheckbox("Step distance in units", settings.doCalibrateStep);
		gd.addCheckbox("Measure all opened Images", settings.doIterateAllImages);
		//gd.addCheckbox("Aggregate measurements of all images", settings.doAggregate);
		gd.addCheckbox("Exclude stripes cut by Edges", settings.doExcludeEdges);
		gd.addCheckbox("Show measured pixels as overlay", settings.doShowOverlay);
		

		gd.addMessage("Measurements");
		gd.addCheckboxGroup(2, 5, settings.measurementsTable, settings.doMeasurements);
		gd.addMessage("Results");
		gd.addCheckboxGroup(1, 6, settings.resultsTable, settings.doResults);

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		settings.doApplyCalibration = gd.getNextBoolean();
		settings.step = Math.max(gd.getNextNumber(), 1d);
		settings.doCalibrateStep = gd.getNextBoolean();
		settings.doIterateAllImages = gd.getNextBoolean();
		settings.doExcludeEdges = gd.getNextBoolean();
		settings.doShowOverlay = gd.getNextBoolean();
		for (int i = 0; i < settings.doMeasurements.length; i++) {
			settings.doMeasurements[i] = gd.getNextBoolean();
		}
		for (int i = 0; i < settings.doResults.length; i++) {
			settings.doResults[i] = gd.getNextBoolean();
		}

		settings.save();
		
		return true;
	}

	public void run(ImageProcessor ip) {
		LinearDistanceResults res = null;
		
		if (settings.doIterateAllImages) {
			for (int id : ij.WindowManager.getIDList()) {
				if (settings.doAggregate) {
					res = new LinearDistanceResults();
					analyzeImage(ij.WindowManager.getImage(id), res);
				} else{
					res = new LinearDistanceResults();
					analyzeImage(ij.IJ.getImage(), res);
					showResult(res);
				}
			}
			if (settings.doAggregate)
				showResult(res);
		} else {
			res = new LinearDistanceResults();
			analyzeImage(ij.IJ.getImage(), res);
			showResult(res);
		}
	}
	
	public void showMessage(String message) {
		new ij.gui.MessageDialog(ij.WindowManager.getCurrentWindow(), "", message);
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
					if (!(settings.doExcludeEdges && onEdge)) {
						if (last)
							w.add((double) (count + 1) * calib);
						else
							b.add((double) (count + 1) * calib);
						if (settings.doShowOverlay && overlay != null) {
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

	@SuppressWarnings("unchecked")
	private void analyzeImage(ImagePlus iplus, LinearDistanceResults res) {
		
		ImageProcessor ip = iplus.getProcessor();
		ImageProcessor oix = null;
		ImageProcessor oiy = null;
		long lineDistanceX = settings.step.intValue();
		long lineDistanceY = settings.step.intValue();
		
		if (settings.doShowOverlay) {
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
		if (settings.doApplyCalibration && cal.scaled()) {
			calx = cal.pixelWidth;
			caly = cal.pixelHeight;

			unit = cal.getXUnit();
			if (unit != cal.getYUnit()) {
				unit += "/" + cal.getYUnit();
			}
		}

		if (settings.doCalibrateStep) {
			lineDistanceX = Math.round((Double) settings.step / calx);
			lineDistanceY = Math.round((Double) settings.step / caly);
		}

		doAnalyzeImage(pixels, false, lineDistanceX, wdy, bdy, oiy, caly);
		doAnalyzeImage(pixelsRotate, true, lineDistanceY, wdx, bdx, oix, calx);
		
		res.wdx.addList(wdx); 
		res.wdy.addList(wdy);
		res.wdxwdy.addList(wdx, wdy);
		res.bdx.addList(bdx);
		res.bdy.addList(bdy);
		res.bdxbdy.addList(bdx, bdy);
		res.wdxbdx.addList(wdx, bdx);
		res.wdybdy.addList(wdy, bdy);
		res.wdxbdy.addList(wdx, bdy);
		
		res.calStr = calx.toString() + ((!calx.equals(caly)) ? "/" + caly.toString() : "");
		res.title = iplus.getTitle();
		res.unit=unit;
		
		if (settings.doShowOverlay) {
			oix.copyBits(oiy, 0, 0, Blitter.ADD);
			ImageRoi roi = new ImageRoi(0, 0, oix);
			roi.setName(iplus.getShortTitle() + " measured stripes");
			roi.setOpacity(0.5d);
			iplus.deleteRoi();
			iplus.setRoi(roi, true);
		}
	}
	
	private void showResult(LinearDistanceResults res) {

		ResultsTable rt = settings.restable;
		
		rt.incrementCounter();
		int row = rt.getCounter() - 1;

		rt.setValue("Image", row, res.title);
		if (res.unit != null)
			rt.setValue("Unit", row, res.unit);
		
		if (settings.doApplyCalibration && res.calStr != null) 
			rt.setValue("Calibration X/Y", row, res.calStr);
		
		String rName, mName, cName;
		Double rValue;
		Stat[] Stats = res.getAll();

		for (int mi = 0; mi < settings.doMeasurements.length; mi++) {
			if (!settings.doMeasurements[mi])
				continue;
			mName = settings.measurementsTable[mi];
			for (int ri = 0; ri < settings.doResults.length; ri++) {
				if (!settings.doResults[ri])
					continue;
				rName = settings.resultsTable[ri];
				cName = String.format("%s %s", mName, rName);
				rValue = Stats[mi].getFormattedValue(ri);
				rt.setValue(cName, row, rValue);
			}
		}

		rt.show("Linear Distances Results");
	}

	public void showData(String name, double value, double scaledValue) {
		
		ResultsTable rt = settings.restable;
		
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
