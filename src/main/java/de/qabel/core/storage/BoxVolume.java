package de.qabel.core.storage;

import com.amazonaws.auth.AWSCredentials;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.DecryptedPlaintext;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.exceptions.QblStorageException;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.crypto.InvalidCipherTextException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

public class BoxVolume {

	StorageReadBackend readBackend;
	StorageWriteBackend writeBackend;

	private QblECKeyPair keyPair;
	private byte[] deviceId;
	private CryptoUtils cryptoUtils;

	public BoxVolume(String bucket, String prefix, AWSCredentials credentials,
	                 QblECKeyPair keyPair, byte[] deviceId) {
		this.keyPair = keyPair;
		this.deviceId = deviceId;
		readBackend = new S3ReadBackend(bucket, prefix);
		writeBackend = new S3WriteBackend(credentials, bucket, prefix);
		cryptoUtils = new CryptoUtils();
	}

	public BoxVolume(StorageReadBackend readBackend, StorageWriteBackend writeBackend,
	                 QblECKeyPair keyPair, byte[] deviceId) {
		this.keyPair = keyPair;
		this.deviceId = deviceId;
		this.readBackend = readBackend;
		this.writeBackend = writeBackend;
		cryptoUtils = new CryptoUtils();
	}

	public BoxNavigation navigate() throws QblStorageException {
		String rootRef = getRootRef();
		InputStream indexDl = readBackend.download(rootRef);
		Path tmp;
		try {
			byte[] encrypted = IOUtils.toByteArray(indexDl);
			DecryptedPlaintext plaintext = cryptoUtils.readBox(keyPair, encrypted);
			// Should work fine for the small metafiles
			tmp = Files.createTempFile(null, null);
			OutputStream out = Files.newOutputStream(tmp);
			out.write(plaintext.getPlaintext());
			out.close();
		} catch (IOException | InvalidCipherTextException | InvalidKeyException e) {
			throw new QblStorageException(e);
		}
		DirectoryMetadata dm = DirectoryMetadata.openDatabase(tmp, deviceId, rootRef);
		return new IndexNavigation(dm, keyPair, deviceId, readBackend, writeBackend);
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

	public void createIndex(String bucket, String prefix) throws QblStorageException {
		createIndex("https://" + bucket + ".s3.amazonaws.com/" + prefix);
	}

	public void createIndex(String root) throws QblStorageException {
		String rootRef = getRootRef();
		DirectoryMetadata dm = DirectoryMetadata.newDatabase(root, deviceId);
		try {
			byte[] plaintext = Files.readAllBytes(dm.path);
			byte[] encrypted = cryptoUtils.createBox(keyPair, keyPair.getPub(), plaintext, 0);
			writeBackend.upload(rootRef, new ByteArrayInputStream(encrypted));
		} catch (IOException e) {
			throw new QblStorageException(e);
		} catch (InvalidKeyException e) {
			throw new QblStorageException(e);
		}


	}
}
