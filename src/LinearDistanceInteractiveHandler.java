import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
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
import ij.process.Blitter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class LinearDistanceInteractiveHandler {
	private int lineDistPx, offsetPx, markLengthPx;
	private Hashtable<Integer, ArrayList<Integer>> markList = new Hashtable<Integer, ArrayList<Integer>>();
	private boolean directionY = false;
	private boolean doApplyCalibration = false;
	private ImagePlus iplus = null;
	private ImageProcessor overlay = null;
	private ImageCanvas icanv = null;
	private Integer remtol = 10;
	private Integer menuHeight = 16;
	private Integer numMarks = 0;
	private LinearDistanceInteractiveMenuStrip menuStrip;
	ImageProcessor ip = null;
	private String[] resultsTable;
	private boolean[] doResults;
	public LinearDistanceInteractiveMouseHandler mouseActionListener;
	private ResultsTable rt;
	Overlay ovl;

	public LinearDistanceInteractiveHandler(int lineDistancePx, int offSetPx, int markLenPx, Boolean dirY,
			ImagePlus image, String[] resTable, boolean[] doRes, ResultsTable restable,
			LinearDistanceInteractiveMenuStrip parentStrip) {
		lineDistPx = lineDistancePx;
		offsetPx = offSetPx;
		markLengthPx = markLenPx;
		directionY = dirY;
		iplus = image;
		ip = iplus.getProcessor();
		icanv = iplus.getCanvas();
		resultsTable = resTable;
		doResults = doRes;
		rt = restable;
		menuStrip = parentStrip;

		overlay = new ColorProcessor(ip.getWidth(), ip.getHeight());

		mouseActionListener = new LinearDistanceInteractiveMouseHandler(this);

		ImageCanvas icanv = iplus.getCanvas();
		icanv.addMouseMotionListener(mouseActionListener);
		icanv.addMouseListener(mouseActionListener);

		ImageRoi roi = new ImageRoi(0, 0, overlay);
		roi.setName(iplus.getShortTitle() + " measured stripes");
		roi.setOpacity(1d);
		// roi.setZeroTransparent(true);

		ovl = new Overlay(roi);
		iplus.setOverlay(roi, Color.red, 0, Color.black);
		// iplus.setRoi(roi);

		ij.IJ.setTool(12);
		icanv.disablePopupMenu(true);
		drawOverlay();
		iplus.draw();
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
		menuStrip.getParent().remove(menuStrip);
		overlay.setColor(Color.TRANSLUCENT);
		overlay.fill();
		iplus.setOverlay(null);
		icanv.removeMouseListener(mouseActionListener);
		icanv.disablePopupMenu(false);
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
			for (Integer mark : e.getValue()) {
				if (lastMark == 0) {
					lastMark = mark;
					continue;
				}
				Integer diff = mark - lastMark;
				stripes.add(diff.doubleValue());
			}
		}

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
		overlay.copyBits(ip, 0, 0, Blitter.COPY);
		overlay.setColor(Color.RED);
		int pxls = (directionY) ? overlay.getHeight() : overlay.getWidth();
		int line = 0;
		int nearLine = 0;
		Point cursorPos = getRealPos();
		if (cursorPos != null)
			nearLine = getNextLine(cursorPos);

		for (int l = offsetPx; l < pxls; l = l + lineDistPx) {
			if (directionY)
				overlay.drawLine(l, 0, l, pxls);
			else
				overlay.drawLine(0, l, pxls, l);

			if (markList.get(line) != null) {
				for (Integer markPos : markList.get(line)) {
					if (directionY)
						overlay.drawLine(l - markLengthPx, markPos, l + markLengthPx, markPos);
					else
						overlay.drawLine(markPos, l - markLengthPx, markPos, l + markLengthPx);
				}
			}
			if (line == nearLine && cursorPos != null) {
				if (directionY)
					overlay.drawLine(l - markLengthPx, cursorPos.y, l + markLengthPx, cursorPos.y);
				else
					overlay.drawLine(cursorPos.x, l - markLengthPx, cursorPos.x, l + markLengthPx);
			}
			line++;
		}
		// icanv.setCursor(Cursor.CURSOR_NONE);
		iplus.draw();
	}

	public int getNextLine(Point Cursor) {
		if (Cursor == null)
			throw new NullPointerException("No Point given");
		if (directionY)
			return Tools.getRoundedInt((double) (Cursor.x - offsetPx) / (double) lineDistPx);
		else
			return Tools.getRoundedInt((double) (Cursor.y - offsetPx) / (double) lineDistPx);
	}
}
