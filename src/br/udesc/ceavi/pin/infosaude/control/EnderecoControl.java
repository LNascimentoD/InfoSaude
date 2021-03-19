package br.udesc.ceavi.pin.infosaude.control;

import br.udesc.ceavi.pin.infosaude.control.dao.ConexaoPostgresJDBC;
import br.udesc.ceavi.pin.infosaude.control.excecpton.DadosVaziosExcepitions;
import br.udesc.ceavi.pin.infosaude.modelo.Endereco;
import br.udesc.ceavi.pin.infosaude.modelo.Estado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lucas
 */
public class EnderecoControl {

	private Connection conexao;
    
    public Connection conexao() throws ClassNotFoundException, SQLException{
    	return this.conexao = ConexaoPostgresJDBC.getConnection();
    }
    
    public boolean validaCampos(String bairro, String cep, String cidade, int numero, String rua) throws DadosVaziosExcepitions {
        boolean a = true;

        if (bairro.equals("")) {
            a = false;
            throw new DadosVaziosExcepitions("BAIRRO INVALIDO!");
        }
        if (cep.equals("")) {
            a = false;
            throw new DadosVaziosExcepitions("CEP INVALIDO!");
        }
        if (cidade.equals("")) {
            a = false;
            throw new DadosVaziosExcepitions("CIDADE INVALIDO!");
        }
        if (numero == 0) {
            a = false;
            throw new DadosVaziosExcepitions("NUMERO DE RESIDENCIA INVALIDO!");
        }
        if (rua.equals("")) {
            a = false;
            throw new DadosVaziosExcepitions("RUA INVALIDA!");
        }

        return a;
    }

    public Long inserir(Endereco endereco) throws SQLException, ClassNotFoundException {
        Long id = null;
        String sqlQuery = "insert into endereco(bairro,cep,cidade,complemento,numero,rua,estado,email,telefone) values(?,?,?,?,?,?,?,?,?)";

        PreparedStatement stmt = null;
        try {
            executePrepared(stmt, endereco, sqlQuery);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                endereco.setId(rs.getLong(1));
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
            if (this.conexao != null) {
                this.conexao.close();
            }
        }
        return id;
    }

    public Endereco getEndereco(Long id_endereco) throws SQLException, ClassNotFoundException {
        Endereco endereco = null;
        String sqlQuery = "select * from endereco where id_endereco = ?";
        PreparedStatement stmt = null;
        try {
            stmt = this.conexao().prepareStatement(sqlQuery);
            stmt.setLong(1, id_endereco);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                endereco = new Endereco(rs.getString("bairro"),
                        rs.getString("cep"),
                        rs.getString("cidade"),
                        rs.getString("complemeto"),
                        rs.getString("email"),
                        rs.getInt("numero"),
                        rs.getString("rua"),
                        rs.getString("telefone"),
                        Estado.valueOf("estado"));
                endereco.setId(id_endereco);
            }
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
            if (this.conexao != null) {
                this.conexao.close();
            }
        }
        return endereco;
    }
    
    public PreparedStatement setPreparedStatementString(PreparedStatement stmt, String info, int num) throws SQLException {
    	stmt.setString(num, info);
        
        return stmt;
    }
    
    public PreparedStatement setPreparedStatementInt(PreparedStatement stmt, Endereco endereco) throws SQLException {
        stmt.setInt(5, endereco.getNumero());
    	return stmt;
    }
    
    public PreparedStatement setPreparedStatementLong(PreparedStatement stmt, Endereco endereco) throws SQLException {
    	stmt.setLong(10, endereco.getId());
    	return stmt;
    }
    
    public int executePrepared(PreparedStatement stmt, Endereco endereco, String sqlQuery) throws ClassNotFoundException, SQLException {
    	String bairro = endereco.getBairro();
    	String cep = endereco.getCep();
    	String cidade = endereco.getCidade();
    	String complemento = endereco.getComplemento();
    	String rua = endereco.getRua();
    	String estado = endereco.getEstado().toString().replaceAll(" ", "_");
    	String email = endereco.getEmail();
    	String telefone = endereco.getTelefone();
    	
    	stmt = this.conexao().prepareStatement(sqlQuery);
    	stmt = setPreparedStatementString(stmt,bairro, 1);
    	stmt = setPreparedStatementString(stmt,cep, 2);
    	stmt = setPreparedStatementString(stmt,cidade, 3);
    	stmt = setPreparedStatementString(stmt,complemento, 4);
    	stmt = setPreparedStatementString(stmt,rua, 6);
    	stmt = setPreparedStatementString(stmt,estado, 7);
    	stmt = setPreparedStatementString(stmt,email, 8);
    	stmt = setPreparedStatementString(stmt,telefone, 9);

    	stmt = setPreparedStatementInt(stmt, endereco);
    	stmt = setPreparedStatementLong(stmt, endereco);
    	return stmt.executeUpdate();
    }

    public boolean update(Endereco endereco) throws SQLException, ClassNotFoundException {
        boolean atualizado = false;
        String sqlQuery = "UPDATE endereco SET bairro=?, cep=?, cidade=?, complemento=?, numero=?, rua=?, estado=?, email=?, telefone=? WHERE endereco.id_endereco = ?";
        PreparedStatement stmt = null;
        try {
            atualizado = executePrepared(stmt, endereco, sqlQuery)  == 1;
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
            if (this.conexao != null) {
                this.conexao.close();
            }
        }
        return atualizado;
    }
}
