/*
   Copyright 2016 Narrative Nights Inc. All Rights Reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

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
 * (精度は低い)
 * <p/>
 * Created by miyoshi on 16/02/24.
 */
public class JavaDigitDetector {
	private static final String TAG = "JavaDigitDetector";

	// 784x10のweight値
	private float[][] weightW = new float[784][10];
	// 10個のbias値
	private float[] weightB = new float[10];

	/**
	 * シンプルなニューラルネットによる文字認識を行う.
	 *
	 * @param pixels 28x28のピクセル値
	 * @return 0〜9の認識結果
	 */
	public int detectDigit(int[] pixels) {
		// 0 ~ 255 のpixel値を 0.0 ~ 1.0 のfloatに変換し、それをinputとする.
		float[] input = new float[784];
		for (int i = 0; i < 784; ++i) {
			input[i] = (float) pixels[i] * (1.0f / 255.0f);
		}

		// 10個の出力値を準備
		float[] output = new float[10];

		// input と weightW の掛け算を行う
		for (int j = 0; j < 784; ++j) {
			for (int i = 0; i < 10; ++i) {
				output[i] += input[j] * weightW[j][i];
			}
		}

		// それに weightB を足す
		for (int i = 0; i < 10; ++i) {
			output[i] += weightB[i];
		}

		// SoftMax関数を通して合計を1にしておく
		output = softMax(output);

		for (int i = 0; i < 10; ++i) {
			Log.i(TAG, "output[" + i + "]=" + output[i]);
		}

		// 最大の値のindexを探す
		int maxIndex = findMaxIndex(output);
		return maxIndex;
	}

	/**
	 * 各値が0.0~1.0の値で、合計が1.0になる様に調整するSoftMax関数.
	 */
	private float[] softMax(float[] values) {
		// 各値のexp値
		float[] exps = new float[values.length];

		// 各値のexp値の合計
		float expSum = 0.0f;
		for (int i = 0; i < values.length; ++i) {
			// 各値のexp値を出す
			float exp = (float) Math.exp(values[i]);
			exps[i] = exp;
			// 合計値を加算
			expSum += exp;
		}

		// 合計値で割って、全部のexp値の合計が1になる様にする
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
		for (int j = 0; j < 784; ++j) {
			String line = linesW.get(j);
			float values[] = splitLine(line);
			for (int i = 0; i < 10; ++i) {
				weightW[j][i] = values[i];
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
			Log.e(TAG, "weight file read failed", e);
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
