import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

class Stat {
	private Double mean = null;
	private Double sum = null;
	private Double variance = null;
	private Double stD = null;
	private Integer num = null;
	private Double median = null;
	private List<Collection<Double>> values = new ArrayList<Collection<Double>>();

	public Stat() {

	}

	public Stat(List<Double>... lists) {
		this();
		addList(lists);
	}

	public void invalidate() {
		median = mean = stD = sum = null;
		num = null;
	}

	public void addList(Collection<Double>... lists) {
		for (Collection<Double> l : lists) {
			values.add(l);
		}
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

	public int getN() {
		if (num == null) {
			num = 0;
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

	public double getMedian() {
		if (median == null) {
			Double[] bigList = new Double[getN()];
			int i = 0;
			for (Collection<Double> list : values) {
				for (Double n : list) {
					bigList[i] = n;
					i++;
				}
			}
			Arrays.sort(bigList);

			int size = bigList.length;
			int mid = size / 2;
			if (size % 2 == 0) {
				double left = bigList[mid - 1];
				double right = bigList[mid];
				median = (left + right) / 2;
			} else {
				median = bigList[mid];
			}
		}
		return median;
	}
	
	public double getVariance() {
		if (variance == null) {
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
			variance = (dst / (double) num);
		}
		return variance;
	}

	public double getStDev() {
		if (stD == null) {
			variance = getVariance();
			stD = Math.sqrt(variance);
		}
		return stD;
	}
}