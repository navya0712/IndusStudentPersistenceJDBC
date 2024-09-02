package com.indus.training.persist.impl;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.indus.training.persist.dao.StudentDao;
import com.indus.training.persist.entity.Student;
import com.indus.training.persist.exceptions.InvalidStudentDataException;
import org.apache.logging.log4j.*;

/**
 * Provides Implementation of the StudentDao interface. This class provides
 * methods to insert, fetch, update, and delete student records.
 */
public class StudentDaoImpl implements StudentDao {

	private static String dbUrl;
	private static String dbUsername;
	private static String dbPassword;
	private static final Logger loggerObj = LogManager.getLogger(StudentDaoImpl.class.getName());

	/**
	 * Constructs a StudentDaoImpl and initializes database connection details. The
	 * database configuration is loaded from the config.properties file.
	 */
	public StudentDaoImpl() {
		loadConfig();
	}

	/**
	 * Loads the database configuration from config.properties. The properties
	 * include database URL, username, and password.
	 * 
	 * @throws RuntimeException if the configuration file is not found or fails to
	 *                          load.
	 */
	private void loadConfig() {
		Properties prop = new Properties();
		try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
			if (input != null) {
				prop.load(input);
				dbUrl = prop.getProperty("db.url", "");
				dbUsername = prop.getProperty("db.username", "");
				dbPassword = prop.getProperty("db.password", "");
				loggerObj.info("StudentDaoImpl initialized with database URL: " + dbUrl);
			} else {
				loggerObj.fatal("Config file not found.");
				throw new RuntimeException("Config file not found.");
			}
		} catch (IOException ex) {
			loggerObj.fatal("Failed to load config properties.", ex);
			throw new RuntimeException("Failed to load config properties.", ex);
		}
	}

	/**
	 * Establishes a connection to the database using the configured URL, username,
	 * and password.
	 * 
	 * @return a Connection object.
	 * @throws SQLException if a database access error occurs.
	 */
	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
	}

	/**
	 * Inserts a Student into the database.
	 * 
	 * @param stuObj the Student object to be inserted.
	 * @return true if the insertion is successful; false otherwise.
	 * @throws SQLException             if a database access error occurs.
	 * @throws IllegalArgumentException if the provided Student} object is null.
	 */
	@Override
	public boolean insertStudent(Student stuObj) throws SQLException, IllegalArgumentException {
		if (stuObj == null) {
			loggerObj.error("Student Object is null in insertStudent");
			throw new IllegalArgumentException("Student object cannot be null.");
		}

		String sql = "INSERT INTO students (student_id, first_name, last_name) VALUES (?, ?, ?)";
		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, stuObj.getStudentId());
			stmt.setString(2, stuObj.getFirstName());
			stmt.setString(3, stuObj.getLastName());

			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				loggerObj.info("Student inserted successfully with ID " + stuObj.getStudentId());
				return true;
			} else {
				loggerObj.info("Insertion failed for Student ID " + stuObj.getStudentId());
				return false;
			}
		} catch (SQLException e) {
			loggerObj.error("Failed to insert student data", e);
			throw e;
		}
	}

	/**
	 * Fetches a Student from the database based on the student ID.
	 * 
	 * @param studentId the ID of the student to be fetched.
	 * @return the Student object if found.
	 * @throws SQLException                if a database access error occurs.
	 * @throws InvalidStudentDataException if the student data is invalid or not
	 *                                     found.
	 */
	@Override
	public Student fetchStudent(int studentId) throws SQLException, InvalidStudentDataException {
		String sql = "SELECT first_name, last_name FROM students WHERE student_id = ?";
		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, studentId);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				Student stuObj = new Student(studentId, rs.getString("first_name"), rs.getString("last_name"));
				loggerObj.info("Student data successfully fetched for ID " + studentId);
				return stuObj;
			} else {
				loggerObj.warn("Student with ID " + studentId + " not found");
				throw new SQLException("Student not found.");
			}
		} catch (SQLException e) {
			loggerObj.error("Error fetching student data for ID " + studentId, e);
			throw e;
		}
	}

	/**
	 * Deletes a Student from the database based on the student ID.
	 * 
	 * @param studentId the ID of the student to be deleted.
	 * @return true if the deletion is successful; false otherwise.
	 * @throws SQLException if a database access error occurs.
	 */
	@Override
	public boolean deleteStudent(int studentId) throws SQLException {
		String sql = "DELETE FROM students WHERE student_id = ?";
		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, studentId);
			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				loggerObj.info("Student deleted successfully for ID " + studentId);
				return true;
			} else {
				loggerObj.warn("Student with ID " + studentId + " not found");
				return false;
			}
		} catch (SQLException e) {
			loggerObj.error("Failed to delete student data", e);
			throw e;
		}
	}

	/**
	 * Updates the first name of a Student in the database based on the student ID.
	 * 
	 * @param studentId    the ID of the student whose first name is to be updated.
	 * @param updFirstName the new first name to be set.
	 * @return true if the update is successful; false otherwise.
	 * @throws SQLException if a database access error occurs.
	 */
	@Override
	public boolean updateStudentFirstName(int studentId, String updFirstName) throws SQLException {
		String sql = "UPDATE students SET first_name = ? WHERE student_id = ?";
		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, updFirstName);
			stmt.setInt(2, studentId);

			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				loggerObj.info("Student First Name updated successfully for ID " + studentId);
				return true;
			} else {
				loggerObj.warn("Student with ID " + studentId + " not found");
				return false;
			}
		} catch (SQLException e) {
			loggerObj.error("Failed to update student first name", e);
			throw e;
		}
	}

	/**
	 * Updates the last name of a Student in the database based on the student ID.
	 * 
	 * @param studentId   the ID of the student whose last name is to be updated.
	 * @param updLastName the new last name to be set.
	 * @return true if the update is successful; false otherwise.
	 * @throws SQLException if a database access error occurs.
	 */
	@Override
	public boolean updateStudentLastName(int studentId, String updLastName) throws SQLException {
		String sql = "UPDATE students SET last_name = ? WHERE student_id = ?";
		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, updLastName);
			stmt.setInt(2, studentId);

			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				loggerObj.info("Student Last Name updated successfully for ID " + studentId);
				return true;
			} else {
				loggerObj.warn("Student with ID " + studentId + " not found");
				return false;
			}
		} catch (SQLException e) {
			loggerObj.error("Failed to update student last name", e);
			throw e;
		}
	}
}
