package de.qabel.core.storage;

import de.qabel.core.exceptions.QblStorageException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

class LocalWriteBackend extends StorageWriteBackend {

	private static final Logger logger = LoggerFactory.getLogger(LocalReadBackend.class.getName());
	private Path root;


	LocalWriteBackend(Path root) {
		this.root = root;
	}

	@Override
	long upload(String name, InputStream inputStream) throws QblStorageException {
		Path file = root.resolve(name);
		logger.info("Uploading file path " + file.toString());
		try {
			OutputStream output = Files.newOutputStream(file);
			output.write(IOUtils.toByteArray(inputStream));
			return Files.getLastModifiedTime(file).toMillis();
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
