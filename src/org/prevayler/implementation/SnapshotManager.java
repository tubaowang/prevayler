// Prevayler(TM) - The Open-Source Prevalence Layer.
// Copyright (C) 2001 Klaus Wuestefeld.
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License version 2.1 as published by the Free Software Foundation. This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.

package org.prevayler.implementation;

import java.io.*;
import org.prevayler.foundation.*;


/** Writes and reads snapshots to/from files. You can extend this class to use a serialization mechanism other than Java's. E.g: XML.
 */
public class SnapshotManager {

	private final File directory;


	public SnapshotManager() throws IOException {
		this("PrevalenceBase");
	}


	/** @param snapshotDirectoryName The path of the directory where the last snapshot file will be read and where the new snapshot files will be created.
	*/
	public SnapshotManager(String snapshotDirectoryName) throws IOException {
		directory = FileManager.produceDirectory(snapshotDirectoryName);
	}


	void writeSnapshot(Object prevalentSystem, long version) throws IOException {
		File tempFile = File.createTempFile("snapshot" + version + "temp", "generatingSnapshot", directory);

		writeSnapshot(prevalentSystem, tempFile);

		File permanent = snapshotFile(version);
		permanent.delete();
		if (!tempFile.renameTo(permanent)) throw new IOException("Temporary snapshot file generated: " + tempFile + "\nUnable to rename it permanently to: " + permanent);
	}


	/** Serializes prevalentSystem and writes it to snapshotFile. You can overload this method to use a serialization mechanism other than Java's. E.g: XML.
	*/
	protected void writeSnapshot(Object prevalentSystem, File snapshotFile) throws IOException {
		SimpleOutputStream stream = new SimpleOutputStream(snapshotFile);
		stream.writeObject(prevalentSystem);
		stream.close();
	}


	/** Returns "snapshot", the default suffix/extension for snapshot files. You can overload this method and return a different suffix if you want. E.g: "XmlSnapshot"
	*/
	protected String suffix() {
		return "snapshot";
	}


	/** Returns zero if no snapshot file was found.
	*/
	long latestVersion() throws IOException {
		String[] fileNames = directory.list();
		if (fileNames == null) throw new IOException("Error reading file list from directory " + directory);

		long result = 0;
		for (int i = 0; i < fileNames.length; i++) {
			long candidate = version(fileNames[i]);
			if (candidate > result) result = candidate;
		}
		return result;
	}


	Object readSnapshot(Object initialVersion, long version) throws ClassNotFoundException, IOException {
		if (version == 0) return initialVersion;

		File snapshotFile = snapshotFile(version);
		return readSnapshot(snapshotFile);
	}


	/** Deserializes and returns the object contained in snapshotFile. You can overload this method to use a deserialization mechanism other than Java's. E.g: XML.
	*/
	protected Object readSnapshot(File snapshotFile) throws ClassNotFoundException, IOException {
		SimpleInputStream ois = new SimpleInputStream(snapshotFile);
		try {	return ois.readObject(); } finally { ois.close(); }
	}


	private File snapshotFile(long version) {
		String fileName = "0000000000000000000" + version;
		return new File(directory, fileName.substring(fileName.length() - 19) + "." + suffix());
	}


	/** Returns -1 if fileName is not the name of a snapshot file.
	*/
	private long version(String fileName) {
		if (!fileName.endsWith("." + suffix())) return -1;
		return Long.parseLong(fileName.substring(0, fileName.indexOf("." + suffix())));    // "00000.snapshot" becomes "00000".
	}

}