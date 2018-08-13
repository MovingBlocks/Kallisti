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

package org.terasology.kallisti.base.component;

import org.terasology.kallisti.base.interfaces.Identifiable;

/**
 * A ComponentContext is an object used for identifying components from
 * external sources inside a Kallisti virtual machine. It should be implemented
 * in a way allowing, given a ComponentContext, to find the in-engine location
 * of a Kallisti component's provider.
 *
 * Please note that a valid ComponentContext must implement equals() and
 * hashCode().
 */
public interface ComponentContext extends Identifiable {
}
