package de.qabel.core.storage;

import de.qabel.core.exceptions.QblStorageException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

class LocalReadBackend extends StorageReadBackend {
	private Path root;


	LocalReadBackend(Path root) {
		this.root = root;
	}


	InputStream download(String name) throws QblStorageException {
		Path file = root.resolve(name);
		try {
			return Files.newInputStream(file);
		} catch (IOException e) {
			throw new QblStorageException(e);
		}
	}

}
