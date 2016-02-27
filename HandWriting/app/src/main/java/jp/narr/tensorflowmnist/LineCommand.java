package jp.narr.tensorflowmnist;

import java.util.ArrayList;
import java.util.List;

public class LineCommand {
	private List<LinePoint> points = new ArrayList<>();

	public LineCommand() {
	}

	public void addPoint(LinePoint elem) {
		points.add(elem);
	}

	public int getPointSize() {
		return points.size();
	}

	public LinePoint getPoint(int index) {
		return points.get(index);
	}
}
