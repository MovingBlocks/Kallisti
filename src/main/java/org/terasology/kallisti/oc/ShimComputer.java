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

public class ShimComputer {
	private final MachineOpenComputers machine;
	private String bootAddress = null;

	public ShimComputer(MachineOpenComputers machine) {
		this.machine = machine;
	}

	public boolean isRobot() {
		return false;
	}

	public String address() {
		return machine.getComputerAddress();
	}

	public String tmpAddress() {
		// TODO
		for (PeripheralOCFilesystem o : machine.getComponentsByClass(PeripheralOCFilesystem.class)) {
			return machine.getComponentAddress(o);
		}

		return "";
	}

	public int freeMemory() {
		return 0;
	}

	public int totalMemory() {
		return 0;
		// return machine.getLuaState().getTotalMemory();
	}

	public int energy() {
		return 10000; // TODO
	}

	public int maxEnergy() {
		return 10000; // TODO
	}

	public double uptime() {
 		return machine.getTime();
	}

	public double realTime() {
		return 0.0; // TODO
	}

	public String getBootAddress() {
		return bootAddress;
	}

	public void setBootAddress(String address) {
		bootAddress = address;
	}

	public String users() {
		return "TODO";
	}

	public Object addUser(String name) {
		return null; // TODO
	}

	public boolean removeUser(String name) {
		return false; // TODO
	}

	public void pushSignal(Object... args) {
		if (args.length >= 1 && args[0] instanceof String) {
			machine.pushSignal(args);
		}
	}

	public void setArchitecture(String s) {
		// TODO
	}
}
