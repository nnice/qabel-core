package de.qabel.core.storage;

import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.exceptions.QblStorageException;

import java.io.InputStream;
import java.util.List;

public class FolderNavigation implements BoxNavigation {

	DirectoryMetadata dm;
	QblECKeyPair keyPair;
	byte[] deviceId;

	FolderNavigation(DirectoryMetadata dm, QblECKeyPair keyPair, byte[] deviceId) {
		this.dm = dm;
		this.keyPair = keyPair;
		this.deviceId = deviceId;
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
	public BoxFile upload(String name, InputStream content) throws QblStorageException {
		return null;
	}

	@Override
	public InputStream download(BoxFile file) throws QblStorageException {
		return null;
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
