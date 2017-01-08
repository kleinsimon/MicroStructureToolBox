import java.awt.Color;
import java.awt.Point;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.ImageCanvas;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.measure.ResultsTable;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class PointAnalysisInteractiveHandler {
	private Integer px, py, nd, sx, sy, markLengthPx;
	private Double dx, dy, ox, oy;
	private Hashtable<Point, Integer> markList = new Hashtable<Point, Integer>();
	private ImagePlus iplus = null;
	private ImageCanvas icanv = null;
	private ArrayList<Point> points;
	private PointAnalysisInteractiveMenuStrip menuStrip;
	ImageProcessor ip = null;
	public PointAnalysisInteractiveMouseHandler mouseActionListener;
	private ResultsTable rt;
	private Boolean randomize;
	private Color ovlColor;

	public PointAnalysisInteractiveHandler(ImagePlus image, ResultsTable restable,
			PointAnalysisInteractiveMenuStrip parentStrip) {
		
		px = ((Double) Prefs.get("PointAnalysisInteractive.pointsX", 20)).intValue();
		py = ((Double) Prefs.get("PointAnalysisInteractive.pointsY", 20)).intValue();
		nd = ((Double) Prefs.get("PointAnalysisInteractive.numDomains", 2)).intValue();
		randomize = Prefs.get("PointAnalysisInteractive.randomizePoints", false);
		
		markLengthPx = ((Double) Prefs.get("PointAnalysisInteractive.markLength", 5)).intValue();
		iplus = image;
		ip = iplus.getProcessor();
		icanv = iplus.getCanvas();
		rt = restable;
		menuStrip = parentStrip;

		setColor(Prefs.get("PointAnalysisInteractive.overlayColor", "Red"));
		
		sx = iplus.getWidth();
		sy = iplus.getHeight();
				
		dx = ((double) sx / ((double) px + 1.0));
		dy = ((double) sy / ((double) py + 1.0));
		
		ox = dx;
		oy = dy;
		
		mouseActionListener = new PointAnalysisInteractiveMouseHandler(this);

		ImageCanvas icanv = iplus.getCanvas();
		icanv.addMouseMotionListener(mouseActionListener);
		icanv.addMouseListener(mouseActionListener);

		initPoints();
		
		ij.IJ.setTool(12);
		icanv.disablePopupMenu(true);
		drawOverlay();
		iplus.draw();
		menuStrip.updateCounter(getMarkCounts());
	}

	public void setColor(String overlayColor) {
		try {
		    Field field = Color.class.getField(overlayColor.toUpperCase());
		    ovlColor = (Color)field.get(null);
		} catch (Exception e) {
			ovlColor = Color.RED;
		}
	}

	public void updateSize() {
		iplus.getWindow().validate();
		icanv.zoomIn(0, 0);
		icanv.zoomOut(0, 0);
	}
	
	public void initPoints() {
		int n;
		if (randomize){
			n = px.intValue();
			points = new ArrayList<Point>(n);
			
			for (int i=0; i<n; i++){
				Point p = new Point(rndInt(0, sx), rndInt(0,sy));
				points.add(p);
				markList.put(p, 0);
			}
		} else {
			n = px.intValue()  * py.intValue();
			points = new ArrayList<Point>(n);
			
			for (int ix=0; ix < px; ix++) {
				for (int iy=0; iy < py; iy++) {
					Point p = new Point(Tools.getRoundedInt(ox + (ix * dx)),Tools.getRoundedInt(oy + (iy * dy)));
					points.add(p);
					markList.put(p, 0);
				}
			}
		}		
	}
	
	public int rndInt(int min, int max){
		double r = Math.random();
		double s = max - min;
		return Tools.getRoundedInt(min + r * s);
	}
	
	public int rndInt(Double min, Double max){
		double r = Math.random();
		double s = max - min;
		return Tools.getRoundedInt(min + r * s);
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

	public void analyze() {

		Integer[] domains = getMarkCounts();

		rt.incrementCounter();
		int row = rt.getCounter() - 1;

		rt.setValue("Image", row, iplus.getTitle());

		for (int ri = 0; ri < nd; ri++) {
			String cName = String.format("Domain %d", ri+1);
			rt.setValue(cName, row, domains[ri]);
		}

		rt.show("Point Analysis Results");
	}
	
	public Integer[] getMarkCounts() {
		Integer domains[] = new Integer[nd];
		
		for (int i=0; i<domains.length; i++)
			domains[i]=0;
		
		for (Integer p : markList.values()) {
			domains[p]++;
		}
		return domains;
	}

	public void clear() {
		initPoints();
		iplus.updateAndDraw();
	}

	public void togglePoint(boolean rev) {
		Point cursorPos = getRealPos();
		Point p = getNextPoint(cursorPos);

		if (markList.get(p) == null) {
			markList.put(p, 1);
		} 
		else {
			int olddom = markList.get(p);
				markList.put(p, circleDom(olddom, rev));
			
		}
		drawOverlay();
		menuStrip.updateCounter(getMarkCounts());
	}
	
	private int circleDom(int olddom, boolean rev) {
		int min = 0;
		int max = nd-1;
		
		if (!rev) {
			if (olddom == max) 
				return min;
			else
				return olddom+1;
		} else {
			if (olddom == min)
				return max;
			else
				return olddom-1;
		}
	}

	public void drawOverlay() {
		ImageProcessor overlay = new ColorProcessor(ip.getWidth(), ip.getHeight());

		overlay.setColor(ovlColor);

		int l = markLengthPx;

		Enumeration<Point> e = markList.keys();
		while (e.hasMoreElements()) {
			Point p = e.nextElement();
			Integer dom = markList.get(p)+1;
			drawMark(dom, overlay, p, l);
		}
		
		ImageRoi roi = new ImageRoi(0, 0, overlay);
		//roi.setName(iplus.getShortTitle() + " measured stripes");
		//roi.setOpacity(1d);
		roi.setZeroTransparent(true);

		//ovl = new Overlay(roi);


		//icanv.setOverlay(ovl);
		//iplus.setOverlay(roi, Color.red, 0, Color.red);
		iplus.setRoi(roi);
		// icanv.setCursor(Cursor.CURSOR_NONE);
		iplus.draw();
	}
	
	public void drawMark(Integer n, ImageProcessor overlay, Point p, int l) {
		int l2 = l/2;
		int cx = p.x - l2;
		int cy = p.y - l2; 
		
		switch (n) {
		case 1:
			//Cross
			overlay.drawLine(cx, p.y, cx+l, p.y);
			overlay.drawLine(p.x, cy, p.x, cy+l);	
			break;
		case 2:
			//Circle
			overlay.drawOval(cx, cy, l, l);
			overlay.drawDot(p.x, p.y);
			break;
		case 3:
			//X
			overlay.drawLine(cx, cy, cx+l, cy+l);
			overlay.drawLine(cx, cy+l, cx+l, cy);	
			break;
		case 4:
			//Cross 1
			overlay.drawLine(cx, cy, cx+l, cy+l);
			overlay.drawLine(p.x, cy, p.x, cy+l);	
			break;
		case 5:
			//Cross 2
			overlay.drawLine(cx, p.y, cx+l, p.y);
			overlay.drawLine(cx, cy+l, cx+l, cy);	
			break;
		default:
			//Square
			overlay.drawRect(cx, cy, l+1, l+1);
			overlay.drawDot(p.x, p.y);
			break;
		}
		overlay.drawString(n.toString(), p.x+l, p.y);
	}
	
	public void applyThreshold(int mode, int threshold) {
		int[] c = new int[3];
		for (Point p : points) {
			Boolean over = false;
			c = ip.getPixel(p.x, p.y, c);
			if (ip.isBinary() || ip.isGrayscale()) 
				over = checkThreshold(c[0], threshold);
			else if (ip.isColorLut())
				over = checkThreshold(c[0], c[1], c[2], threshold, mode);
			markList.put(p, (over) ? 1 : 0);
		}
		drawOverlay();
		menuStrip.updateCounter(getMarkCounts());
	}
	
	public Boolean checkThreshold(int r, int g, int b, int threshold, int mode) {
		float[] hsbvals = java.awt.Color.RGBtoHSB(r, g, b, null);
		
		switch(mode) {
			case 0:
				return hsbvals[0] >= threshold;
			case 1:
				return hsbvals[1] >= threshold ;
			case 2:
				return hsbvals[2] >= threshold ;
		}
		
		return false;
	}
	public Boolean checkThreshold(java.awt.Color color, int threshold, int mode) {
		int b = color.getBlue();
		int r = color.getRed();
		int g = color.getGreen();
		return checkThreshold(r, g, b, threshold, mode);
	}
	
	public Boolean checkThreshold(int value, int threshold) {
		return value >= threshold;
	}

	public Point getNextPoint(Point Cursor) {
		if (Cursor == null)
			throw new NullPointerException("No Point given");
		

		double dst = Double.POSITIVE_INFINITY;
		Point nearest = null;
		
		for (Point p : points) {
			double d = Point.distance(p.x, p.y, Cursor.x, Cursor.y);
			if (d<dst){
				dst = d;
				nearest = p;
			}
		}

		return nearest;
	}
}
