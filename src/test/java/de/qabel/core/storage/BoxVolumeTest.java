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

public abstract class BoxVolumeTest {
	private static final Logger logger = LoggerFactory.getLogger(BoxVolumeTest.class.getName());

	BoxVolume volume;
	byte[] deviceID;
	final String bucket = "qabel";
	String prefix = UUID.randomUUID().toString();
	private final String testFileName = "src/test/java/de/qabel/core/crypto/testFile";

	@Before
	public void setUp() throws IOException {
		UUID uuid = UUID.randomUUID();
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		deviceID = bb.array();

		setUpVolume();
	}

	abstract void setUpVolume() throws IOException;

	@After
	public void cleanUp() throws IOException {
		cleanVolume();
	}

	protected abstract void cleanVolume() throws IOException;

	@Test(expected = QblStorageNotFound.class)
	public void testIndexNotFound() throws QblStorageException {
		String root = volume.getRootRef();
		assertThat(UUID.fromString(root), isA(UUID.class));
		volume.navigate();
	}

	@Test
	public void testCreateIndex() throws QblStorageException {
		volume.createIndex(bucket, prefix);
		BoxNavigation nav = volume.navigate();
		assertThat(nav.listFiles().size(), is(0));
	}

	@Test
	public void testUploadFile() throws QblStorageException, IOException {
		volume.createIndex(bucket, prefix);
		BoxNavigation nav = volume.navigate();
		File file = new File(testFileName);
		nav.upload("foobar", file);
		nav.commit();
		BoxNavigation nav_new = volume.navigate();
		InputStream dlStream = nav_new.download("foobar");
		assertNotNull("Download stream is null", dlStream);
		byte[] dl = IOUtils.toByteArray(dlStream);
		assertThat(dl, is(Files.readAllBytes(file.toPath())));
	}

	@Test
	public void testCreateFolder() throws QblStorageException, IOException {
		volume.createIndex(bucket, prefix);
		BoxNavigation nav = volume.navigate();
		File file = new File(testFileName);
		BoxFolder boxFolder = nav.createFolder("foobdir");
		nav.commit();
		BoxNavigation folder = nav.navigate(boxFolder);
		assertNotNull(folder);
		folder.upload("foobar", file);
		folder.commit();
		BoxNavigation folder_new = nav.navigate(boxFolder);
		InputStream dlStream = folder_new.download("foobar");
		assertNotNull("Download stream is null", dlStream);
		byte[] dl = IOUtils.toByteArray(dlStream);
		assertThat(dl, is(Files.readAllBytes(file.toPath())));
		BoxNavigation nav_new = volume.navigate();
		List<BoxFolder> folders = nav_new.listFolders();
		assertThat(folders.size(), is(1));
		assertThat(boxFolder, equalTo(folders.get(0)));
	}

}