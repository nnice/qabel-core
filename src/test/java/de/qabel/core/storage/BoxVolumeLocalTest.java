package de.qabel.core.storage;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BoxVolumeLocalTest extends BoxVolumeTest {

	private Path tempFolder;

	@Override
	public void setUpVolume() throws IOException {
		tempFolder = Files.createTempDirectory("");

		volume = new BoxVolume(new LocalReadBackend(tempFolder),
				new LocalWriteBackend(tempFolder),
				keyPair, deviceID);
		volume2 = new BoxVolume(new LocalReadBackend(tempFolder),
				new LocalWriteBackend(tempFolder),
				keyPair, deviceID2);
	}

	@Override
	protected void cleanVolume() throws IOException {
		FileUtils.deleteDirectory(tempFolder.toFile());
	}
}
