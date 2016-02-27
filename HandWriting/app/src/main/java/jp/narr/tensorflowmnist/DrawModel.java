package jp.narr.tensorflowmnist;

import java.util.ArrayList;
import java.util.List;

public class DrawModel {
	private transient LineCommand currentLine;

	private int width;
	private int height;

	private int undoCursorIndex;
	private List<LineCommand> commands = new ArrayList<>();

	public DrawModel(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void startLine(float x, float y, float pressure) {
		clearHistoryFromNow();
		
		currentLine = new LineCommand();
		currentLine.addPoint(new LinePoint(x, y, pressure));
		commands.add(currentLine);
		undoCursorIndex = commands.size();
	}
	
	private void clearHistoryFromNow() {
		if (undoCursorIndex < commands.size()) {
			int size = commands.size();
			for (int i = size - 1; i >= undoCursorIndex; --i) {
				commands.remove(i);
			}
		}
	}

	public void endLine() {
		if( currentLine != null ) {
			//currentLine.close();
			currentLine = null;			
		}
	}

	public void addLinePoint(float x, float y, float pressure) {
		if (currentLine != null) {
			currentLine.addPoint(new LinePoint(x, y, pressure));
		}
	}

	/**
	 * @return 表示用にundoCursorの位置までを返す
	 */
	public int getLineSize() {
		return undoCursorIndex;
	}

	public LineCommand getCommand(int index) {
		return commands.get(index);
	}

	public void clear() {
		commands.clear();
		undoCursorIndex = 0;
		currentLine = null;
	}
}
