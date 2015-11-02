package de.qabel.core.storage;

import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.exceptions.QblStorageException;
import de.qabel.core.module.ModuleManager;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class DirectoryMetadata {
	private final static Logger logger = LoggerFactory.getLogger(DirectoryMetadata.class.getName());
	private final static String JDBC_CLASS_NAME = "org.sqlite.JDBC";
	private final static String JDBC_PREFIX = "jdbc:sqlite:";
	private Connection connection;
	private byte[] deviceId;


	private final String initSql =
			"CREATE TABLE meta ("
					+ " name VARCHAR(24) PRIMARY KEY,"
					+ " value TEXT );"
					+ "CREATE TABLE spec_version ("
					+ " version INTEGER PRIMARY KEY );"
					+ "CREATE TABLE version ("
					+ " id INTEGER PRIMARY KEY,"
					+ " version BLOB NOT NULL,"
					+ " time LONG NOT NULL );"
					+ "CREATE TABLE shares ("
					+ " id INTEGER PRIMARY KEY,"
					+ " ref VARCHAR(255)NOT NULL,"
					+ " recipient BLOB NOT NULL,"
					+ " type INTEGER NOT NULL );"
					+ "CREATE TABLE files ("
					+ " id INTEGER PRIMARY KEY,"
					+ " block VARCHAR(255)NOT NULL,"
					+ " name VARCHAR(255)NOT NULL,"
					+ " size LONG NOT NULL,"
					+ " mtime LONG NOT NULL,"
					+ " key BLOB NOT NULL );"
					+ "CREATE TABLE folders ("
					+ " id INTEGER PRIMARY KEY,"
					+ " ref VARCHAR(255)NOT NULL,"
					+ " name VARCHAR(255)NOT NULL,"
					+ " key BLOB NOT NULL );"
					+ "CREATE TABLE externals ("
					+ " id INTEGER PRIMARY KEY,"
					+ " owner BLOB NOT NULL,"
					+ " name VARCHAR(255)NOT NULL,"
					+ " key BLOB NOT NULL,"
					+ " url TEXT NOT NULL )";


	public DirectoryMetadata(Connection connection, byte[] deviceId) {
		this.connection = connection;
		this.deviceId = deviceId;
	}

	static DirectoryMetadata newDatabase(byte[] deviceId) throws QblStorageException {
		Connection connection;
		try {
			Class.forName(JDBC_CLASS_NAME);
			connection = DriverManager.getConnection(JDBC_PREFIX + ":memory:");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot load JDBC class!", e);
		} catch (SQLException e) {
			throw new RuntimeException("Cannot load in-memory database!", e);
		}
		DirectoryMetadata dm = new DirectoryMetadata(connection, deviceId);
		try {
			dm.initDatabase();
		} catch (SQLException e) {
			throw new RuntimeException("Cannot init the database", e);
		}
		return dm;
	}

	private void initDatabase() throws SQLException, QblStorageException {
		try (Statement statement = connection.createStatement()) {
			statement.executeUpdate(initSql);
		}
		try (PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO version (version, time) VALUES (?, ?)")) {
			statement.setBytes(1, initVersion());
			statement.setLong(2, DateTimeUtils.currentTimeMillis());
			statement.executeUpdate();
		}
	}

	private byte[] initVersion() throws QblStorageException {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new QblStorageException(e);
		}
		md.update(new byte[] {0, 0});
		md.update(deviceId);
		return md.digest();
	}

	byte[] getVersion() throws QblStorageException {
		try (Statement statement = connection.createStatement()) {
			try (ResultSet rs = statement.executeQuery(
					"SELECT version FROM version ORDER BY id DESC LIMIT 1")) {
				if (rs.next()) {
					return rs.getBytes(1);
				} else {
					throw new QblStorageException("No version found!");
				}
			}
		} catch (SQLException e) {
			throw new QblStorageException(e);
		}
	}

	void commit() throws QblStorageException {
		byte[] oldVersion = getVersion();
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new QblStorageException(e);
		}
		md.update(new byte[] {0, 1});
		md.update(oldVersion);
		md.update(deviceId);
		try (PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO version (version, time) VALUES (?, ?)")) {
			statement.setBytes(1, md.digest());
			statement.setLong(2, DateTimeUtils.currentTimeMillis());
			if (statement.executeUpdate() != 1) {
				throw new QblStorageException("Could not update version!");
			}
		} catch (SQLException e) {
			throw new QblStorageException(e);
		}
	}


	List<BoxFile> listFiles() throws QblStorageException {
		try (Statement statement = connection.createStatement()) {
			try (ResultSet rs = statement.executeQuery(
					"SELECT block, name, size, mtime, key FROM files")) {
				List<BoxFile> files = new ArrayList<>();
				while (rs.next()) {
					files.add(new BoxFile(rs.getString(1),
							rs.getString(2), rs.getLong(3), rs.getLong(4), rs.getBytes(5)));
				}
				return files;
			}
		} catch (SQLException e) {
			throw new QblStorageException(e);
		}
	}


	void insertFile(BoxFile file) throws QblStorageException {
		try {
			PreparedStatement st = connection.prepareStatement(
					"INSERT INTO files (block, name, size, mtime, key) VALUES(?, ?, ?, ?, ?)");
			st.setString(1, file.block);
			st.setString(2, file.name);
			st.setLong(3, file.size);
			st.setLong(4, file.mtime);
			st.setBytes(5, file.key);
			if (st.executeUpdate() != 1) {
				throw new QblStorageException("Failed to insert file");
			}

		} catch (SQLException e) {
			throw new QblStorageException(e);
		}
	}

	void deleteFile(BoxFile file) throws QblStorageException {
		try {
			PreparedStatement st = connection.prepareStatement(
					"DELETE FROM files WHERE name=?");
			st.setString(1, file.name);
			if (st.executeUpdate() != 1) {
				throw new QblStorageException("Failed to insert file");
			}

		} catch (SQLException e) {
			throw new QblStorageException(e);
		}
	}
	public void insertFolder(BoxFolder folder) throws QblStorageException {
		try {
			PreparedStatement st = connection.prepareStatement(
					"INSERT INTO folders (ref, name, key) VALUES(?, ?, ?)");
			st.setString(1, folder.ref);
			st.setString(2, folder.name);
			st.setBytes(3, folder.key);
			if (st.executeUpdate() != 1) {
				throw new QblStorageException("Failed to insert folder");
			}

		} catch (SQLException e) {
			throw new QblStorageException(e);
		}
	}

	public void deleteFolder(BoxFolder folder) throws QblStorageException {
		try {
			PreparedStatement st = connection.prepareStatement(
					"DELETE FROM folders WHERE name=?");
			st.setString(1, folder.name);
			if (st.executeUpdate() != 1) {
				throw new QblStorageException("Failed to insert folder");
			}

		} catch (SQLException e) {
			throw new QblStorageException(e);
		}
	}

	List<BoxFolder> listFolders() throws QblStorageException {
		try (Statement statement = connection.createStatement()) {
			try (ResultSet rs = statement.executeQuery(
					"SELECT ref, name, key FROM folders")) {
				List<BoxFolder> folders = new ArrayList<>();
				while (rs.next()) {
					folders.add(new BoxFolder(rs.getString(1), rs.getString(2), rs.getBytes(3)));
				}
				return folders;
			}
		} catch (SQLException e) {
			throw new QblStorageException(e);
		}
	}

	public void insertExternal(BoxExternal external) throws QblStorageException {
		try {
			PreparedStatement st = connection.prepareStatement(
					"INSERT INTO externals (url, name, owner, key) VALUES(?, ?, ?, ?)");
			st.setString(1, external.url);
			st.setString(2, external.name);
			st.setBytes(3, external.owner.getKey());
			st.setBytes(4, external.key);
			if (st.executeUpdate() != 1) {
				throw new QblStorageException("Failed to insert external");
			}

		} catch (SQLException e) {
			throw new QblStorageException(e);
		}
	}

	public void deleteExternal(BoxExternal external) throws QblStorageException {
		try {
			PreparedStatement st = connection.prepareStatement(
					"DELETE FROM externals WHERE name=?");
			st.setString(1, external.name);
			if (st.executeUpdate() != 1) {
				throw new QblStorageException("Failed to insert external");
			}

		} catch (SQLException e) {
			throw new QblStorageException(e);
		}
	}

	List<BoxExternal> listExternals() throws QblStorageException {
		try (Statement statement = connection.createStatement()) {
			try (ResultSet rs = statement.executeQuery(
					"SELECT url, name, owner, key FROM externals")) {
				List<BoxExternal> externals = new ArrayList<>();
				while (rs.next()) {
					externals.add(new BoxExternal(rs.getString(1), rs.getString(2),
							new QblECPublicKey(rs.getBytes(3)), rs.getBytes(4)));
				}
				return externals;
			}
		} catch (SQLException e) {
			throw new QblStorageException(e);
		}
	}

}

