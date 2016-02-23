package jp.narr.tensorflowmnist;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Javaによる手書き文字認識.
 * <p/>
 * Created by miyoshi on 16/02/24.
 */
public class JavaDigitDetector {
	private float[][] weightW = new float[768][10];
	private float[] weightB = new float[10];

	/**
	 * draw pixels
	 */
	public int detectDigit(int[] pixels) {
		// 0~255のpixel値を0.0~1.0のfloatに変換する.
		float[] input = new float[768];
		for (int i = 0; i < 768; ++i) {
			input[i] = (float) pixels[i] * (1.0f / 255.0f);
		}

		// 出力値
		float[] output = new float[10];

		// inputとweightWの掛け算
		for (int j = 0; j < 768; ++j) {
			for (int i = 0; i < 10; ++i) {
				output[i] += input[j] * weightW[j][i];
			}
		}

		// それにweightBを足す
		for (int i = 0; i < 10; ++i) {
			output[i] += weightB[i];
		}

		output = softMax(output);

		for (int i = 0; i < 10; ++i) {
			Log.i("demo", "out[" + i + "]=" + output[i]);
		}

		return findMaxIndex(output);
	}

	private float[] softMax(float[] values) {
		float[] exps = new float[values.length];

		float expSum = 0.0f;
		for (int i = 0; i < values.length; ++i) {
			float exp = (float) Math.exp(values[i]);
			exps[i] = exp;
			expSum += exp;
		}

		for (int i = 0; i < values.length; ++i) {
			exps[i] /= expSum;
		}

		return exps;
	}

	/**
	 * @return 一番最大の値のindexを得る.
	 */
	private int findMaxIndex(float values[]) {
		float maxValue = Float.MIN_VALUE;
		int maxIndex = -1;

		for (int i = 0; i < values.length; ++i) {
			float value = values[i];
			if (value > maxValue) {
				maxValue = value;
				maxIndex = i;
			}
		}

		return maxIndex;
	}

	/**
	 * Asset内の "w.csv", "b.csv"を読み込み、それぞれ weightW, weightB に
	 * 値をロードする.
	 */
	public boolean setup(Context context) {
		ArrayList<String> linesW = loadFile(context, "w.csv");
		ArrayList<String> linesB = loadFile(context, "b.csv");

		if (linesW == null || linesB == null) {
			return false;
		}

		// weightWの設定
		for (int i = 0; i < 768; ++i) {
			String line = linesW.get(i);
			float values[] = splitLine(line);
			for (int j = 0; j < 10; ++j) {
				weightW[i][j] = values[j];
			}
		}

		// weightBの設定
		for (int i = 0; i < 10; ++i) {
			String line = linesB.get(i);
			weightB[i] = Float.parseFloat(line);
		}

		return true;
	}

	/**
	 * 1行にある10個のfloat値をパース.
	 */
	private float[] splitLine(String line) {
		StringTokenizer st = new StringTokenizer(line, ",");
		float[] ret = new float[10];
		for (int i = 0; i < ret.length; ++i) {
			String token = st.nextToken();
			ret[i] = Float.parseFloat(token);
		}
		return ret;
	}

	/**
	 * テキストファイルをロードして各行をArrayListに貯めて返す.
	 */
	private ArrayList<String> loadFile(Context context, String fileName) {
		AssetManager assetManager = context.getAssets();

		BufferedReader reader = null;

		try {
			InputStream is = assetManager.open(fileName);
			reader = new BufferedReader(new InputStreamReader(is));

			ArrayList<String> lines = new ArrayList<>();

			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				lines.add(line);
			}
			return lines;
		} catch (IOException e) {
			Log.e("demo", "weight file read failed", e);
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ignored) {
				}
			}
		}

	}
}
