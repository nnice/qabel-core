package de.qabel.core.storage;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.exceptions.QblStorageException;
import de.qabel.core.exceptions.QblStorageNotFound;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class BoxVolumeTest {
	private final static Logger logger = LoggerFactory.getLogger(BoxVolumeTest.class.getName());

	BoxVolume volume;
	private byte[] deviceID;
	private String bucket = "qabel";
	String prefix = UUID.randomUUID().toString();
	private BoxVolume localVolume;

	@Before
	public void setUp() throws IOException {
		UUID uuid = UUID.randomUUID();
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		deviceID = bb.array();

		DefaultAWSCredentialsProviderChain chain = new DefaultAWSCredentialsProviderChain();

		volume = new BoxVolume(bucket,prefix, chain.getCredentials(), new QblECKeyPair(), deviceID);

		Path tempFolder = Files.createTempDirectory("");

		localVolume = new BoxVolume(new LocalReadBackend(tempFolder),
				new LocalWriteBackend(tempFolder),
				new QblECKeyPair(), deviceID);
	}

	@After
	public void cleanUp() {
		AmazonS3Client client = ((S3WriteBackend) volume.writeBackend).s3Client;
		ObjectListing listing = client.listObjects(bucket, prefix);
		List<DeleteObjectsRequest.KeyVersion> keys = new ArrayList<>();
		for (S3ObjectSummary summary: listing.getObjectSummaries()) {
			keys.add(new DeleteObjectsRequest.KeyVersion(summary.getKey()));
		}
		DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket);
		deleteObjectsRequest.setKeys(keys);
		client.deleteObjects(deleteObjectsRequest);
	}


	@Test
	public void testShareManagement() {
		fail("Not implemented");
	}

	@Test(expected = QblStorageNotFound.class)
	public void testIndexNotFound() throws QblStorageException {
		String root = volume.getRootRef();
		assertThat(UUID.fromString(root), isA(UUID.class));
		BoxNavigation nav = volume.navigate();
	}

	@Test
	public void testCreateIndex() throws QblStorageException {
		volume.createIndex(bucket, prefix);
		BoxNavigation nav = volume.navigate();
	}

}