package br.udesc.ceavi.pin.infosaude.control.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class ProfissionalStatement extends PreparaStatement{
	public PreparedStatement executePrepared2(PreparedStatement stmt, long id, String sqlQuery) throws ClassNotFoundException, SQLException {
    	stmt = this.conexao().prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
        stmt = setPreparedLong(stmt, id, 1);
        stmt.executeUpdate();
        return stmt;
    }
}
