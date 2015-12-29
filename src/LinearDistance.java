
//=====================================================
//      Name:           LinearDistance
//      Project:        Measures the linear distance between binary inversions in x and y direction
//      Version:        0.1
//      Author:         Simon Klein, simon.klein@simonklein.de
//      Date:           24.11.2015
//      Comment:		Buildfile taken from Patrick Pirrotte       

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
	static int step = Prefs.getInt("LinearDistance.stepSize", 1);
	static boolean doApplyCalibration = Prefs.getBoolean("LinearDistance.doApplyCalibration", true);
	static boolean doCalibrateStep = Prefs.getBoolean("LinearDistance.doCalibrateStep", false);
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
		gd.addCheckbox("Apply image calbration", doApplyCalibration);
		gd.addNumericField("Step distance between measures in pixels/units", step, 1);
		gd.addCheckbox("Step distance in units", doCalibrateStep);
		gd.addCheckbox("Measure all opened Images", doIterateAllImages);
		gd.addCheckbox("Exclude stripes cut by Edges", doExcludeEdges);
		gd.addCheckbox("Show measured pixels as overlay", doShowOverlay);
		gd.addMessage("Activate the results you want to gather");
		gd.addCheckbox("Standard Deviations  ", doCalculateStDev);
		gd.addCheckbox("Numbers  ", doCalculateNum);
		gd.addCheckbox("White Phase  ", doCalculateWhite);
		gd.addCheckbox("Black Phase  ", doCalculateBlack);
		gd.addCheckbox("X Direction  ", doCalculateX);
		gd.addCheckbox("Y Direction  ", doCalculateY);

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		doApplyCalibration = gd.getNextBoolean();
		Prefs.set("LinearDistance.doApplyScale", doApplyCalibration);
		step = Math.max((int) gd.getNextNumber(), 1);
		Prefs.set("LinearDistance.stepSize", step);
		doCalibrateStep = gd.getNextBoolean();
		Prefs.set("LinearDistance.doCalibrateStep", doCalibrateStep);
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
		doCalculateX = gd.getNextBoolean();
		Prefs.set("LinearDistance.doCalculateX", doCalculateX);
		doCalculateY = gd.getNextBoolean();
		Prefs.set("LinearDistance.doCalculateY", doCalculateY);

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
		long lineDistanceX = step;
		long lineDistanceY = step;
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
			lineDistanceX = Math.round((double) step / calx);
			lineDistanceY = Math.round((double) step / caly);
		}

		if (doCalculateY) {
			doAnalyzeImage(pixels, false, lineDistanceX, wdy, bdy, oiy, calx);
		}
		if (doCalculateX) {
			doAnalyzeImage(pixelsRotate, true, lineDistanceY, wdx, bdx, oix, caly);
		}

		Stat bothy = new Stat(wdy, bdy);
		Stat bothx = new Stat(wdx, bdx);
		Stat all = new Stat(wdy, bdy, wdx, bdx);
		Stat wboth = new Stat(wdy, wdx);
		Stat bboth = new Stat(bdy, bdx);
		Stat wy = new Stat(wdy);
		Stat wx = new Stat(wdx);
		Stat by = new Stat(bdy);
		Stat bx = new Stat(bdx);

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
			rt.setValue("Mean Dist. White y", row, wy.getMean());
			if (doCalculateStDev)
				rt.setValue("st.Dev. White y", row, wy.getStDev());
			if (doCalculateNum)
				rt.setValue("N White Stripes y", row, wy.getN());
		}
		if (doCalculateBlack && doCalculateY) {
			rt.setValue("Mean Dist. Black y", row, by.getMean());
			if (doCalculateStDev)
				rt.setValue("st.Dev. Black y", row, by.getStDev());
			if (doCalculateNum)
				rt.setValue("N Black Stripes y", row, by.getN());
		}
		if (doCalculateBlack && doCalculateWhite && doCalculateY) {
			rt.setValue("Mean Dist. All y", row, bothy.getMean());
			if (doCalculateStDev)
				rt.setValue("st.Dev. All y", row, bothy.getStDev());
			if (doCalculateNum)
				rt.setValue("N All Stripes y", row, bothy.getN());
		}
		if (doCalculateWhite && doCalculateX) {
			rt.setValue("Mean Dist. White x", row, wx.getMean());
			if (doCalculateStDev)
				rt.setValue("st.Dev. White x", row, wx.getStDev());
			if (doCalculateNum)
				rt.setValue("N White Stripes x", row, wx.getN());
		}
		if (doCalculateBlack && doCalculateX) {
			rt.setValue("Mean Dist. Black x", row, bx.getMean());
			if (doCalculateStDev)
				rt.setValue("st.Dev. Black x", row, bx.getStDev());
			if (doCalculateNum)
				rt.setValue("N Black Stripes x", row, bx.getN());
		}
		if (doCalculateBlack && doCalculateWhite && doCalculateX) {
			rt.setValue("Mean Dist. All x", row, bothx.getMean());
			if (doCalculateStDev)
				rt.setValue("st.Dev. All x", row, bothx.getStDev());
			if (doCalculateNum)
				rt.setValue("N All Stripes x", row, bothx.getN());
		}
		if (doCalculateWhite && doCalculateX && doCalculateX) {
			rt.setValue("Mean Dist. White x and y", row, wboth.getMean());
			if (doCalculateStDev)
				rt.setValue("st.Dev. White x and y", row, wboth.getStDev());
			if (doCalculateNum)
				rt.setValue("N White Stripes x and y", row, wboth.getN());
		}
		if (doCalculateBlack && doCalculateX && doCalculateX) {
			rt.setValue("Mean Dist. Black x and y", row, bboth.getMean());
			if (doCalculateStDev)
				rt.setValue("st.Dev. Black x and y", row, bboth.getStDev());
			if (doCalculateNum)
				rt.setValue("N Black Stripes x and y", row, bboth.getN());
		}
		if (doCalculateBlack && doCalculateWhite && doCalculateX && doCalculateX) {
			rt.setValue("Mean Dist. All x and y", row, all.getMean());
			if (doCalculateStDev)
				rt.setValue("st.Dev. All x and y", row, all.getStDev());
			if (doCalculateNum)
				rt.setValue("N All Stripes x and y", row, all.getN());
		}

		rt.show("Linear Distances Results");
	}

	public int getRoundedInt(Double from) {
		Long rnd = Math.round(from);
		return Math.toIntExact(rnd);
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

	class Stat {
		private Double mean = null;
		private Double sum = null;
		private Double stD = null;
		private Long num = null;
		private List<Collection<Double>> values = new ArrayList<Collection<Double>>();

		public Stat() {

		}

		public Stat(List<Double>... lists) {
			addList(lists);
		}

		public void invalidate() {
			mean = stD = sum = null;
			num = null;
		}

		public void addList(Collection<Double>... lists) {
			for (Collection<Double> l : lists)
				values.add(l);
			invalidate();
		}

		public void clearLists() {
			values.clear();
			invalidate();
		}

		public void removeList(Collection<Double>... lists) {
			for (Collection<Double> l : lists)
				values.remove(l);
			invalidate();
		}

		public long getN() {
			if (num == null) {
				num = 0l;
				for (Collection<Double> lst : values) {
					if (lst.isEmpty())
						continue;
					num += lst.size();
				}
			}

			return num;
		}

		public double getSum() {
			if (sum == null) {
				sum = 0d;
				for (Collection<Double> lst : values) {
					if (lst.isEmpty())
						continue;
					for (double n : lst) {
						sum += n;
					}
				}
			}

			return sum;
		}

		public double getMean() {
			if (mean == null) {
				sum = getSum();
				num = getN();
				mean = sum / (double) num;
			}

			return mean;
		}

		public double getStDev() {
			if (stD == null) {
				mean = getMean();
				num = getN();
				Double dst = 0d;
				for (Collection<Double> lst : values) {
					if (lst.isEmpty())
						continue;
					for (double x : lst) {
						dst += Math.pow((x - mean), 2);
					}
				}
				stD = Math.sqrt(dst / (double) num);
			}
			return stD;
		}
	}
}
