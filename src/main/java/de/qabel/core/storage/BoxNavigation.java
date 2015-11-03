package de.qabel.core.storage;

import de.qabel.core.exceptions.QblStorageException;

import java.io.InputStream;
import java.util.List;

public interface BoxNavigation {

	BoxNavigation navigate(BoxFolder target);
	BoxNavigation navigate(BoxExternal target);

	List<BoxFile> listFiles() throws QblStorageException;
	List<BoxFolder> listFolder() throws QblStorageException;
	List<BoxExternal> listExternals() throws QblStorageException;

	BoxFile upload(String name, InputStream content) throws QblStorageException;
	InputStream download(BoxFile file) throws QblStorageException;

	void delete(BoxFile file) throws QblStorageException;
	void delete(BoxFolder folder) throws QblStorageException;
	void delete(BoxExternal external) throws QblStorageException;

}
