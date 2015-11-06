package de.qabel.core.storage;

import com.amazonaws.util.IOUtils;
import de.qabel.core.crypto.DecryptedPlaintext;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.exceptions.QblStorageException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;

public class IndexNavigation extends AbstractNavigation {

	private static final Logger logger = LoggerFactory.getLogger(IndexNavigation.class.getName());

	IndexNavigation(DirectoryMetadata dm, QblECKeyPair keyPair, byte[] deviceId,
	                StorageReadBackend readBackend, StorageWriteBackend writeBackend) {
		super(dm, keyPair, deviceId, readBackend, writeBackend);
	}

	@Override
	protected DirectoryMetadata reloadMetadata() throws QblStorageException {
		// TODO: duplicate with BoxVoume.navigate()
		String rootRef = dm.getFileName();
		InputStream indexDl = readBackend.download(rootRef);
		Path tmp;
		try {
			byte[] encrypted = IOUtils.toByteArray(indexDl);
			DecryptedPlaintext plaintext = cryptoUtils.readBox(keyPair, encrypted);
			tmp = Files.createTempFile(null, null);
			logger.info("Using " + tmp.toString() + " for the metadata file");
			OutputStream out = Files.newOutputStream(tmp);
			out.write(plaintext.getPlaintext());
			out.close();
		} catch (IOException | InvalidCipherTextException | InvalidKeyException e) {
			throw new QblStorageException(e);
		}
		return DirectoryMetadata.openDatabase(tmp, deviceId, rootRef);
	}

	@Override
	protected void uploadDirectoryMetadata() throws QblStorageException {
		try {
			byte[] plaintext = Files.readAllBytes(dm.path);
			byte[] encrypted = cryptoUtils.createBox(keyPair, keyPair.getPub(), plaintext, 0);
			writeBackend.upload(dm.getFileName(), new ByteArrayInputStream(encrypted));
			logger.info("Uploading metadata file with name " + dm.getFileName());
		} catch (IOException | InvalidKeyException e) {
			throw new QblStorageException(e);
		}
	}
}
