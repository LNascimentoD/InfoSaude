/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.udesc.ceavi.pin.infosaude.control;

import br.udesc.ceavi.pin.infosaude.control.dao.ConexaoPostgresJDBC;
import br.udesc.ceavi.pin.infosaude.control.excecpton.DadosVaziosExcepitions;
import br.udesc.ceavi.pin.infosaude.modelo.Endereco;
import br.udesc.ceavi.pin.infosaude.modelo.Instituicao;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author lucas
 */
public class InstituicaoControl {
    
    private final ConexaoPostgresJDBC conexao;

    public InstituicaoControl() throws ClassNotFoundException, SQLException {
        this.conexao = new ConexaoPostgresJDBC();
    }

    public boolean validaCampos(String cnpj, String nome, String senha) throws DadosVaziosExcepitions {
        boolean a = true;
        
        if(cnpj.equals("")){
            a = false;
            throw new DadosVaziosExcepitions("CNPJ INVALIDO!");
        }
        if(nome.equals("")){
            a = false;
            throw new DadosVaziosExcepitions("NOME INVALIDO!");
        }
        if(senha.equals("")){
            a = false;
            throw new DadosVaziosExcepitions("SENHA INVALIDO!");
        }
        
        return a;
    }
    
    public Long inserir(Instituicao instituicao,Endereco endereco) throws SQLException,ClassNotFoundException{
        Long id = null;
        String sqlQuery = "insert into instituicao(id_endereco,cnpj,nome,senha) values(?,?,?,?)returning id_instituicao";
        
        try {
            PreparedStatement stmt = this.conexao.getConnection().prepareStatement(sqlQuery);
            stmt.setLong(1, endereco.getId());
            stmt.setString(2, instituicao.getCnpj());
            stmt.setString(3, instituicao.getNome());
            stmt.setString(4, instituicao.getSenha());
            
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                id = rs.getLong("id_instituicao");
            }
            
            this.conexao.commit();
        } catch (SQLException error) {
            this.conexao.rollback();
            throw error;
        }
        
        return id;
    }
    
}
