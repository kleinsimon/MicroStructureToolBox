
//=====================================================
//      Name:           LinearDistance
//      Project:        Measures the linear distance between binary inversions in x and y direction
//      Version:        0.1
//
//      Author:         Simon Klein, simon.klein@simonklein.de
//      Date:           24.11.2015
//      Comment:       

import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.math3.stat.descriptive.AggregateSummaryStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class LinearDistance implements PlugInFilter {
	static int step = 1;
	static String selectedStep = "0 degrees";
	static boolean doIterateAllImages = false;
	static boolean doCalculateStDev = true;
	static boolean doCalculateNum = true;
	static boolean doCalculateWhite = false;
	static boolean doCalculateBlack = false;
	static boolean doCalculateBlackAndWhite = false;
	static boolean doCalculateX = false;
	static boolean doCalculateY = false;
	static boolean doCalculateXAndY = true;
	static boolean doCalculateAll = false;
	// ResultsTable rt = ResultsTable.getResultsTable();
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

		gd.addCheckbox("Measure all opened Images", doIterateAllImages);
		gd.addMessage("Activate the results you want to gather");
		gd.addCheckbox("Standard Deviations  ", doCalculateStDev);
		gd.addCheckbox("Numbers  ", doCalculateStDev);
		gd.addCheckbox("White Phase  ", doCalculateWhite);
		gd.addCheckbox("Black Phase  ", doCalculateBlack);
		gd.addCheckbox("Both Phases  ", doCalculateBlackAndWhite);
		gd.addCheckbox("X Direction  ", doCalculateX);
		gd.addCheckbox("Y Direction  ", doCalculateY);
		gd.addCheckbox("Both Directions  ", doCalculateXAndY);
		gd.addCheckbox("Both Directions and both Phases ", doCalculateAll);

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		step = Math.max((int) gd.getNextNumber(), 1);
		selectedStep = gd.getNextChoice();
		doIterateAllImages = gd.getNextBoolean();
		doCalculateStDev = gd.getNextBoolean();
		doCalculateWhite = gd.getNextBoolean();
		doCalculateBlack = gd.getNextBoolean();
		doCalculateBlackAndWhite = gd.getNextBoolean();
		doCalculateX = gd.getNextBoolean();
		doCalculateY = gd.getNextBoolean();
		doCalculateXAndY = gd.getNextBoolean();
		doCalculateAll = gd.getNextBoolean();
		doCalculateNum = gd.getNextBoolean();
		return true;
	}

	public void run(ImageProcessor ip) {
		if (doIterateAllImages) {
			for (int id : ij.WindowManager.getIDList()) {
				analyzeImage(ij.WindowManager.getImage(id).getProcessor());
			}
		} else
			analyzeImage(ip);
	}

	public void doAnalyzeImage(int[][] pixels, int step, SummaryStatistics w, SummaryStatistics b) {
		int count = 0;

		Boolean now = null;
		Boolean last = null;

		for (int x = 0; x < pixels.length; x += step) {
			for (int y = 0; y < pixels[x].length; y++) {
				now = (pixels[x][y] == (255));
				if (now == last)
					count++;
				if ((now != last || y == pixels[x].length - 1) && count > 0) {
					if (last)
						w.addValue(count);
					else
						b.addValue(count);
					count = 0;
				}
				last = now;
				now = null;
			}
			last = null;
			count = 0;
		}
	}

	private void analyzeImage(ImageProcessor ip) {
		int[][] pixels = ip.getIntArray();
		int[][] pixelsRotate = new int[pixels[0].length][pixels.length];

		for (int row = 0; row < pixels.length; row++)
			for (int col = 0; col < pixels[row].length; col++)
				pixelsRotate[col][row] = pixels[row][col];

		SummaryStatistics wdy = new SummaryStatistics();
		SummaryStatistics bdy = new SummaryStatistics();
		SummaryStatistics wdx = new SummaryStatistics();
		SummaryStatistics bdx = new SummaryStatistics();

		doAnalyzeImage(pixels, step, wdy, bdy);
		doAnalyzeImage(pixelsRotate, step, wdx, bdx);

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

		rt.incrementCounter();
		int row = rt.getCounter() - 1;

		rt.setValue("Image", row, ij.IJ.getImage().getTitle());
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
		if (doCalculateBlackAndWhite && doCalculateY) {
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
		if (doCalculateBlackAndWhite && doCalculateX) {
			rt.setValue("Mean Dist. All x", row, bothx.getMean());
			if (doCalculateStDev)
				rt.setValue("st.Dev. All x", row, bothx.getStandardDeviation());
			if (doCalculateNum)
				rt.setValue("N All Stripes x", row, bothx.getN());
		}
		if (doCalculateWhite && doCalculateXAndY) {
			rt.setValue("Mean Dist. White x and y", row, wboth.getMean());
			if (doCalculateStDev)
				rt.setValue("st.Dev. White x and y", row, wboth.getStandardDeviation());
			if (doCalculateNum)
				rt.setValue("N White Stripes x and y", row, wboth.getN());
		}
		if (doCalculateBlack && doCalculateXAndY) {
			rt.setValue("Mean Dist. Black x and y", row, bboth.getMean());
			if (doCalculateStDev)
				rt.setValue("st.Dev. Black x and y", row, bboth.getStandardDeviation());
			if (doCalculateNum)
				rt.setValue("N Black Stripes x and y", row, bboth.getN());
		}
		if (doCalculateAll) {
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
