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

package org.terasology.kallisti.base.interfaces;

import org.terasology.kallisti.base.component.ComponentInterface;
import org.terasology.kallisti.base.util.PersistenceException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An interface for components whose data can be stored, persisted and read,
 * including across version upgrades.
 */
public interface Persistable {
	void persist(OutputStream data) throws IOException, PersistenceException;
	void unpersist(InputStream data) throws IOException, PersistenceException;
}
