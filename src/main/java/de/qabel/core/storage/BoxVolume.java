package de.qabel.core.storage;

import com.amazonaws.auth.AWSCredentials;
import de.qabel.core.crypto.QblECKeyPair;

import java.util.HashMap;
import java.util.Map;

public class BoxVolume {

	Map<String, DirectoryMetadata> cache = new HashMap<>();
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


}
