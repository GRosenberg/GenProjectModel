/*******************************************************************************
 * // Copyright ==========
 * Copyright (c) 2008-2014 G Rosenberg.
 * // Copyright ==========
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 * // Contributor ==========
 *		G Rosenberg - initial API and implementation
 * // Contributor ==========
 *
 * Versions:
 * // Version ==========
 * 		1.0 - 2014.03.26: First release level code
 * 		1.1 - 2014.08.26: Updates, add Tests support
 * // Version ==========
 *******************************************************************************/
// IOProcessor ==========
package net.certiv.json.generator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.certiv.json.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class IOProcessor {

	private String[] opts; // pathnames
	private String source; // processed pathnames
	private String target;

	private boolean FileIn; // flags
	private boolean FileOut;
	private boolean StdIO;
	private boolean TextIn;

	private String srcData; // loaded mtl source data

	public IOProcessor(String[] args) {
		if (args.length == 0) {
			printHelp();
		}
		opts = new String[3];
		opts[0] = opts[1] = opts[2] = "";
		for (int idx = 0; idx < args.length; idx++) {

			switch (args[idx].trim().toLowerCase()) {
				case "-h":
					printHelp();
					break;
				default:
					printHelp();
					break;
			}
		}
	}

	public boolean init() {
		if (!(FileIn || StdIO)) {
			Log.error(this, "Need to specify a source.");
			return false;
		}

		if (FileIn) {
			source = opts[0];
			File srcFile = new File(source);
			if (!srcFile.exists()) {
				Log.error(this, "Source file does not exist (" + source + ")");
				return false;
			}
		}

		if (FileOut) {
			target = opts[1];
		} else if (FileIn && !StdIO) {
			FileOut = true;
			int idx = source.lastIndexOf('.');
			target = source.substring(0, idx + 1) + "html";
		}
		if (FileOut) {
			File dstFile = new File(target);
			if (dstFile.exists()) {
				dstFile.delete();
				dstFile = new File(target);
			}
		}

		return true;
	}

	public String loadData() {
		if (FileIn) {
			String msg = "Error reading source data from file '" + source + "'";
			try {
				srcData = FileUtils.readFileToString(new File(source));
			} catch (IOException e) {
				Log.error(this, msg);
			}
		} else if (StdIO && !TextIn) {
			String msg = "Error reading source data from standard in";
			InputStream in = System.in;
			try {
				srcData = IOUtils.toString(System.in);
			} catch (IOException e) {
				Log.error(this, msg);
			} finally {
				IOUtils.closeQuietly(in);
			}
		} else if (TextIn) {
			StdIO = true;
		}
		return srcData;
	}

	public void storeData(String data) {
		if (FileOut) {
			try {
				FileUtils.writeStringToFile(new File(target), data);
			} catch (IOException e) {
				Log.error(this, "Error writing result data to file '" + target + "'", e);
			}
		} else if (StdIO) {
			OutputStream out = System.out;
			try {
				IOUtils.write(data, out);
			} catch (IOException e) {
				Log.error(this, "Error writing result data to standard out", e);
			} finally {
				IOUtils.closeQuietly(out);
			}
		}
	}

	@SuppressWarnings("unused")
	private String cwd() {
		String cwd = "";
		try {
			cwd = new File(".").getCanonicalPath();
		} catch (IOException e) {}
		return cwd;
	}

	@SuppressWarnings("unused")
	private String readFile(String filename) {
		File file = new File(filename);
		String data = null;
		try {
			data = FileUtils.readFileToString(file);
		} catch (IOException e) {
			Log.error(this, "Error reading file", e);
		}
		return data;

	}

	private void printHelp() {
		System.out.println("Usage:");
		System.out.println("java -jar [cli_options]" + System.lineSeparator());
		// etc.
		System.exit(0);
	}
}
// IOProcessor ==========
