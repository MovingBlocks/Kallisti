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

package org.terasology.kallisti.base.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public final class KallistiFileUtils {
	private KallistiFileUtils() {
	}

	/**
	 * Reads the contents of a file into a byte array.
	 * @param inFile The file to read.
	 * @return The read byte array.
	 * @throws IOException
	 */
	public static byte[] read(File inFile) throws IOException {
		FileInputStream in = new FileInputStream(inFile);
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}

		in.close();
		out.close();

		return out.toByteArray();
	}

	/**
	 * Reads the contents of a file into a string using the UTF-8 character set.
	 * @param inFile The file to read.
	 * @return The read string.
	 * @throws IOException
	 */
	public static String readString(File inFile) throws IOException {
		return readString(inFile, Charset.forName("UTF-8"));
	}

	/**
	 * Reads the contents of a file into a string using the provided character set.
	 * @param inFile The file to read.
	 * @param charset The character set to use.
	 * @return The read string.
	 * @throws IOException
	 */
	public static String readString(File inFile, Charset charset) throws IOException {
		return new String(read(inFile), charset);
	}

}
