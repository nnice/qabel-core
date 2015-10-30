package de.qabel.core.storage;

import de.qabel.core.exceptions.QblStorageException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class DirectoryMetadataTest {

	private DirectoryMetadata dm;

	@Before
	public void setUp() throws Exception {
		dm = DirectoryMetadata.newDatabase();
	}

	@Test
	public void testInitDatabase() throws QblStorageException {
		byte[] version = dm.getVersion();
		assertThat(dm.listFiles().size(), is(0));
		assertThat(dm.listFolders().size(), is(0));
		assertThat(dm.listExternals().size(), is(0));
		dm.commit();
		assertThat(dm.getVersion(), is(not(equalTo(version))));

	}

	@Test
	public void testInsertFile() throws QblStorageException {
		BoxFile file = new BoxFile("block", "name", 0l, 0l, new byte[] {1,2,});
		dm.insertFile(file);
		assertThat(dm.listFiles().size(), is(1));
		assertThat(file, equalTo(dm.listFiles().get(0)));
	}
}