package jp.narr.tensorflowmnist;

import android.content.Context;
import android.content.res.AssetManager;
import android.test.InstrumentationTestCase;
import android.util.Log;

import java.io.*;

/**
 * Created by miyoshi on 16/02/26.
 */
public class JavaDigitDetectorTest extends InstrumentationTestCase {
	private static final String TAG = "JavaDigitDetectorTest";

	private float images[][];
	private int labels[];

	private Context getApplicationContext() {
		return getInstrumentation().getTargetContext().getApplicationContext();
	}

	public void setUp() throws Exception {
		super.setUp();

		Context context = getApplicationContext();
		AssetManager assetManager = context.getAssets();

		DataInputStream in = null;

		images = new float[10000][28 * 28];
		labels = new int[10000];

		try {
			in = new DataInputStream(assetManager.open("x.bin"));
			for (int j = 0; j < 10000; j++) {
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

		in = null;

		try {
			in = new DataInputStream(assetManager.open("y.bin"));
			for (int j = 0; j < 10000; j++) {
				labels[j] = readLittleEndianInt(in);
			}
		} catch (IOException e) {
			Log.e(TAG, "test labels read failed", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	private byte[] buffer = new byte[4];

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

	public void testDetectDigit() throws Exception {
		JavaDigitDetector detector = new JavaDigitDetector();

		Context context = getApplicationContext();
		boolean ret = detector.setup(context);

		int succeedCount = 0;

		for(int i=0; i<10000; ++i) {
			float[] input = images[i];
			int output = detector.detectDigitSub(input);
			int label = labels[i];
			if (output == label) {
				succeedCount++;
			}
		}

		Log.i(TAG, "success count=" + succeedCount); //..

		assertEquals(succeedCount > 8500, true);
	}
}
