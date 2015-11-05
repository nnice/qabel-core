package de.qabel.core.storage;

import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.exceptions.QblStorageException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.util.List;
import java.util.UUID;

public abstract class AbstractNavigation implements BoxNavigation {

	DirectoryMetadata dm;
	QblECKeyPair keyPair;
	byte[] deviceId;
	CryptoUtils cryptoUtils;

	StorageReadBackend readBackend;
	StorageWriteBackend writeBackend;


	AbstractNavigation(DirectoryMetadata dm, QblECKeyPair keyPair, byte[] deviceId,
	                   StorageReadBackend readBackend, StorageWriteBackend writeBackend) {
		this.dm = dm;
		this.keyPair = keyPair;
		this.deviceId = deviceId;
		this.readBackend = readBackend;
		this.writeBackend = writeBackend;
		cryptoUtils = new CryptoUtils();
	}

	@Override
	public BoxNavigation navigate(BoxFolder target) {
		return null;
	}

	@Override
	public BoxNavigation navigate(BoxExternal target) {
		return null;
	}

	@Override
	public List<BoxFile> listFiles() throws QblStorageException {
		return dm.listFiles();
	}

	@Override
	public List<BoxFolder> listFolder() throws QblStorageException {
		return dm.listFolders();
	}

	@Override
	public List<BoxExternal> listExternals() throws QblStorageException {
		return dm.listExternals();
	}

	@Override
	public BoxFile upload(String name, File file) throws QblStorageException {
		SecretKey key = cryptoUtils.generateSymmetricKey();
		String block = UUID.randomUUID().toString();
		BoxFile boxFile = new BoxFile(block, name, file.length(), 0l, key.getEncoded());
		boxFile.mtime = uploadEncrypted(file, key, block);
		dm.insertFile(boxFile);
		return boxFile;
	}

	protected long uploadEncrypted(File file, SecretKey key, String block) throws QblStorageException {
		try {
			Path tempFile = Files.createTempFile("", "");
			OutputStream outputStream = Files.newOutputStream(tempFile);
			cryptoUtils.encryptFileAuthenticatedSymmetric(file, outputStream, key);
			return writeBackend.upload(block, Files.newInputStream(tempFile));
		} catch (IOException e) {
			throw new QblStorageException(e);
		}
		 catch (InvalidKeyException e) {
			 throw new QblStorageException(e);
		 }
	}

	@Override
	public InputStream download(String name) throws QblStorageException {
		BoxFile boxFile = dm.getFile(name);
		InputStream download = readBackend.download(boxFile.block);
		File temp;
		SecretKey key = new SecretKeySpec(boxFile.key, "AES");
		try {
			temp = Files.createTempFile("", "").toFile();
			cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(download, temp, key);
			return Files.newInputStream(temp.toPath());
		} catch (IOException e) {
			throw new QblStorageException(e);
		} catch (InvalidKeyException e) {
			throw new QblStorageException(e);
		}
	}

	@Override
	public void delete(BoxFile file) throws QblStorageException {

	}

	@Override
	public void delete(BoxFolder folder) throws QblStorageException {

	}

	@Override
	public void delete(BoxExternal external) throws QblStorageException {

	}
}
