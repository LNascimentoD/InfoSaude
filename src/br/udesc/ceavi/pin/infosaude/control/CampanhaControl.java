package br.udesc.ceavi.pin.infosaude.control;

import br.udesc.ceavi.pin.infosaude.control.dao.ConexaoPostgresJDBC;
import br.udesc.ceavi.pin.infosaude.modelo.Campanha;
import br.udesc.ceavi.pin.infosaude.modelo.Vacina;
import br.udesc.ceavi.pin.infosaude.principal.Main;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author lucas
 */
public class CampanhaControl {

    private Connection conexao;
    
    public Connection conexao() throws ClassNotFoundException, SQLException{
    	return this.conexao = ConexaoPostgresJDBC.getConnection();
    }
    
    public long buscaData(Date data) {    	
    	return data.getTime();
    }
    
    public boolean validarCampos(String slogan, Date dataInicio, Date dataFim) {
        boolean a = true;
        
        long inicio = buscaData(dataInicio);
        long fim = buscaData(dataFim);

        if (slogan.equals("")) {
            a = false;
            JOptionPane.showMessageDialog(null, "INSIRA UM SLOGAN PARA A CAMPANHA");
        }
        if (inicio > fim) {
            a = false;
            JOptionPane.showMessageDialog(null, "DADO INVALIDO! DATA INSERIDA INVALIDA \nDIA NÃO CONSISTE!");
        }
        return a;
    }
    
    public PreparedStatement generateStatementLong(PreparedStatement stmt, long id_instituicao, long id_vacina) throws SQLException {
    	stmt.setLong(1, id_instituicao);
        stmt.setLong(2, id_vacina);
        return stmt;
    }
    
    public PreparedStatement generateStatementDate(PreparedStatement stmt, Date dataini, Date datafim) throws SQLException {
    	stmt.setDate(3, dataini);
        stmt.setDate(4, datafim);
        return stmt;
    }

    public PreparedStatement generateStatementString(PreparedStatement stmt, String slogan) throws SQLException {
    	stmt.setString(4, slogan);
    	return stmt;
    }
    
    public String returnSlogin(Campanha campanha) {
    	return campanha.getSlogan();
    }
    
    public long returnDateInicio(Campanha campanha) {
    	return campanha.getDataInicio().getTime();
    }
    
    public long returnDateFim(Campanha campanha) {
    	return campanha.getDataFim().getTime();
    }
    
    public PreparedStatement executeSTMT(PreparedStatement stmt, long id_instituicao, long id_vacina, String sqlQuery, String slogan, Date dataini, Date datafim) throws ClassNotFoundException, SQLException {
    	stmt = this.conexao().prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
        stmt = generateStatementLong(stmt, id_instituicao, id_vacina);
        stmt = generateStatementDate(stmt, dataini, datafim);
        stmt = generateStatementString(stmt, slogan);

        stmt.executeUpdate();
        return stmt;
    }
    
    public Long inserir(Campanha campanha, long id_instituicao, long id_vacina) throws SQLException, ClassNotFoundException {
        Long id = null;
        String sqlQuery = "insert into campanha(id_instituicao,id_vacina,data_inicio,data_fim,slogam) values(?,?,?,?,?)";

        PreparedStatement stmt = null;
        try {
        	long dataInicio = returnDateInicio(campanha);
        	long dataFim = returnDateInicio(campanha);
        	
        	String slogan = returnSlogin(campanha);
        	java.sql.Date dataini = new Date(dataInicio);
            java.sql.Date datafim = new Date(dataFim);
            
            stmt = executeSTMT(stmt, id_instituicao, id_vacina, sqlQuery, slogan, dataini, datafim);
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getLong(1);
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

    //Obtem as campanhas participada pelo usuario
    public List<Campanha> getCampanhaUsuario() throws SQLException, ClassNotFoundException {
        List<Campanha> listaDeCampanha = new ArrayList();
        String sqlQuery1 = "select c.id_campanha,c.slogam,c.id_vacina,c.data_inicio,c.data_fin"
                + "from carterinha natural inner join campanha"
                + "where c.id_usuario = ?";
        PreparedStatement stmt = this.conexao().prepareStatement(sqlQuery1);
        stmt.setLong(1, Main.usuario.getId());
        stmt.execute();
        ResultSet resultSet = stmt.getResultSet();

        while (resultSet.next()) {
            Campanha campanha = new Campanha();
            campanha.setId(resultSet.getLong("id_campanha"));
            campanha.setSlogan(resultSet.getString("slogam"));
            campanha.setVacina(new Vacina(resultSet.getLong("id_vacina")));
            campanha.setDataFim(new Date(resultSet.getShort(resultSet.getString("data_fim"))));
            campanha.setDataInicio(new Date(resultSet.getShort(resultSet.getString("data_inicio"))));
            listaDeCampanha.add(campanha);
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
            }
        }
        if (conexao != null) {
            this.conexao.close();
        }
        return listaDeCampanha;
    }

//Obtem todas as campanha
    public List<Campanha> getCampanhas() throws SQLException, ClassNotFoundException {
        List<Campanha> listaDeCampanha = new ArrayList();
        String sqlQuery = "select * from campanha natural inner join vacina";
        PreparedStatement stmt = this.conexao().prepareStatement(sqlQuery);
        stmt.execute();
        ResultSet resultSet = stmt.getResultSet();

        while (resultSet.next()) {
            Campanha campanha = new Campanha();
            campanha.setId(resultSet.getLong("id_campanha"));
            campanha.setSlogan(resultSet.getString("slogam"));
            Vacina vacina = new Vacina(resultSet.getLong("id_vacina"),
                    resultSet.getInt("num_doses"), resultSet.getString("nome_vacina"), resultSet.getString("observacao"));
            campanha.setVacina(vacina);
            campanha.setDataFim(new Date(resultSet.getShort(resultSet.getString("data_fim"))));
            campanha.setDataInicio(new Date(resultSet.getShort(resultSet.getString("data_inicio"))));
            listaDeCampanha.add(campanha);
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
            }
        }
        if (conexao != null) {
            this.conexao.close();
        }
        return listaDeCampanha;
    }

    public Campanha buscarCampanhaPorIdVacina(Long id_vacina) throws ClassNotFoundException, SQLException {
        String sqlQuery = "select c.id_campanha, c.slogan, c.data_fim, c.data_inicio, v.nome_vacina "
                + "from campanha as c natural inner join vacina as v where c.id_vacina = ? and c.data_fim > ?";
        PreparedStatement stmt = null;
        Campanha campanha = null;
        try {
            stmt = this.conexao().prepareStatement(sqlQuery);
            stmt.setLong(1, id_vacina);
            stmt.setDate(2, new java.sql.Date(Calendar.getInstance().getTimeInMillis()));
            stmt.executeQuery();
            ResultSet rs = stmt.getResultSet();
            if (rs.next()) {
                campanha = new Campanha();
                campanha.setId(rs.getLong(1));
                campanha.setSlogan(rs.getString(2));
                campanha.setDataFim(rs.getDate(3));
                campanha.setDataInicio(rs.getDate(4));
                Vacina vacina = new Vacina();
                vacina.setId(id_vacina);
                vacina.setVacina(rs.getString(5));
                campanha.setVacina(vacina);
            }
            this.conexao.commit();
        } catch (SQLException error) {
            this.conexao.rollback();
            error.printStackTrace();
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
        return campanha;
    }
}
