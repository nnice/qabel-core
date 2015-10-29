package de.qabel.core.storage;

import de.qabel.core.exceptions.QblStorageException;

import java.io.InputStream;

abstract class StorageReadBackend {

	abstract InputStream download(String name) throws QblStorageException;
}
