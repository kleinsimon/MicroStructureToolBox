package mstb.ld;

import mstb.Stat;

public class LinearDistanceResults {
	public Stat wdx = new Stat();
	public Stat wdy = new Stat();
	public Stat wdxwdy = new Stat();
	public Stat bdx = new Stat();
	public Stat bdy = new Stat();
	public Stat bdxbdy = new Stat();
	public Stat wdxbdx = new Stat();
	public Stat wdybdy = new Stat();
	public Stat wdxbdy = new Stat();
	public String title = null;
	public Double calx = 1.0;
	public Double caly = 1.0;
	public String calStr = null;
	public String unit = null;

	
	public Stat[] getAll() {
		return new Stat[]{wdx, wdy, wdxwdy, bdx, bdy, bdxbdy, wdxbdx, wdybdy, wdxbdy};
	}
}
