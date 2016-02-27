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

public class Stroke {
	// ブラシ角のタイプ
	public static final int EDGE_TYPE_SOFT = 0; // ソフトエッジ (エッジぼやけ気味)
	public static final int EDGE_TYPE_HARD = 1; // ハードエッジ (エッジくっきりめ)

	public static final int MAX_RADIUS = 15; // 最大ブラシ半径
	public static final int WEIGHT_HWIDTH = MAX_RADIUS; // weightテーブル半幅
	public static final int WEIGHT_WIDTH = MAX_RADIUS * 2 + 1; // weightテーブル幅

	private float[] mWeight = new float[WEIGHT_WIDTH * WEIGHT_WIDTH]; // 筆圧最小時weight

	private int mBrushWidth; // 実際にweightが入っている部分のブラシ幅(必ず奇数になる)
	private float mDelayLength; // 前回のdraw時にあまった長さ
	private float mInterval; // ブラシの1点を打つ間隔

	private static int lowerClip(int v) {
		if (v < 0) {
			return 0;
		}
		return v;
	}

	private boolean drawDot(int x, int y, float v, int[] pixels, int w, int h) {
		if (x < 0) {
			return false;
		}
		if (y < 0) {
			return false;
		}
		if (x >= w) {
			return false;
		}
		if (y >= h) {
			return false;
		}

		int pixel = pixels[y * w + x];

		// ここのシフト部分修正した
		int b = (pixel & 0xff);
		int g = (pixel & 0xff00) >>> 8;
		int r = (pixel & 0xff0000) >>> 16;

		int v_ = (int) (255.0f * v);

		int nr = r - v_;
		int ng = g - v_;
		int nb = b - v_;
		int newR = lowerClip(nr);
		int newG = lowerClip(ng);
		int newB = lowerClip(nb);

		int newPixel = newB | newG << 8 | newR << 16 | 0xff000000;
		pixels[y * w + x] = newPixel;

		return true;
	}

	/**
	 * バイリニアサンプリング値を求める.
	 *
	 * @param tx  , ty 画素内での位置
	 * @param tx_ , ty_ 1-tx, 1-ty
	 * @param a00 , a01, a10, a11 周囲4点の値
	 *            <p/>
	 *            +---------+ | a00 a01 | | a10 a11 | +---------+
	 */
	static float getBilinearSample(float tx, float tx_, float ty, float ty_,
	                               float a00, float a01, float a10, float a11) {
		float b0 = a00 * tx_ + a01 * tx; // x方向の補間
		float b1 = a10 * tx_ + a11 * tx; // x方向の補間

		return b0 * ty_ + b1 * ty; // y方向の補間
	}

	/**
	 * <!-- calcWeight(): -->
	 * <p/>
	 * FIXME リニアになっていないので修正が必要.
	 */
	private float getWeight(int j, int i, float rate) {
		float w0 = mWeight[j * WEIGHT_WIDTH + i];
		return w0;
		/*
		float rate_ = 1.0f - rate;

		float ret = w0 * rate_ + w1 * rate;
		// FIXME 本来無くてもよいはずの処理
		if (ret > 1.0f) {
			ret = 1.0f;
		}
		if (ret < 0.0f) {
			ret = 0.0f;
		}
		return ret;
		*/
	}

	/**
	 * <!-- drawAADot(): -->
	 *
	 * @param rate         weightの間を補間するためのrate. pressureから求まる値( 0.0~1.0)
	 * @param multiplyRate 最終的にもとまった値に対して線形に掛け算するrate.
	 *                     <p/>
	 *                     drawlineで最後に半端で残ったピクセルに関しては、
	 *                     rateでのweight補間(weight0とweight1の補間)した値に対してさらに、
	 *                     multiplyRateをかける必要がある.
	 *                     最後の半端な点以外の点に関してはすべて同じmutiplyRate(1AAdotが表現する 線分の長さに相当)になっている.
	 *                     <p/>
	 *                     ==> multiplyRateは現在1.0でしか使用していない. 様子を見てこれて問題なければ消す事. [TODO]
	 */
	private void drawAADot(float x, float y, int[] pixels, int w, int h,
	                       float rate, float multiplyRate) {

		// x,yがマイナスの時もあるので、単純なintキャスト(ix=(int)x)ではだめ.
		int ix = (int) Math.floor(x);
		int iy = (int) Math.floor(y);

		float tx = x - (float) (ix);
		float ty = y - (float) (iy);

		int sx, sy; // 書き込みバッファのどこの位置を基準としてはじめるか.
		int bw = mBrushWidth; // ブラシweight幅
		int hbw = (bw - 1) / 2; // ブラシ幅の片側半分

		if (tx < 0.5f) {
			tx = 0.5f - tx;
			sx = ix - hbw;
		} else {
			tx = 1.5f - tx;
			sx = ix - hbw + 1;
		}

		if (ty < 0.5f) {
			ty = 0.5f - ty;
			sy = iy - hbw;
		} else {
			ty = 1.5f - ty;
			sy = iy - hbw + 1;
		}

		// +---------+
		// | a00 a01 |
		// | a10 a11 |
		// +---------+
		float tx_ = 1.0f - tx;
		float ty_ = 1.0f - ty;

		int off = WEIGHT_HWIDTH - hbw;

		int i, j;
		for (j = 0; j < bw - 1; ++j) { // y
			for (i = 0; i < bw - 1; ++i) { // x
				float a00 = getWeight(off + j, off + i, rate) * multiplyRate;
				float a01 = getWeight(off + j, off + i + 1, rate) * multiplyRate;
				float a10 = getWeight(off + j + 1, off + i, rate) * multiplyRate;
				float a11 = getWeight(off + j + 1, off + i + 1, rate)
						* multiplyRate;
				float w0 = getBilinearSample(tx, tx_, ty, ty_, a00, a01, a10,
						a11);
				boolean ret = drawDot(sx + i, sy + j, w0, pixels, w, h);
			}
		}
	}

