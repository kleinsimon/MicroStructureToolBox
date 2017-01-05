import java.awt.Color;
import java.awt.Point;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class LinearDistanceInteractiveHandler {
	private Hashtable<Integer, ArrayList<Integer>> markList = new Hashtable<Integer, ArrayList<Integer>>();

	private Double step = 50.d;
	private Double offset = 25.d;

	private int markLength = 5;
	private boolean doCalibrateStep, doCalibrateOffset, doCenterLines, doApplyCalibration;
	private boolean directionY = false;
	private boolean[] doResults;
	
	private ImagePlus iplus = null;
	private ImageCanvas icanv = null;
	private Integer remtol = 10;
	//private Integer menuHeight = 16;
	private Integer numMarks = 0;
	private LinearDistanceInteractiveMenuStrip menuStrip;
	ImageProcessor ip = null;
	private String[] resultsTable;
	public LinearDistanceInteractiveMouseHandler mouseActionListener;
	private ResultsTable rt;
	Overlay ovl;
	private Color ovlColor;

	public LinearDistanceInteractiveHandler(ImagePlus image, String[] resTable, ResultsTable restable, LinearDistanceInteractiveMenuStrip parentStrip) {

		iplus = image;
		ip = iplus.getProcessor();
		icanv = iplus.getCanvas();
		resultsTable = resTable;
		rt = restable;
		menuStrip = parentStrip;

		mouseActionListener = new LinearDistanceInteractiveMouseHandler(this);

		ImageCanvas icanv = iplus.getCanvas();
		icanv.addMouseMotionListener(mouseActionListener);
		icanv.addMouseListener(mouseActionListener);

		ij.IJ.setTool(12);
		icanv.disablePopupMenu(true);
		iplus.draw();
				
		step = Prefs.get("LinearDistanceInteractive.stepSize", 50);
		offset = Prefs.get("LinearDistanceInteractive.offset", 25);
		markLength = Tools.getRoundedInt(Prefs.get("LinearDistanceInteractive.markLength", 5));
		doCalibrateStep = Prefs.get("LinearDistanceInteractive.doCalibrateStep", false);
		doCalibrateOffset = Prefs.get("LinearDistanceInteractive.doCalibrateOffset", false);
		doCenterLines = Prefs.get("LinearDistanceInteractive.doCenterLines", true);
		directionY = Prefs.get("LinearDistanceInteractive.directionY", false);
		setColor(Prefs.get("LinearDistanceInteractive.overlayColor", "Red"));
		
		Calibration cal = iplus.getCalibration();

		if (doCalibrateStep) {
			step = (Double) step / ((directionY) ? cal.pixelWidth : cal.pixelHeight);
		}
		
		if (doCalibrateOffset) {
			offset = (Double) offset / ((directionY) ? cal.pixelWidth : cal.pixelHeight);
		}
		
		drawOverlay();
	}

	public void updateSize() {
		iplus.getWindow().validate();
		icanv.zoomIn(0, 0);
		icanv.zoomOut(0, 0);
	}

	public Point getRealPos() {
		return icanv.getCursorLoc();
	}

	public void remove() {
		iplus.setOverlay(null);
		iplus.updateAndDraw();
		icanv.removeMouseListener(mouseActionListener);
		icanv.removeMouseMotionListener(mouseActionListener);
		icanv.disablePopupMenu(false);
	}

	public void setColor(String overlayColor) {
		try {
		    Field field = Color.class.getField(overlayColor.toUpperCase());
		    ovlColor = (Color)field.get(null);
		} catch (Exception e) {
			ovlColor = Color.RED;
		}
	}
	
	public boolean askResults() {
		doResults = Tools.StringToBoolean(Prefs.get("LinearDistanceInteractive.doResults", ""), resultsTable.length);
		doApplyCalibration = Prefs.get("LinearDistanceInteractive.doApplyCalibration", true);
		
		GenericDialog gd = new GenericDialog("Select results");
		gd.addMessage("Select the results you want to obtain.");
		gd.addCheckboxGroup(1, 6, resultsTable, doResults);
		gd.addCheckbox("Apply image calbration", doApplyCalibration);

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		for (int i = 0; i < doResults.length; i++) {
			doResults[i] = gd.getNextBoolean();
		}
		doApplyCalibration = gd.getNextBoolean();

		Prefs.set("LinearDistanceInteractive.doResults", Tools.BooleanToString(doResults));
		Prefs.set("LinearDistanceInteractive.doApplyScale", doApplyCalibration);
		Prefs.savePreferences();
		return true;
	}

	public void analyze() {
		if (!askResults())
			return;

		List<Double> stripes = new ArrayList<Double>();
		for (Entry<Integer, ArrayList<Integer>> e : markList.entrySet()) {
			Integer lastMark = 0;
			ArrayList<Integer> marks = e.getValue();
			Collections.sort(marks);
			for (Integer mark : marks) {
				if (lastMark == 0) {
					lastMark = mark;
					continue;
				}
				Integer diff = mark - lastMark;
				stripes.add(diff.doubleValue());
			}
		}

		@SuppressWarnings("unchecked")
		Stat res = new Stat(stripes);
		rt.incrementCounter();
		int row = rt.getCounter() - 1;
		String unit = "px";
		Calibration cal = iplus.getCalibration();

		double calval = 1d;
		if (doApplyCalibration) {
			calval = (directionY) ? cal.pixelHeight : cal.pixelWidth;
			unit = iplus.getCalibration().getUnit();
		}

		rt.setValue("Image", row, iplus.getTitle() + ((directionY) ? " V" : " H"));

		for (int ri = 0; ri < doResults.length; ri++) {
			if (!doResults[ri])
				continue;
			String rName = resultsTable[ri];
			String cName = String.format("%s [%s] %s", "Stripe length", unit, rName);
			double rValue = res.getFormattedValue(ri) * calval;
			rt.setValue(cName, row, rValue);
		}

		rt.show("Linear Distances Results");
	}

	public void clear() {
		markList = new Hashtable<Integer, ArrayList<Integer>>();
		numMarks = 0;
		iplus.updateAndDraw();
	}

	public void addPoint() {
		Point cursorPos = getRealPos();
		int line = getNextLine(cursorPos);

		if (markList.get(line) == null) {
			markList.put(line, new ArrayList<Integer>());
		}

		if (directionY)
			markList.get(line).add(cursorPos.y);
		else
			markList.get(line).add(cursorPos.x);
		drawOverlay();
		numMarks++;
		menuStrip.setCounts(numMarks);
	}

	public void removePoint() {
		Point cursorPos = getRealPos();

		int line = getNextLine(cursorPos);

		if (markList.get(line) == null)
			return;

		Integer pos = (directionY) ? cursorPos.y : cursorPos.x;
		Integer dist = Integer.MAX_VALUE;
		Integer found = null;

		for (Integer mark : markList.get(line)) {
			int dt = Math.abs(mark - pos);
			if (dt < dist) {
				dist = dt;
				found = mark;
			}
		}
		if (found != null && dist <= remtol) {
			markList.get(line).remove((Object) found);
			drawOverlay();
		}
		numMarks--;
		menuStrip.setCounts(numMarks);
	}

	public void drawOverlay() {
		//overlay.copyBits(ip, 0, 0, Blitter.COPY);
		ImageProcessor overlay = new ColorProcessor(ip.getWidth(), ip.getHeight());
		//overlay = (ImageProcessor) ip.clone();
		overlay.setColor(ovlColor);
		int pxlh = (directionY) ? overlay.getWidth() : overlay.getHeight();
		int pxlw = (!directionY) ? overlay.getWidth() : overlay.getHeight();
		int line = 0;
		int nearLine = 0;
		Point cursorPos = getRealPos();
		
		if (cursorPos != null)
			nearLine = getNextLine(cursorPos);
		
		double offsetLeft = offset; 
		
		if (doCenterLines){
			Double numlines = (((double) pxlh - 2.0 * offset ) / step);
			double nl = Math.floor(numlines);
			offsetLeft = (pxlh - nl * step) / 2.0;
			//offsetLeft = 1.0;
		}

		for (double ld = offsetLeft; ld <= pxlh-offset; ld += step) {
			int l = Tools.getRoundedInt(ld);
			if (directionY)
				overlay.drawLine(l, 0, l, pxlw);
			else
				overlay.drawLine(0, l, pxlw, l);

			if (markList.get(line) != null) {
				for (Integer markPos : markList.get(line)) {
					if (directionY)
						overlay.drawLine(l - markLength, markPos, l + markLength, markPos);
					else
						overlay.drawLine(markPos, l - markLength, markPos, l + markLength);
				}
			}
			if (line == nearLine && cursorPos != null) {
				if (directionY)
					overlay.drawLine(l - markLength, cursorPos.y, l + markLength, cursorPos.y);
				else
					overlay.drawLine(cursorPos.x, l - markLength, cursorPos.x, l + markLength);
			}
			line++;
		}
		
		ImageRoi roi = new ImageRoi(0, 0, overlay);
		//roi.setName(iplus.getShortTitle() + " measured stripes");
		//roi.setOpacity(1d);
		roi.setZeroTransparent(true);

		//ovl = new Overlay(roi);
		iplus.setOverlay(roi, Color.red, 0, Color.red);
		// iplus.setRoi(roi);
		// icanv.setCursor(Cursor.CURSOR_NONE);
		iplus.draw();
	}

	public int getNextLine(Point Cursor) {
		if (Cursor == null)
			throw new NullPointerException("No Point given");
		if (directionY)
			return Tools.getRoundedInt((((double) Cursor.x) - offset) /  step);
		else
			return Tools.getRoundedInt((((double) Cursor.y) - offset) /  step);
	}
}
