package de.qabel.core.storage;

import de.qabel.core.exceptions.QblStorageException;
import de.qabel.core.module.ModuleManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class DirectoryMetadata {
	private final static Logger logger = LoggerFactory.getLogger(DirectoryMetadata.class.getName());
	private final static String JDBC_CLASS_NAME = "org.sqlite.JDBC";
	private final static String JDBC_PREFIX = "jdbc:sqlite:";
	private Connection connection;

	public DirectoryMetadata(Connection connection) {
		this.connection = connection;
	}

	static DirectoryMetadata newDatabase() {
		Connection connection;
		try {
			Class.forName(JDBC_CLASS_NAME);
			connection = DriverManager.getConnection(JDBC_PREFIX + ":memory:");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot load JDBC class!", e);
		} catch (SQLException e) {
			throw new RuntimeException("Cannot load in-memory database!", e);
		}
		DirectoryMetadata dm = new DirectoryMetadata(connection);
		try {
			dm.initDatabase();
		} catch (SQLException e) {
			throw new RuntimeException("Cannot init the database", e);
		}
		return dm;
	}

	private void initDatabase() throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.executeUpdate(initSql);
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

	List<BoxFolder> listFolders() {
		return new ArrayList<>();
	}

	List<BoxExternal> listExternals() {
		return new ArrayList<>();
	}

	byte[] getVersion() {
		return new byte[]{1};
	}

	void commit() {
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


	private String initSql =
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
}

