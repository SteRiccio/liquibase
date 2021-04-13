package liquibase.sqlgenerator.core;

import java.net.InetAddress;
import java.sql.Timestamp;

import liquibase.database.Database;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.LockDatabaseChangeLogStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.util.NetUtil;
import liquibase.util.SystemUtils;

public class LockDatabaseChangeLogGenerator extends AbstractSqlGenerator<LockDatabaseChangeLogStatement> {

	public ValidationErrors validate(LockDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    private static String hostname;
    private static String hostaddress;

    static {
    	if (SystemUtils.IS_OS_ANDROID) {
    		hostname = "localhost";
    		hostaddress = "127.0.0.1";
    	} else {
	        try {
	        	InetAddress localHost = NetUtil.getLocalHost();
	            hostname = localHost.getHostName();
	            hostaddress = localHost.getHostAddress();
	        } catch (Exception e) {
	            throw new UnexpectedLiquibaseException(e);
	        }
    	}
    }

    public Sql[] generateSql(LockDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
    	String liquibaseSchema = null;
    		liquibaseSchema = database.getLiquibaseSchemaName();

        UpdateStatement updateStatement = new UpdateStatement(liquibaseSchema, database.getDatabaseChangeLogLockTableName());
        updateStatement.addNewColumnValue("LOCKED", true);
        updateStatement.addNewColumnValue("LOCKGRANTED", new Timestamp(new java.util.Date().getTime()));
        updateStatement.addNewColumnValue("LOCKEDBY", hostname + " (" + hostaddress + ")");
        updateStatement.setWhereClause(database.escapeColumnName(liquibaseSchema, database.getDatabaseChangeLogTableName(), "ID") + " = 1 AND " + database.escapeColumnName(liquibaseSchema, database.getDatabaseChangeLogTableName(), "LOCKED") + " = "+ TypeConverterFactory.getInstance().findTypeConverter(database).getBooleanType().getFalseBooleanValue());

        return SqlGeneratorFactory.getInstance().generateSql(updateStatement, database);

    }
}