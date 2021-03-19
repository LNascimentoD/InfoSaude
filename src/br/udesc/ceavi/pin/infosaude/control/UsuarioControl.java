package br.udesc.ceavi.pin.infosaude.control;

import br.udesc.ceavi.pin.infosaude.control.dao.ConexaoPostgresJDBC;
import br.udesc.ceavi.pin.infosaude.modelo.Pessoa;
import br.udesc.ceavi.pin.infosaude.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author lucas
 */
public class UsuarioControl {

	private Connection conexao;
    
    public Connection conexao() throws ClassNotFoundException, SQLException{
    	return this.conexao = ConexaoPostgresJDBC.getConnection();
    }
    
    public PreparedStatement setarLong(PreparedStatement stmt, int num, long id) throws SQLException {
    	 stmt.setLong(num, id);
    	return stmt;
    }
    
    public PreparedStatement executarStmt(String sqlQuery, PreparedStatement stmt, long userId) throws ClassNotFoundException, SQLException {
    	stmt = this.conexao().prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
    	setarLong(stmt, 1, userId);
    	stmt.executeUpdate();
    	return stmt;
    }
   
    public boolean inserir(Usuario usuario) throws SQLException, ClassNotFoundException {
        Long id = null;
        String sqlQuery = "insert into usuario(id_pessoa) values(?)";
        PreparedStatement stmt = null;
        long userId = usuario.getId();
        try {
        	stmt = executarStmt(sqlQuery, stmt, userId);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getLong("id_usuario");
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
        return true;
    }

    public Pessoa setarId(Pessoa pessoa, long id) {
    	pessoa.setId(id);
    	return pessoa;
    }
    
    public Pessoa setarNome(Pessoa pessoa, String nome) {
    	pessoa.setNome(nome);
    	return pessoa;
    }
    public Pessoa buscarPeloCPF(String cpf_usuario) throws SQLException, ClassNotFoundException {
        String sqlQuery = "select u.id_usuario, p.nome_pessoa, p.numero_sus from usuario as u natural inner join pessoa as p where p.cpf = ?";
        PreparedStatement stmt = null;
        Pessoa pessoa = null;
        try {
            stmt = this.conexao().prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, cpf_usuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                pessoa = new Pessoa();
                long id = rs.getLong(1);
                String nome = rs.getString(2);
                String numeroSUS = rs.getString(3);
                pessoa = setarId(pessoa, id);
                pessoa = setarNome(pessoa, nome);
                pessoa = setarNome(pessoa, numeroSUS);
            }
            this.conexao.commit();
        } catch (SQLException error) {
            this.conexao.rollback();
            error.printStackTrace();
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
        return pessoa;
    }
}
