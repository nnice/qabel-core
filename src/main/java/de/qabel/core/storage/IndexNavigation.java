package de.qabel.core.storage;

import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.exceptions.QblStorageException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;

public class IndexNavigation extends AbstractNavigation {

	IndexNavigation(DirectoryMetadata dm, QblECKeyPair keyPair, byte[] deviceId,
	                StorageReadBackend readBackend, StorageWriteBackend writeBackend) {
		super(dm, keyPair, deviceId, readBackend, writeBackend);
	}

	@Override
	public void commit() throws QblStorageException {
		dm.commit();
		try {
			byte[] plaintext = Files.readAllBytes(dm.path);
			byte[] encrypted = cryptoUtils.createBox(keyPair, keyPair.getPub(), plaintext, 0);
			writeBackend.upload(dm.getFileName(), new ByteArrayInputStream(encrypted));
		} catch (IOException | InvalidKeyException e) {
			throw new QblStorageException(e);
		}
	}
}
