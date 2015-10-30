package de.qabel.core.storage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class DirectoryMetadataTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testInitDatabase() {
		DirectoryMetadata dm = DirectoryMetadata.newDatabase();
		byte[] version = dm.getVersion();
		assertThat(dm.listFiles().size(), is(0));
		assertThat(dm.listFolders().size(), is(0));
		assertThat(dm.listExternals().size(), is(0));
		dm.commit();
		assertThat(dm.getVersion(), is(not(equalTo(version))));

	}
}