	/**
	 * <!-- Stroke(): -->
	 */
	public Stroke(float radius, float thickness,
	              float interval, int edgeType) {
		mDelayLength = 0.0f;
		setBrush(radius, thickness, interval, edgeType);
	}

	/**
	 * <!-- drawLine(): -->
	 *
	 * @param rate0 0.0〜1.0にノーマライズされた筆圧(始点での)
	 * @param rate1 0.0〜1.0にノーマライズされた筆圧(終点での)
	 */
	public void drawLine(float x0, float y0, float rate0, float x1, float y1,
	                     float rate1, ImageBuffer imageBuffer) {

		float wx = x1 - x0;
		float wy = y1 - y0;
		float wrate = rate1 - rate0;

		float length = (float) Math.sqrt(wx * wx + wy * wy);

		float dx = wx / length * mInterval;
		float dy = wy / length * mInterval;
		float drate = wrate / length * mInterval;

		float lx = x0;
		float ly = y0;
		float rate = rate0;

		// delay length分の反映
		float t = mDelayLength / mInterval;
		float tdx = dx * t;
		float tdy = dy * t;
		float tdrate = drate * t;
		lx += tdx;
		ly += tdy;
		rate += tdrate;

		float len = length;
		len -= mDelayLength;
		// 一旦ここでのlineの長さ分delayLengthを引いておく.
		// (そして1dot打つ毎に、加算していく)
		mDelayLength -= length;

		int[] pixels = imageBuffer.getPixels();
		int w = imageBuffer.getWidth();
		int h = imageBuffer.getHeight();

		for (; len > 0.0f; len -= mInterval) {
			drawAADot(lx, ly, pixels, w, h, rate, 1.0f);
			lx += dx;
			ly += dy;
			rate += drate;
			mDelayLength += mInterval;
		}
	}

	public void reset() {
		mDelayLength = 0;
	}

	/**
	 * <!-- calcWeightSoft(): -->
	 * <p/>
	 * ソフトエッジ.
	 */
	private float calcWeightSoft(float r, float radius, float thickness) {
		float v = r / radius;
		if (v > 1.0f) {
			v = 1.0f;
		}
		float theta = 3.141592f * 0.5f * v;
		return (float) Math.cos(theta) * thickness;
	}

	/**
	 * <!-- calcWeightHard(): -->
	 * <p/>
	 * ハードエッジ.
	 */
	private float calcWeightHard(float r, float radius, float thickness) {
		float v = r / radius;
		v = 1.0f - v;
		if (v < 0.0f) {
			v = 0.0f;
		}
		if (v > 1.0f) {
			v = 1.0f;
		}
		v = 1.0f - (float) Math.sqrt(v);
		return (1 - v * v * v * v) * thickness;
	}

	/**
	 * <!-- setBrush(): -->
	 */
	private void setBrush(float radius, float thickness, float interval_, int edgeType) {
		if (radius > MAX_RADIUS - 1) {
			radius = (float) MAX_RADIUS - 1;
		}

		int i, j;
		for (i = 0; i < WEIGHT_WIDTH; ++i) {
			for (j = 0; j < WEIGHT_WIDTH; ++j) {
				int dx = (WEIGHT_HWIDTH - i);
				int dy = (WEIGHT_HWIDTH - j);
				float r = (float) Math.sqrt((float) (dx * dx + dy * dy));

				if (edgeType == EDGE_TYPE_SOFT) {
					mWeight[j * WEIGHT_WIDTH + i] = calcWeightSoft(r, radius, thickness);
				} else {
					mWeight[j * WEIGHT_WIDTH + i] = calcWeightHard(r, radius, thickness);
				}
			}
		}

		// FIXME 値の設定はまだ仮
		mBrushWidth = ((int) (radius) + 1) * 2 + 1;

		mInterval = interval_;
	}
}
