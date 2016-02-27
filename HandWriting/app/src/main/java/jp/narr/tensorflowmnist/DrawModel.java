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

import java.util.ArrayList;
import java.util.List;

public class DrawModel {
	private transient LineCommand mCurrentLine;

	private int mWidth;
	private int mHeight;

	private int undomUndoCursorIndexIndex;
	private List<LineCommand> mCommands = new ArrayList<>();

	public DrawModel(int width, int height) {
		this.mWidth = width;
		this.mHeight = height;
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public void startLine(float x, float y) {
		clearHistoryFromNow();
		
		mCurrentLine = new LineCommand();
		mCurrentLine.addPoint(new LinePoint(x, y));
		mCommands.add(mCurrentLine);
		undomUndoCursorIndexIndex = mCommands.size();
	}
	
	private void clearHistoryFromNow() {
		if (undomUndoCursorIndexIndex < mCommands.size()) {
			int size = mCommands.size();
			for (int i = size - 1; i >= undomUndoCursorIndexIndex; --i) {
				mCommands.remove(i);
			}
		}
	}

	public void endLine() {
		if( mCurrentLine != null ) {
			mCurrentLine = null;
		}
	}

	public void addLinePoint(float x, float y) {
		if (mCurrentLine != null) {
			mCurrentLine.addPoint(new LinePoint(x, y));
		}
	}

	/**
	 * @return 表示用にundoCursorの位置までを返す
	 */
	public int getLineSize() {
		return undomUndoCursorIndexIndex;
	}

	public LineCommand getCommand(int index) {
		return mCommands.get(index);
	}

	public void clear() {
		mCommands.clear();
		undomUndoCursorIndexIndex = 0;
		mCurrentLine = null;
	}
}
