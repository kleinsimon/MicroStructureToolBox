
//=====================================================
//      Name:           LinearDistance
//      Project:        Measures the linear distance between binary inversions in x and y direction
//      Version:        0.1
//
//      Author:         Simon Klein, simon.klein@simonklein.de
//      Date:           24.11.2015
//      Comment:       

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.math3.stat.descriptive.AggregateSummaryStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
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
	static int step = Prefs.getInt("LinearDistance.stepSize", 1);
	static boolean doApplyCalibration = Prefs.getBoolean("LinearDistance.doApplyCalibration", true);
	static boolean doIterateAllImages = Prefs.getBoolean("LinearDistance.doIterateAllImages", true);
	static boolean doExcludeEdges = Prefs.getBoolean("LinearDistance.doExcludeEdges", true);
	static boolean doShowOverlay = Prefs.getBoolean("LinearDistance.doShowOverlay", true);
	static boolean doCalculateStDev = Prefs.getBoolean("LinearDistance.doCalculateStDev", true);
	static boolean doCalculateNum = Prefs.getBoolean("LinearDistance.doCalculateNum", true);
	static boolean doCalculateWhite = Prefs.getBoolean("LinearDistance.doCalculateWhite", true);
	static boolean doCalculateBlack = Prefs.getBoolean("LinearDistance.doCalculateBlack", true);
	static boolean doCalculateBlackAndWhite = Prefs.getBoolean("LinearDistance.doCalculateBlackAndWhite", true);
	static boolean doCalculateX = Prefs.getBoolean("LinearDistance.doCalculateX", true);
	static boolean doCalculateY = Prefs.getBoolean("LinearDistance.doCalculateY", true);
	static boolean doCalculateXAndY = Prefs.getBoolean("LinearDistance.doCalculateXAndY", true);
	static boolean doCalculateAll = Prefs.getBoolean("LinearDistance.doCalculateAll", true);

	ResultsTable rt = new ResultsTable();

	public int setup(String arg, ImagePlus imp) {
		if (imp != null && !showDialog())
			return DONE;
		rt.reset();
		return DOES_8G + DOES_STACKS + SUPPORTS_MASKING;
	}

	boolean showDialog() {
		GenericDialog gd = new GenericDialog("Linear Distances by Simon Klein");
		gd.addMessage("Linear Distances Plugin, created by Simon Klein");
		gd.addMessage("This plug-in calculates the linear distances in x and y directions for binary images.");
		gd.addMessage("All values are measured in pixels.");

		gd.addNumericField("Distance between measures in pixels", step, 1);
		gd.addCheckbox("Apply image calbration", doApplyCalibration);
		gd.addCheckbox("Measure all opened Images", doIterateAllImages);
		gd.addCheckbox("Exclude stripes cut by Edges", doExcludeEdges);
		gd.addCheckbox("Show measured pixels as overlay", doShowOverlay);

		gd.addMessage("Activate the results you want to gather");

		gd.addCheckbox("Standard Deviations  ", doCalculateStDev);
		gd.addCheckbox("Numbers  ", doCalculateNum);
		gd.addCheckbox("White Phase  ", doCalculateWhite);
		gd.addCheckbox("Black Phase  ", doCalculateBlack);
		// gd.addCheckbox("Both Phases ", doCalculateBlackAndWhite);
		gd.addCheckbox("X Direction  ", doCalculateX);
		gd.addCheckbox("Y Direction  ", doCalculateY);
		// gd.addCheckbox("Both Directions ", doCalculateXAndY);
		// gd.addCheckbox("Both Directions and both Phases ", doCalculateAll);

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		step = Math.max((int) gd.getNextNumber(), 1);
		Prefs.set("LinearDistance.stepSize", step);
		doApplyCalibration = gd.getNextBoolean();
		Prefs.set("LinearDistance.doApplyScale", doApplyCalibration);
		doIterateAllImages = gd.getNextBoolean();
		Prefs.set("LinearDistance.doIterateAllImages", doIterateAllImages);
		doExcludeEdges = gd.getNextBoolean();
		Prefs.set("LinearDistance.doExcludeEdges", doExcludeEdges);
		doShowOverlay = gd.getNextBoolean();
		Prefs.set("LinearDistance.doShowOverlay", doShowOverlay);
		doCalculateStDev = gd.getNextBoolean();
		Prefs.set("LinearDistance.doCalculateStDev", doCalculateStDev);
		doCalculateNum = gd.getNextBoolean();
		Prefs.set("LinearDistance.doCalculateNum", doCalculateNum);
		doCalculateWhite = gd.getNextBoolean();
		Prefs.set("LinearDistance.doCalculateWhite", doCalculateWhite);
		doCalculateBlack = gd.getNextBoolean();
		Prefs.set("LinearDistance.doCalculateBlack", doCalculateBlack);
		// doCalculateBlackAndWhite = gd.getNextBoolean();
		// Prefs.set("LinearDistance.doCalculateBlackAndWhite",
		// doCalculateBlackAndWhite);
		doCalculateX = gd.getNextBoolean();
		Prefs.set("LinearDistance.doCalculateX", doCalculateX);
		doCalculateY = gd.getNextBoolean();
		Prefs.set("LinearDistance.doCalculateY", doCalculateY);
		// doCalculateXAndY = gd.getNextBoolean();
		// Prefs.set("LinearDistance.doCalculateXAndY", doCalculateXAndY);
		// doCalculateAll = gd.getNextBoolean();
		// Prefs.set("LinearDistance.doCalculateAll", doCalculateAll);

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

	public void doAnalyzeImage(int[][] pixels, Boolean goX, int step, SummaryStatistics w, SummaryStatistics b,
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
							w.addValue((double) (count + 1) * calib);
						else
							b.addValue((double) (count + 1) * calib);
						if (doShowOverlay && overlay != null) {
							if ((doCalculateWhite && last) || (doCalculateBlack && !last)) {
								for (int yi = 0; yi <= count; yi++) {
									int ny = Math.max(0, y - yi - 1);
									if (goX)
										overlay.set(ny, x, color);
									else
										overlay.set(x, ny, color);
								}
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

		SummaryStatistics wdy = new SummaryStatistics();
		SummaryStatistics bdy = new SummaryStatistics();
		SummaryStatistics wdx = new SummaryStatistics();
		SummaryStatistics bdx = new SummaryStatistics();

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

		if (doCalculateY) {
			doAnalyzeImage(pixels, false, step, wdy, bdy, oiy, calx);
		}
		if (doCalculateX) {
			doAnalyzeImage(pixelsRotate, true, step, wdx, bdx, oix, caly);
		}

		Collection<SummaryStatistics> colbothy = new ArrayList<SummaryStatistics>();
		Collection<SummaryStatistics> colbothx = new ArrayList<SummaryStatistics>();
		Collection<SummaryStatistics> colall = new ArrayList<SummaryStatistics>();
		Collection<SummaryStatistics> colwboth = new ArrayList<SummaryStatistics>();
		Collection<SummaryStatistics> colbboth = new ArrayList<SummaryStatistics>();
		colbothy.add(wdy);
		colbothy.add(bdy);
		colbothx.add(wdx);
		colbothx.add(bdx);
		colwboth.add(wdy);
		colwboth.add(wdx);
		colbboth.add(bdy);
		colbboth.add(bdx);
		colall.add(wdy);
		colall.add(bdy);
		colall.add(wdx);
		colall.add(bdx);
		StatisticalSummaryValues bothy = AggregateSummaryStatistics.aggregate(colbothy);
		StatisticalSummaryValues bothx = AggregateSummaryStatistics.aggregate(colbothx);
		StatisticalSummaryValues wboth = AggregateSummaryStatistics.aggregate(colwboth);
		StatisticalSummaryValues bboth = AggregateSummaryStatistics.aggregate(colbboth);
		StatisticalSummaryValues all = AggregateSummaryStatistics.aggregate(colall);

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
		if (doCalculateWhite && doCalculateY) {
			rt.setValue("Mean Dist. White y", row, wdy.getMean());
			if (doCalculateStDev)
				rt.setValue("st.Dev. White y", row, wdy.getStandardDeviation());
			if (doCalculateNum)
				rt.setValue("N White Stripes y", row, wdy.getN());
		}
		if (doCalculateBlack && doCalculateY) {
			rt.setValue("Mean Dist. Black y", row, bdy.getMean());
			if (doCalculateStDev)
				rt.setValue("st.Dev. Black y", row, bdy.getStandardDeviation());
			if (doCalculateNum)
				rt.setValue("N Black Stripes y", row, bdy.getN());
		}
		if (doCalculateBlack && doCalculateWhite && doCalculateY) {
			rt.setValue("Mean Dist. All y", row, bothy.getMean());
			rt.setValue("st.Dev. All y", row, bothy.getStandardDeviation());
			if (doCalculateNum)
				rt.setValue("N All Stripes y", row, bothy.getN());
		}
		if (doCalculateWhite && doCalculateX) {
			rt.setValue("Mean Dist. White x", row, wdx.getMean());
			if (doCalculateStDev)
				rt.setValue("st.Dev. White x", row, wdx.getStandardDeviation());
			if (doCalculateNum)
				rt.setValue("N White Stripes x", row, wdx.getN());
		}
		if (doCalculateBlack && doCalculateX) {
			rt.setValue("Mean Dist. Black x", row, bdx.getMean());
			if (doCalculateStDev)
				rt.setValue("st.Dev. Black x", row, bdx.getStandardDeviation());
			if (doCalculateNum)
				rt.setValue("N Black Stripes x", row, bdx.getN());
		}
		if (doCalculateBlack && doCalculateWhite && doCalculateX) {
			rt.setValue("Mean Dist. All x", row, bothx.getMean());
			if (doCalculateStDev)
				rt.setValue("st.Dev. All x", row, bothx.getStandardDeviation());
			if (doCalculateNum)
				rt.setValue("N All Stripes x", row, bothx.getN());
		}
		if (doCalculateWhite && doCalculateX && doCalculateX) {
			rt.setValue("Mean Dist. White x and y", row, wboth.getMean());
			if (doCalculateStDev)
				rt.setValue("st.Dev. White x and y", row, wboth.getStandardDeviation());
			if (doCalculateNum)
				rt.setValue("N White Stripes x and y", row, wboth.getN());
		}
		if (doCalculateBlack && doCalculateX && doCalculateX) {
			rt.setValue("Mean Dist. Black x and y", row, bboth.getMean());
			if (doCalculateStDev)
				rt.setValue("st.Dev. Black x and y", row, bboth.getStandardDeviation());
			if (doCalculateNum)
				rt.setValue("N Black Stripes x and y", row, bboth.getN());
		}
		if (doCalculateBlack && doCalculateWhite && doCalculateX && doCalculateX) {
			rt.setValue("Mean Dist. All x and y", row, all.getMean());
			if (doCalculateStDev)
				rt.setValue("st.Dev. All x and y", row, all.getStandardDeviation());
			if (doCalculateNum)
				rt.setValue("N All Stripes x and y", row, all.getN());
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
