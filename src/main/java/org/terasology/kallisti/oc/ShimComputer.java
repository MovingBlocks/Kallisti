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

public class ShimComputer {
	private final MachineOpenComputers machine;
	private String bootAddress = null;

	public ShimComputer(MachineOpenComputers machine) {
		this.machine = machine;
	}

	@ComponentMethod
	public boolean isRobot() {
		return false;
	}

	@ComponentMethod
	public String address() {
		return machine.getComputerAddress();
	}

	@ComponentMethod
	public String tmpAddress() {
		// TODO
		for (PeripheralOCFilesystem o : machine.getComponentsByClass(PeripheralOCFilesystem.class)) {
			return machine.getComponentAddress(o);
		}

		return "";
	}

	@ComponentMethod
	public int freeMemory() {
		return Math.round(machine.getLuaState().getFreeMemory() / machine.getMemorySizeMultiplier());
	}

	@ComponentMethod
	public int totalMemory() {
		return Math.round(machine.getLuaState().getTotalMemory() / machine.getMemorySizeMultiplier());
	}

	@ComponentMethod
	public int energy() {
		return 10000; // TODO
	}

	@ComponentMethod
	public int maxEnergy() {
		return 10000; // TODO
	}

	@ComponentMethod
	public double uptime() {
 		return machine.getTime();
	}

	@ComponentMethod
	public double realTime() {
		return (System.currentTimeMillis() / 1000.0);
	}

	@ComponentMethod
	public String getBootAddress() {
		return bootAddress;
	}

	@ComponentMethod
	public boolean setBootAddress(String address) {
		bootAddress = address;
		return true;
	}

	@ComponentMethod
	public String users() {
		return "TODO";
	}

	@ComponentMethod
	public Object addUser(String name) {
		return null; // TODO
	}

	@ComponentMethod
	public boolean removeUser(String name) {
		return false; // TODO
	}

	@ComponentMethod
	public void pushSignal(Object... args) {
		if (args.length >= 1 && args[0] instanceof String) {
			machine.pushSignal(args);
		}
	}

	@ComponentMethod
	public void setArchitecture(String s) {
		// TODO
	}
}
