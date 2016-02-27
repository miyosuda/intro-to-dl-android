package jp.narr.tensorflowmnist;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miyoshi on 16/02/27.
 */
public class LineCommand {
	private List<LinePoint> points = new ArrayList<>();
	private boolean closed;

	public LineCommand() {
		closed = false;
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

	public void close() {
		closed = true;
	}
	
	public boolean isClosed() {
		return closed;
	}	
}
