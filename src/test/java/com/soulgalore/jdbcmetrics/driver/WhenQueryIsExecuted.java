package com.soulgalore.jdbcmetrics.driver;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;

import com.soulgalore.jdbcmetrics.QueryThreadLocal;

public class WhenQueryIsExecuted extends AbstractDriverTest {

	Connection connection;
	Statement statement;
	
	@Before
	public void setup() throws SQLException {
		connection = driver.connect(URL_JDBC_METRICS, null);
		assertThat(connection, notNullValue());
		statement = connection.createStatement();
		assertThat(statement, notNullValue());
		
		QueryThreadLocal.init();
		assertThat(reads(), is(0));
		assertThat(writes(), is(0));
	}
	
	@Test
	public void executeShouldIncreaseReadCounter() throws SQLException {
		statement.execute("SELECT 1");
		assertThat(reads(), is(1));
		assertThat(writes(), is(0));
	}
	
	@Test
	public void executeShouldIncreaseWriteCounter() throws SQLException {
		statement.execute("INSERT 1 (SELECT 2)");
		assertThat(reads(), is(0));
		assertThat(writes(), is(1));
	}

	@Test
	public void executeQueryShouldIncreaseReadCounter() throws SQLException {
		statement.executeQuery("SELECT 1");
		assertThat(reads(), is(1));
		assertThat(writes(), is(0));
	}

	@Test
	public void executeUpdateShouldIncreaseWriteCounter() throws SQLException {
		statement.executeUpdate("INSERT 1");
		assertThat(reads(), is(0));
		assertThat(writes(), is(1));
	}

	@Test
	public void executeBatchShouldIncreaseCounters() throws SQLException {
		statement.addBatch("SELECT 1");
		statement.addBatch("INSERT 2");
		statement.addBatch("SELECT 3");
		statement.addBatch("SELECT 4");
		statement.addBatch("INSERT 5");
		statement.executeBatch();
		assertThat(reads(), is(3));
		assertThat(writes(), is(2));
	}
	
	private int reads() {
		return QueryThreadLocal.getNrOfQueries().getReads();
	}
	
	private int writes() {
		return QueryThreadLocal.getNrOfQueries().getWrites();
	}
}
