package de.qabel.core.storage;

import de.qabel.core.exceptions.QblStorageException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

class LocalWriteBackend extends StorageWriteBackend {
	private Path root;


	LocalWriteBackend(Path root) {
		this.root = root;
	}

	@Override
	void upload(String name, InputStream inputStream) throws QblStorageException {
		Path file = root.resolve(name);
		try {
			OutputStream output = Files.newOutputStream(file);
			output.write(IOUtils.toByteArray(inputStream));
		} catch (IOException e) {
			throw new QblStorageException(e);
		}
	}

	@Override
	void delete(String name) throws QblStorageException {
		Path file = root.resolve(name);
		try {
			Files.delete(file);
		} catch (IOException e) {
			throw new QblStorageException(e);
		}
	}
}
