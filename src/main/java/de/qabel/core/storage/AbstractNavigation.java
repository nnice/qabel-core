package de.qabel.core.storage;

import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.exceptions.QblStorageException;
import de.qabel.core.exceptions.QblStorageNotFound;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.util.List;
import java.util.UUID;

public abstract class AbstractNavigation implements BoxNavigation {

	private static final Logger logger = LoggerFactory.getLogger(AbstractNavigation.class.getName());

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
	public BoxNavigation navigate(BoxFolder target) throws QblStorageException {
		Path tmp;
		try {
			InputStream indexDl = readBackend.download(target.ref);
			tmp = Files.createTempFile(null, null);
			SecretKey key = makeKey(target.key);
			if (cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(indexDl, tmp.toFile(), key)) {
				DirectoryMetadata dm = DirectoryMetadata.openDatabase(tmp, deviceId, target.ref);
				return new FolderNavigation(dm, keyPair, target.key, deviceId, readBackend, writeBackend);
			} else {
				throw new QblStorageNotFound("Invalid key");
			}
		} catch (IOException | InvalidKeyException e) {
			throw new QblStorageException(e);
		}
	}

	private SecretKey makeKey(byte[] key2) {
		return new SecretKeySpec(key2, "AES");
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
	public List<BoxFolder> listFolders() throws QblStorageException {
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
		// Overwrite = delete old file, upload new file
		BoxFile oldFile = dm.getFile(name);
		if (oldFile != null) {
			writeBackend.delete(oldFile.block);
			dm.deleteFile(oldFile);
		}
		dm.insertFile(boxFile);
		return boxFile;
	}

	protected long uploadEncrypted(File file, SecretKey key, String block) throws QblStorageException {
		try {
			Path tempFile = Files.createTempFile("", "");
			OutputStream outputStream = Files.newOutputStream(tempFile);
			if (!cryptoUtils.encryptFileAuthenticatedSymmetric(file, outputStream, key)) {
				throw new QblStorageException("Encryption failed");
			}
			outputStream.flush();
			return writeBackend.upload(block, Files.newInputStream(tempFile));
		} catch (IOException | InvalidKeyException e) {
			throw new QblStorageException(e);
		}
	}

	@Override
	public InputStream download(BoxFile boxFile) throws QblStorageException {
		InputStream download = readBackend.download(boxFile.block);
		File temp;
		SecretKey key = makeKey(boxFile.key);
		try {
			temp = Files.createTempFile("", "").toFile();
			if (!cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(download, temp, key)) {
				throw new QblStorageException("Decryption failed");
			}
			return Files.newInputStream(temp.toPath());
		} catch (IOException e) {
			throw new QblStorageException(e);
		} catch (InvalidKeyException e) {
			throw new QblStorageException(e);
		}
	}

	@Override
	public BoxFolder createFolder(String name) throws QblStorageException {
		DirectoryMetadata dm = DirectoryMetadata.newDatabase(null, deviceId);
		SecretKey secretKey = cryptoUtils.generateSymmetricKey();
		BoxFolder folder = new BoxFolder(dm.getFileName(), name, secretKey.getEncoded());
		this.dm.insertFolder(folder);
		BoxNavigation newFolder = new FolderNavigation(dm, keyPair, secretKey.getEncoded(),
				deviceId, readBackend, writeBackend);
		newFolder.commit();
		return folder;
	}

	@Override
	public void delete(BoxFile file) throws QblStorageException {
		dm.deleteFile(file);
		writeBackend.delete(file.block);
	}

	@Override
	public void delete(BoxFolder folder) throws QblStorageException {
		BoxNavigation folderNav = navigate(folder);
		for (BoxFile file: folderNav.listFiles()) {
			logger.info("Deleting file " + file.name);
			folderNav.delete(file);
		}
		for (BoxFolder subFolder: folderNav.listFolders()) {
			logger.info("Deleting folder " + folder.name);
			folderNav.delete(subFolder);
		}
		dm.deleteFolder(folder);
		writeBackend.delete(folder.ref);
	}

	@Override
	public void delete(BoxExternal external) throws QblStorageException {

	}
}
