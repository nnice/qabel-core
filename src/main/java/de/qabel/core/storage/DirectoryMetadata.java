package de.qabel.core.storage;

import java.util.List;

class DirectoryMetadata {
	static DirectoryMetadata newDatabase() {
		return new DirectoryMetadata();
	}

	List<BoxFile> listFiles() {
		return null;
	}

	List<BoxFolder> listFolders() {
		return null;
	}

	List<BoxExternal> listExternals() {
		return null;
	}

	byte[] getVersion() {
		return new byte[] {1};
	}

	void commit() {
	}
}
