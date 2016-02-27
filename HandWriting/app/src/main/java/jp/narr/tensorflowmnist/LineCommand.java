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

public class LineCommand {
	private List<LinePoint> mPoints = new ArrayList<>();

	public LineCommand() {
	}

	public void addPoint(LinePoint elem) {
		mPoints.add(elem);
	}

	public int getPointSize() {
		return mPoints.size();
	}

	public LinePoint getPoint(int index) {
		return mPoints.get(index);
	}
}
