package de.qabel.core.storage;

import de.qabel.core.exceptions.QblStorageException;

import java.io.InputStream;

abstract class StorageWriteBackend {
	abstract void upload(String name, InputStream inputStream) throws QblStorageException;
	abstract void delete(String name) throws QblStorageException;
}
