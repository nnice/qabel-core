package de.qabel.core.storage;

import de.qabel.core.exceptions.QblStorageException;

import java.io.InputStream;

abstract class StorageWriteBackend {
	abstract long upload(String name, InputStream content) throws QblStorageException;
	abstract void delete(String name) throws QblStorageException;
}
