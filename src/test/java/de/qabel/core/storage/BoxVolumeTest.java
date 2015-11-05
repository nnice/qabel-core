package de.qabel.core.storage;

import com.amazonaws.util.IOUtils;
import de.qabel.core.exceptions.QblStorageException;
import de.qabel.core.exceptions.QblStorageNotFound;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.meanbean.util.AssertionUtils.fail;

public abstract class BoxVolumeTest {
	private static final Logger logger = LoggerFactory.getLogger(BoxVolumeTest.class.getName());

	BoxVolume volume;
	byte[] deviceID;
	final String bucket = "qabel";
	String prefix = UUID.randomUUID().toString();
	private final String testFileName = "src/test/java/de/qabel/core/crypto/testFile";

	@Before
	public void setUp() throws IOException, QblStorageException {
		UUID uuid = UUID.randomUUID();
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		deviceID = bb.array();

		setUpVolume();

		volume.createIndex(bucket, prefix);
	}

	abstract void setUpVolume() throws IOException;

	@After
	public void cleanUp() throws IOException {
		cleanVolume();
	}

	protected abstract void cleanVolume() throws IOException;

	@Test
	public void testCreateIndex() throws QblStorageException {
		BoxNavigation nav = volume.navigate();
		assertThat(nav.listFiles().size(), is(0));
	}

	@Test
	public void testUploadFile() throws QblStorageException, IOException {
		uploadFile(volume.navigate());
	}

	@Test(expected = QblStorageNotFound.class)
	public void testDeleteFile() throws QblStorageException, IOException {
		BoxNavigation nav = volume.navigate();
		BoxFile boxFile = uploadFile(nav);
		nav.delete(boxFile);
		nav.commit();
		nav.download(boxFile);
	}

	private BoxFile uploadFile(BoxNavigation nav) throws QblStorageException, IOException {
		File file = new File(testFileName);
		BoxFile boxFile = nav.upload("foobar", file);
		nav.commit();
		BoxNavigation nav_new = volume.navigate();
		checkFile(boxFile, nav_new);
		return boxFile;
	}

	private void checkFile(BoxFile boxFile, BoxNavigation nav_new) throws QblStorageException, IOException {
		InputStream dlStream = nav_new.download(boxFile);
		assertNotNull("Download stream is null", dlStream);
		byte[] dl = IOUtils.toByteArray(dlStream);
		File file = new File(testFileName);
		assertThat(dl, is(Files.readAllBytes(file.toPath())));
	}

	@Test
	public void testCreateFolder() throws QblStorageException, IOException {
		BoxNavigation nav = volume.navigate();
		BoxFolder boxFolder = nav.createFolder("foobdir");
		nav.commit();

		BoxNavigation folder = nav.navigate(boxFolder);
		assertNotNull(folder);
		BoxFile boxFile = uploadFile(folder);

		BoxNavigation folder_new = nav.navigate(boxFolder);
		checkFile(boxFile, folder_new);

		BoxNavigation nav_new = volume.navigate();
		List<BoxFolder> folders = nav_new.listFolders();
		assertThat(folders.size(), is(1));
		assertThat(boxFolder, equalTo(folders.get(0)));
	}

	@Test
	public void testDeleteFolder() throws QblStorageException, IOException {
		BoxNavigation nav = volume.navigate();
		BoxFolder boxFolder = nav.createFolder("foobdir");
		nav.commit();

		BoxNavigation folder = nav.navigate(boxFolder);
		BoxFile boxFile = uploadFile(folder);
		BoxFolder subfolder = folder.createFolder("subfolder");
		folder.commit();

		nav.delete(boxFolder);
		nav.commit();
		BoxNavigation nav_after = volume.navigate();
		assertThat(nav_after.listFolders().isEmpty(), is(true));
		checkDeleted(boxFolder, subfolder, boxFile, nav_after);
	}

	private void checkDeleted(BoxFolder boxFolder, BoxFolder subfolder, BoxFile boxFile, BoxNavigation nav) throws QblStorageException {
		try {
			nav.download(boxFile);
			fail("Could download file in deleted folder");
		} catch (QblStorageNotFound e) { }
		try {
			nav.navigate(boxFolder);
			fail("Could navigate to deleted folder");
		} catch (QblStorageNotFound e) { }
		try {
			nav.navigate(subfolder);
			fail("Could navigate to deleted subfolder");
		} catch (QblStorageNotFound e) { }
	}

}