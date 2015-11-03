package de.qabel.core.storage;

import java.io.InputStream;
import java.util.List;

public interface BoxNavigation {

	BoxNavigation navigate(BoxFolder target);
	BoxNavigation navigate(BoxExternal target);

	List<BoxFile> listFiles();
	List<BoxFolder> listFolder();
	List<BoxExternal> listExternals();

	BoxFile upload(String name, InputStream content);
	InputStream download(BoxFile file);

	void delete(BoxFile file);
	void delete(BoxFolder folder);
	void delete(BoxExternal external);

}
