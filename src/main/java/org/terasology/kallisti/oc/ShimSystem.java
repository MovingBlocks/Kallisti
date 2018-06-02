/*
 * Copyright 2018 Adrian Siekierka, MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.kallisti.oc;

import org.terasology.kallisti.base.component.ComponentMethod;

public class ShimSystem {
	private final MachineOpenComputers machine;

	public ShimSystem(MachineOpenComputers machine) {
		this.machine = machine;
	}

	@ComponentMethod
	public double timeout() {
		return 1.0; // TODO
	}

	@ComponentMethod
	public boolean allowBytecode() {
		return false; // TODO
	}

	@ComponentMethod
	public boolean allowGC() {
		return false; // TODO
	}
}
