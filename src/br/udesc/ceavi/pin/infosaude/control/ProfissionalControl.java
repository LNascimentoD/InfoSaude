package br.udesc.ceavi.pin.infosaude.control;

import br.udesc.ceavi.pin.infosaude.control.dao.ConexaoPostgresJDBC;
import br.udesc.ceavi.pin.infosaude.control.dao.PreparaStatement;
import br.udesc.ceavi.pin.infosaude.control.dao.ProfissionalStatement;
import br.udesc.ceavi.pin.infosaude.modelo.Instituicao;
import br.udesc.ceavi.pin.infosaude.modelo.Profissional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author lucas
 */
public class ProfissionalControl {

	private Connection conexao;
    
    public Connection conexao() throws ClassNotFoundException, SQLException{
    	return this.conexao = ConexaoPostgresJDBC.getConnection();
    }
    
    public ProfissionalStatement getStatement() {
    	ProfissionalStatement ps = new ProfissionalStatement();
    	return ps;
    }
    
    public long getResultLong(ResultSet rs, String index) throws SQLException {
    	return rs.getLong(index);
    }
    
    public Long inserir(Profissional profissional, Instituicao instituicao) throws SQLException, ClassNotFoundException {
        Long id = null;
        String sqlQuery = "insert into profissional(id_instituicao) values(?)";

        PreparedStatement stmt = null;
        try {
        	long _id = instituicao.getId();
            stmt = getStatement().executePrepared2(stmt, _id, sqlQuery);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                id = getResultLong(rs, "id_instituicao");
            }

            this.conexao.commit();
        } catch (SQLException error) {
            this.conexao.rollback();
            throw error;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                }
            }
            if (conexao != null) {
                this.conexao.close();
            }
        }

        return id;
    }
    
    public ResultSet executePrepared(PreparedStatement stmt, String sqlQuery, String login, String senha) throws ClassNotFoundException, SQLException {
    	stmt = this.conexao().prepareStatement(sqlQuery);
    	stmt = getStatement().setPreparedString(stmt, login, 1);
    	stmt = getStatement().setPreparedString(stmt, senha, 2);
        stmt.executeQuery();
        ResultSet rs = stmt.getResultSet();
        
        return rs;
    }

    public Long getAcessoProfissional(String login, String senha) throws SQLException, ClassNotFoundException {
        Long id_profissional = -1l;
        String sqlQuery = "select prof.id_profissional from pessoa as p natural inner join profissional as prof where p.login = ? and p.senha = ?";
        PreparedStatement stmt = null;
        int q = -1;
        try {
            ResultSet rs = executePrepared(stmt, sqlQuery, login, senha);       
            while (rs.next()) {
                id_profissional = getResultLong(rs, "id_profissional");
            }
        } catch (SQLException ex) {
            this.conexao.rollback();
            throw ex;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                }
            }
            if (conexao != null) {
                this.conexao.close();
            }
        }

        return id_profissional;
    }
}
