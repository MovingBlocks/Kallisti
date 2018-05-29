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

import java.util.List;

public class LuaProxyList implements LuaProxy<List> {
    @Override
    public Object index(List object, Object key) {
        if (key instanceof Number) {
            int p = ((Number) key).intValue() - 1;
            if (p >= 0 && p < object.size()) {
                return object.get(p);
            }
        } else if ("n".equals(key)) {
            return object.size();
        }

        return null;
    }

    @Override
    public void newindex(List object, Object key, Object value) {
        if (key instanceof Number) {
            int p = ((Number) key).intValue() - 1;
            if (p >= 0 && p < object.size()) {
                object.set(p, value);
            }
        }
    }
}
