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
import org.terasology.kallisti.base.component.Peripheral;

import java.util.Collections;
import java.util.List;

public class PeripheralOCComputer implements Peripheral {
    @ComponentMethod
    public void beep(Number pitch, Number duration) {
        // TODO
    }

    @ComponentMethod
    public List<String> getProgramLocations() {
        return Collections.emptyList();
    }

    @Override
    public String type() {
        return "computer";
    }
}
