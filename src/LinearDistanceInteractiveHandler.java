import java.awt.Point;
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
	
	private ImagePlus iplus = null;
	private ImageCanvas icanv = null;
	
	//private Integer menuHeight = 16;
	private Integer numMarks = 0;
	ImageProcessor ip = null;
	private LinearDistanceInteractiveMenuStrip menuStrip;
	private LinearDistanceInteractiveMouseHandler mouseActionListener;
	private LinearDistanceInteractiveSettings settings;
	private Double step, offset;
	Overlay ovl;

	public LinearDistanceInteractiveHandler(ImagePlus image, LinearDistanceInteractiveSettings settings,  LinearDistanceInteractiveMenuStrip parentStrip) {
		this.settings = settings;
		
		iplus = image;
		ip = iplus.getProcessor();
		icanv = iplus.getCanvas();
		menuStrip = parentStrip;

		mouseActionListener = new LinearDistanceInteractiveMouseHandler(this);

		ImageCanvas icanv = iplus.getCanvas();
		icanv.addMouseMotionListener(mouseActionListener);
		icanv.addMouseListener(mouseActionListener);

		ij.IJ.setTool(12);
		icanv.disablePopupMenu(true);
		iplus.draw();

		Calibration cal = iplus.getCalibration();

		if (settings.doCalibrateStep) 
			step = (Double) settings.step / ((settings.directionY) ? cal.pixelWidth : cal.pixelHeight);
		else
			step = settings.step;
		
		if (settings.doCalibrateOffset) 
			offset = (Double) settings.offset / ((settings.directionY) ? cal.pixelWidth : cal.pixelHeight);
		else 
			offset = settings.offset;
		
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

	public boolean askResults() {	
		GenericDialog gd = new GenericDialog("Select results");
		gd.addMessage("Select the results you want to obtain.");
		gd.addCheckboxGroup(1, 6, settings.resultsTable, settings.doResults);
		gd.addCheckbox("Apply image calbration", settings.doApplyCalibration);

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		for (int i = 0; i < settings.doResults.length; i++) {
			settings.doResults[i] = gd.getNextBoolean();
		}
		settings.doApplyCalibration = gd.getNextBoolean();

		Prefs.set("LinearDistanceInteractive.doResults", Tools.BooleanToString(settings.doResults));
		Prefs.set("LinearDistanceInteractive.doApplyScale", settings.doApplyCalibration);

		settings.save();
		
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
		ResultsTable rt = settings.restable;
		rt.incrementCounter();
		int row = rt.getCounter() - 1;
		String unit = "px";
		Calibration cal = iplus.getCalibration();

		double calval = 1d;
		if (settings.doApplyCalibration) {
			calval = (settings.directionY) ? cal.pixelHeight : cal.pixelWidth;
			unit = iplus.getCalibration().getUnit();
		}

		rt.setValue("Image", row, iplus.getTitle() + ((settings.directionY) ? " V" : " H"));

		for (int ri = 0; ri < settings.doResults.length; ri++) {
			if (!settings.doResults[ri])
				continue;
			String rName = settings.resultsTable[ri];
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
		
		int[] found = findPoint(cursorPos);
		if (found!=null)
			return;
		
		int line = getNextLine(cursorPos);

		if (markList.get(line) == null) {
			markList.put(line, new ArrayList<Integer>());
		}

		if (settings.directionY)
			markList.get(line).add(cursorPos.y);
		else
			markList.get(line).add(cursorPos.x);
		
		drawOverlay();
		numMarks++;
		menuStrip.setCounts(numMarks);
	}

	public void removePoint() {
		Point cursorPos = getRealPos();

		int[] found = findPoint(cursorPos);
		
		if (found != null) {
			markList.get(found[0]).remove((Object) found[1]);
			numMarks--;
			drawOverlay();
		}
		menuStrip.setCounts(numMarks);
	}
	
	public int[] findPoint(Point pos) {
		int dist = Integer.MAX_VALUE;
		int[] found = new int[2];
		
		int line = getNextLine(pos);

		if (markList.get(line) == null)
			return null;
		
		Integer lpos = (settings.directionY) ? pos.y : pos.x;

		for (Integer mark : markList.get(line)) {
			int dt = Math.abs(mark - lpos);
			if (dt < dist) {
				dist = dt;
				found[0] = line;
				found[1] = mark;
			}
		}
		if (found != null && dist <= settings.remtol)
			return found;
		
		return null;
	}

	public void drawOverlay() {
		//overlay.copyBits(ip, 0, 0, Blitter.COPY);
		ImageProcessor overlay = new ColorProcessor(ip.getWidth(), ip.getHeight());
		//overlay = (ImageProcessor) ip.clone();
		overlay.setColor(settings.getovlColor());
		int pxlh = (settings.directionY) ? overlay.getWidth() : overlay.getHeight();
		int pxlw = (!settings.directionY) ? overlay.getWidth() : overlay.getHeight();
		int line = 0;
		int nearLine = 0;
		Point cursorPos = getRealPos();
		
		if (cursorPos != null)
			nearLine = getNextLine(cursorPos);
		
		double offsetLeft = offset; 
		
		if (settings.doCenterLines){
			Double numlines = (((double) pxlh - 2.0 * offset ) / step);
			double nl = Math.floor(numlines);
			offsetLeft = (pxlh - nl * step) / 2.0;
			//offsetLeft = 1.0;
		}
		int ml = settings.markLength;

		for (double ld = offsetLeft; ld <= pxlh-offset; ld += step) {
			int l = Tools.getRoundedInt(ld);
			if (settings.directionY)
				overlay.drawLine(l, 0, l, pxlw);
			else
				overlay.drawLine(0, l, pxlw, l);

			if (markList.get(line) != null) {
				for (Integer markPos : markList.get(line)) {
					if (settings.directionY)
						overlay.drawLine(l - ml, markPos, l + ml, markPos);
					else
						overlay.drawLine(markPos, l - ml, markPos, l + ml);
				}
			}
			if (line == nearLine && cursorPos != null) {
				if (settings.directionY)
					overlay.drawLine(l - ml, cursorPos.y, l + ml, cursorPos.y);
				else
					overlay.drawLine(cursorPos.x, l - ml, cursorPos.x, l + ml);
			}
			line++;
		}
		
		ImageRoi roi = new ImageRoi(0, 0, overlay);
		//roi.setName(iplus.getShortTitle() + " measured stripes");
		//roi.setOpacity(1d);
		roi.setZeroTransparent(true);

		//roi.setProcessor(ip);
		//ovl = new Overlay(roi);
		//iplus.setOverlay(roi, Color.red, 0, Color.red);
		//iplus.setRoi((ImageRoi) null);
		iplus.setRoi(roi);
		// icanv.setCursor(Cursor.CURSOR_NONE);
		iplus.draw();
	}

	public int getNextLine(Point Cursor) {
		if (Cursor == null)
			throw new NullPointerException("No Point given");
		if (settings.directionY)
			return Tools.getRoundedInt((((double) Cursor.x) - offset) /  step);
		else
			return Tools.getRoundedInt((((double) Cursor.y) - offset) /  step);
	}
}
