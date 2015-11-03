package de.qabel.core.storage;

import com.amazonaws.auth.AWSCredentials;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.exceptions.QblStorageException;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

public class BoxVolume {

	StorageReadBackend readBackend;
	StorageWriteBackend writeBackend;

	private QblECKeyPair keyPair;

	public BoxVolume(String bucket, String prefix, AWSCredentials credentials, QblECKeyPair keyPair) {
		this.keyPair = keyPair;
		readBackend = new S3ReadBackend(bucket, prefix);
		writeBackend = new S3WriteBackend(credentials, bucket, prefix);
	}

	public BoxVolume(StorageReadBackend readBackend, StorageWriteBackend writeBackend,
	                 QblECKeyPair keyPair) {
		this.keyPair = keyPair;
		this.readBackend = readBackend;
		this.writeBackend = writeBackend;
	}

	public BoxNavigation navigate() {
		return null;
	}


	public String getRootRef() throws QblStorageException {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new QblStorageException(e);
		}
		md.update(keyPair.getPrivateKey());
		byte[] digest = md.digest();
		byte[] firstBytes = Arrays.copyOfRange(digest, 0, 16);
		ByteBuffer bb = ByteBuffer.wrap(firstBytes);
		UUID uuid = new UUID(bb.getLong(), bb.getLong());
		return uuid.toString();
	}
}
