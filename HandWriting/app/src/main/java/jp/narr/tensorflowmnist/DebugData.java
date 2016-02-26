package jp.narr.tensorflowmnist;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by miyoshi on 16/02/26.
 */
public class DebugData {
	private static final String TAG = "DebugData";

	private byte[] buffer = new byte[4];
	private float images[][] = new float[10000][28 * 28];

	public void setup(Context context) {
		AssetManager assetManager = context.getAssets();

		DataInputStream in = null;

		try {
			in = new DataInputStream(assetManager.open("x.bin"));
			//..for (int j = 0; j < 10000; j++) {
			for (int j = 0; j < 10; j++) {
				for (int i = 0; i < 28 * 28; i++) {
					images[j][i] = readLittleEndianFloat(in);
				}
			}
		} catch (IOException e) {
			Log.e(TAG, "test images file read failed", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	public int readLittleEndianInt(InputStream in) throws IOException {
		in.read(buffer);
		return (buffer[3]) << 24 |
				(buffer[2] & 0xff) << 16 |
				(buffer[1] & 0xff) << 8 |
				(buffer[0] & 0xff);
	}

	public float readLittleEndianFloat(InputStream in) throws IOException {
		return Float.intBitsToFloat(readLittleEndianInt(in));
	}

	public void applyToBitmap(Bitmap bitmap, int index) {
		float[] image = images[index];
		int[] pix = new int[28 * 28];
		for(int i=0; i<28*28; ++i) {
			float fp = image[i];
			int b = (int)(fp * 255.0f);
			b = 255 - b;
			int p = 0xff000000 | b << 16 | b << 8 | b;
			pix[i] = p;
		}
		bitmap.setPixels(pix, 0, 28, 0, 0, 28, 28);
	}
}
