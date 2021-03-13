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

    private final ConexaoPostgresJDBC conexao;

    public UsuarioControl() throws ClassNotFoundException, SQLException {
        this.conexao = new ConexaoPostgresJDBC();
    }
    
    public Connection conexao() {
    	return this.conexao.getConnection();
    }
    
    public  void close() {
    	this.conexao.close();
    }
    
    public void commit() throws SQLException {
    	this.conexao.commit();
    }
    
    public void rollback() {
    	this.conexao.rollback();
    }
    
    public boolean inserir(Usuario usuario) throws SQLException {
        Long id = null;
        String sqlQuery = "insert into usuario(id_pessoa) values(?)";
        PreparedStatement stmt = null;
        try {
            stmt = this.conexao().prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            stmt.setLong(1, usuario.getId());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getLong("id_usuario");
            }
            this.commit();
        } catch (SQLException error) {
            this.rollback();
            throw error;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                }
            }
            if (conexao != null) {
                this.close();
            }
        }
        return true;
    }

    public Pessoa buscarPeloCPF(String cpf_usuario) throws SQLException {
        String sqlQuery = "select u.id_usuario, p.nome_pessoa, p.numero_sus from usuario as u natural inner join pessoa as p where p.cpf = ?";
        PreparedStatement stmt = null;
        Pessoa pessoa = null;
        try {
            stmt = this.conexao().prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, cpf_usuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                pessoa = new Pessoa();
                pessoa.setId(rs.getLong(1));
                pessoa.setNome(rs.getString(2));
                pessoa.setNumeroSUS(rs.getString(3));
            }
            this.commit();
        } catch (SQLException error) {
            this.rollback();
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
                this.close();
            }
        }
        return pessoa;
    }
}
