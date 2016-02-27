package jp.narr.tensorflowmnist;

import android.graphics.Region;

public class DrawInfo {
	private int lastCommandSize;
	private int lastPointSize;

	public int getStartLineIndex() {
		int startLineIndex = lastCommandSize - 1;
		if (startLineIndex < 0) {
			return 0;
		}
		return startLineIndex;
	}

	public int getStartPointIndex(int curLineIndex) {
		if (curLineIndex == lastCommandSize - 1) {
			int startElemIndex = lastPointSize - 1;
			if (startElemIndex < 0) {
				return 0;
			}
			return startElemIndex;
		} else {
			return 0;
		}
	}

	public void setLastCommandSize(int commandSize) {
		lastCommandSize = commandSize;
	}

	public void setLastPointSize(int pointSize) {
		lastPointSize = pointSize;
	}

	public void reset() {
		lastCommandSize = 0;
		lastPointSize = 0;

	}
}
