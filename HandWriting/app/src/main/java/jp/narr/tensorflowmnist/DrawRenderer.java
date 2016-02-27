package jp.narr.tensorflowmnist;

public class DrawRenderer {

	//@formatter:off
    private static Stroke[] penStrokes = new Stroke[] {
		//new Stroke(1.0f, 0.0f, 1.0f, 0.2f, 0.5f, Stroke.EDGE_TYPE_SOFT),
		//new Stroke(1.2f, 0.0f, 1.2f, 0.7f, 0.5f, Stroke.EDGE_TYPE_SOFT),
		new Stroke(1.2f, 0.0f, 1.2f, 0.7f, 0.5f, Stroke.EDGE_TYPE_HARD),
	};
    //@formatter:on

	private static void renderLine(LineCommand line, int index, ImageBuffer imageBuffer, DrawInfo drawInfo) {
		Stroke stroke = penStrokes[0];

		int pointSize = line.getPointSize();
		if (pointSize < 1) {
			return;
		}

		int startPointIndex = drawInfo.getStartPointIndex(index);

		LinePoint point = line.getPoint(startPointIndex);
		float lastX = point.x;
		float lastY = point.y;
		float lastPressure = point.pressure;

		for (int j = startPointIndex; j < pointSize; ++j) {
			if (j == 0) {
				stroke.reset();
			}

			point = line.getPoint(j);
			float x = point.x;
			float y = point.y;
			float pressure = point.pressure;

			stroke.drawLine(lastX, lastY, lastPressure, x, y, pressure, imageBuffer);

			lastX = x;
			lastY = y;
			lastPressure = pressure;
		}
	}

	public static void renderModel(ImageBuffer imageBuffer, DrawModel model, DrawInfo drawInfo) {
		int lineSize = model.getLineSize();
		int startLineIndex = drawInfo.getStartLineIndex();

		for (int i = startLineIndex; i < lineSize; ++i) {
			LineCommand line = model.getCommand(i);
			renderLine(line, i, imageBuffer, drawInfo);

			if (i == lineSize - 1) {
				drawInfo.setLastCommandSize(lineSize);
				drawInfo.setLastPointSize(line.getPointSize());
			}
		}
	}
}
