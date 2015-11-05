package de.qabel.core.storage;

import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.exceptions.QblStorageException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class FolderNavigation extends AbstractNavigation {
	private final byte[] key;

	FolderNavigation(DirectoryMetadata dm, QblECKeyPair keyPair, byte[] key, byte[] deviceId,
	                 StorageReadBackend readBackend, StorageWriteBackend writeBackend) {
		super(dm, keyPair, deviceId, readBackend, writeBackend);
		this.key = key;
	}

	@Override
	public void commit() throws QblStorageException {
		dm.commit();
		SecretKey secretKey = new SecretKeySpec(key, "AES");
		uploadEncrypted(dm.getPath().toFile(), secretKey, dm.getFileName());
	}
}
