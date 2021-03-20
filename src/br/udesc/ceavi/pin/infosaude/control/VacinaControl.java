package br.udesc.ceavi.pin.infosaude.control;

import br.udesc.ceavi.pin.infosaude.control.dao.ConexaoPostgresJDBC;
import br.udesc.ceavi.pin.infosaude.control.excecpton.DadosVaziosExcepitions;
import br.udesc.ceavi.pin.infosaude.modelo.Campanha;
import br.udesc.ceavi.pin.infosaude.modelo.Profissional;
import br.udesc.ceavi.pin.infosaude.modelo.PublicoAlvo;
import br.udesc.ceavi.pin.infosaude.modelo.Sexo;
import br.udesc.ceavi.pin.infosaude.modelo.Vacina;
import br.udesc.ceavi.pin.infosaude.principal.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author lucas
 */
public class VacinaControl {

    private Connection conexao;
    
    public Connection conexao() throws ClassNotFoundException, SQLException{
    	return this.conexao = ConexaoPostgresJDBC.getConnection();
    }
    
    public String getResultSetString(ResultSet rs, String campo) throws SQLException {
    	return rs.getString(campo);
    }
    
    public long getResultSetLong(ResultSet rs, String campo) throws SQLException {
    	return rs.getLong(campo);
    }
    
    public int getResultSetInt(ResultSet rs, String campo) throws SQLException {
    	return rs.getInt(campo);
    }
    
    //Obter vacina cadastrar em banco
    public List<Vacina> getVacinas() throws SQLException, ClassNotFoundException {
        List<Vacina> listaVacina = new ArrayList<>();
        String sqlQuery = "select * from vacina as v";
        PreparedStatement stmt = this.conexao().prepareStatement(sqlQuery);
        stmt.execute();
        ResultSet resultSet = stmt.getResultSet();

        while (resultSet.next()) {
            Vacina vacina = new Vacina(getResultSetLong(resultSet, "id_vacina"),
                    getResultSetInt(resultSet, "num_doses"), getResultSetString(resultSet, "nome_vacina"), getResultSetString(resultSet, "observacao"));
            listaVacina.add(vacina);
        }
        return listaVacina;
    }
    
    public PublicoAlvo setarMin(PublicoAlvo publicoAlvo, int idadeMin) {
    	publicoAlvo.setMinIdade(idadeMin);
    	return publicoAlvo;
    }
    
    public PublicoAlvo setarMax(PublicoAlvo publicoAlvo, int idadeMax) {
    	publicoAlvo.setMinIdade(idadeMax);
    	return publicoAlvo;
    }
    
    public PublicoAlvo setarSexo(PublicoAlvo publicoAlvo, Sexo sexo) {
    	publicoAlvo.setSexo(sexo);
    	return publicoAlvo;
    }
    
    //Obter publico alvo de vacina
    public List<PublicoAlvo> obterPublicoAlvo(Long id_vacina) throws SQLException, ClassNotFoundException {
        List<PublicoAlvo> listaPublicoAlvo = new ArrayList<>();
        String sqlQuery = "select pa.max_idade,pa.min_idade,pa.sexo from publico_alvo as pa natural inner join vacina where pa.id_vacina = ?";
        PreparedStatement stmt = this.conexao().prepareStatement(sqlQuery);
        stmt.setLong(1, id_vacina);

        stmt.execute();
        ResultSet resultSet = stmt.getResultSet();

        while (resultSet.next()) {
            PublicoAlvo publicoAlvo = new PublicoAlvo();
            
            int idadeMAX = resultSet.getInt("max_idade");
            int idadeMIN = resultSet.getInt("min_idade");
            Sexo sexo = Sexo.valueOf(resultSet.getString("sexo"));
            publicoAlvo = setarMin(publicoAlvo, idadeMIN);
            publicoAlvo = setarMax(publicoAlvo,idadeMAX);
            publicoAlvo = setarSexo(publicoAlvo, sexo);

            listaPublicoAlvo.add(publicoAlvo);
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
        return listaPublicoAlvo;
    }

    //Inserir vacina
    public Long inserir(int doses, String nome, String obs) throws SQLException, ClassNotFoundException {
        Long id_vacina = null;
        String sqlQueryComObs = "insert into vacina(num_doses,nome_vacina,observacao) values(?,?,?)";
        String sqlQuerySemObs = "insert into vacina(num_doses,nome_vacina) values(?,?)";

        PreparedStatement stmt = null;
        try {
            if (!obs.equals("")) {
                stmt = this.conexao().prepareStatement(sqlQueryComObs, Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, doses);
                stmt.setString(2, nome);
                stmt.setString(3, obs);
            } else {
                stmt = this.conexao().prepareStatement(sqlQuerySemObs, Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, doses);
                stmt.setString(2, nome);
            }
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                id_vacina = rs.getLong(1);
            }
            System.out.println("KEY: " + id_vacina);
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

        return id_vacina;
    }

    //Validar dados de vacina
    public boolean validarVacina(int numeroDoses, String nomeVacina) throws DadosVaziosExcepitions {
        if (numeroDoses < 1) {
            throw new DadosVaziosExcepitions("Numero de Dose Incompativel");
        }
        if (nomeVacina.equals("")) {
            throw new DadosVaziosExcepitions("Numero de Dose Incompativel");
        }
        return true;
    }
    
    //Obtem as Vacinas aplicadas no usuario
    public List<Vacina> getVacinaUsuario() throws SQLException, ClassNotFoundException {
        List<Vacina> listaDeVacina = new ArrayList();
        String sqlQuery1 = "select v.id_vacina, v.nome_vacina,c.dose_aplicada,c.observacoes,c.id_profissional, p.nome_pessoa as profissional, p.id_pessoa"
                + "from vacina as v natural inner join carterinha as c natura inner join pessoa as p"
                + "where c.id_usuario = ?";
        PreparedStatement stmt = this.conexao().prepareStatement(sqlQuery1);
        stmt.setLong(1, Main.usuario.getId());
        stmt.execute();
        ResultSet resultSet = stmt.getResultSet();

        while (resultSet.next()) {
            Vacina vacina = new Vacina();
            
            int idVacina = resultSet.getInt("id_vacina");
            int dose = resultSet.getInt("dose_aplivada");
            String nomeVacina = resultSet.getString("nome_vacina");
            String observacao = resultSet.getString("observacoes");
            Profissional profissional = new Profissional(resultSet.getLong("id_pessoa"), resultSet.getLong("id_profissional"), resultSet.getString("profissional"));
            
            vacina.setVacinada(idVacina, dose, nomeVacina, observacao, profissional);
            listaDeVacina.add(vacina);
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
        return listaDeVacina;
    }

    //Obter Numero de pessoa condizentes com o publico alvo informado
    public int getNumeroPessoaCondizentesComOPublicoAlvo(PublicoAlvo publico_alvo) {

        return -1;
    }

    public int getNumPessoaQueAplicaramVacinaPerantePublicoAlvo(PublicoAlvo publico_alvo, long id_vacina) {
        return -1;
    }

    public Vacina getVacinaPeloNome(String nome_vacina) throws ClassNotFoundException, SQLException {
        System.out.println("Nome passado :" + nome_vacina);
        String sqlQuery = "select v.id_vacina, v.num_doses from vacina as v where v.nome_vacina ~* ?";
        Vacina vacina = null;
        PreparedStatement stmt = null;
        try {
            stmt = this.conexao().prepareStatement(sqlQuery);
            stmt.setString(1, nome_vacina);
            stmt.executeQuery();
            ResultSet rs = stmt.getResultSet();
            if (rs.next()) {
                vacina = new Vacina();
                vacina.setId(rs.getLong(1));
                vacina.setDose(rs.getInt(2));
            }
            this.conexao.commit();
        } catch (SQLException error) {
            this.conexao.rollback();
            error.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (SQLException ex) {
            }
            this.conexao.close();
        }
        return vacina;
    }
    
    public PreparedStatement setarInt(PreparedStatement stmt, int dose, int num) throws SQLException {
    	stmt.setInt(num, dose);
    	return stmt;
    }
    
    public PreparedStatement setarLong(PreparedStatement stmt, Long id, int num) throws SQLException {
		stmt.setLong(num, id);
    	return stmt;
    }
    
    public PreparedStatement criaPS(PreparedStatement stmt, Long id_vacina, Long id_usuario, Long id_campanha, Long id_profissional, int dose, String observacoes) throws SQLException {
        setarLong(stmt, id_usuario, 1);
        setarLong(stmt, id_vacina, 2);
        setarLong(stmt, id_profissional, 3);
        stmt.setDate(4, new java.sql.Date(Calendar.getInstance().getTimeInMillis()));
        setarInt(stmt, dose, 5);
        return stmt;
    }
    
    public ResultSet executePrepared(PreparedStatement stmt, String sql, long id_usuario) throws ClassNotFoundException, SQLException {
    	stmt = this.conexao().prepareStatement(sql);
        stmt = setarLong(stmt, id_usuario, 1);
        ResultSet rs = stmt.executeQuery();
		return rs;
    }

    public boolean aplicarVacina(Long id_vacina, Long id_usuario, Long id_campanha, Long id_profissional, int dose, String observacoes) throws SQLException, ClassNotFoundException {
        String sqlQueryComCampanhaEOBS = "insert into carterinha(id_usuario,id_vacina,id_campanha,id_profissional,data_aplicacao,observacoes,dose_aplicada)"
                + "values (?,?,?,?,?,?,?);";
        String sqlQueryComOBS = "insert into carterinha(id_usuario,id_vacina,id_profissional,data_aplicacao,observacoes,dose_aplicada)"
                + "values (?,?,?,?,?,?);";
        String sqlQuery1ComCampanha = "insert into carterinha(id_usuario,id_vacina,id_campanha,id_profissional,data_aplicacao,dose_aplicada)"
                + "values (?,?,?,?,?,?);";
        String sqlQuerySIMPLE = "insert into carterinha(id_usuario,id_vacina,id_profissional,data_aplicacao,dose_aplicada)"
                + "values (?,?,?,?,?);";
        PreparedStatement stmt = null;
        int q = -1;
        try {
            if (id_campanha == -1 && observacoes.equals("")) {
                stmt = this.conexao().prepareStatement(sqlQuerySIMPLE);
                criaPS(stmt, id_vacina, id_usuario, id_campanha, id_profissional, dose, observacoes);
            } else if (id_campanha != -1 && observacoes.equals("")) {
                stmt = this.conexao().prepareStatement(sqlQuery1ComCampanha);
                criaPS(stmt, id_vacina, id_usuario, id_campanha, id_profissional, dose, observacoes);
            } else if (id_campanha == -1 && !observacoes.equals("")) {
                stmt = this.conexao().prepareStatement(sqlQueryComOBS);
                criaPS(stmt, id_vacina, id_usuario, id_campanha, id_profissional, dose, observacoes);
            } else if (id_campanha != -1 && !observacoes.equals("")) {
                stmt = this.conexao().prepareStatement(sqlQueryComCampanhaEOBS);
                criaPS(stmt, id_vacina, id_usuario, id_campanha, id_profissional, dose, observacoes);
            }
            stmt.executeQuery();
            ResultSet rs = stmt.getResultSet();
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

        return true;
    }

    public List<Campanha> getCampanhaDoUsuario(Long id_usuario) throws SQLException, ClassNotFoundException {
        List<Campanha> lista = new ArrayList<>();
        String sql = "select p.id_pessoa,c.id_profissional,p.nome_pessoa, c.data_aplicacao,"
                + "c.dose_aplicada, v.nome_vacina,camp.slogan,camp.data_inicio,camp.data_fim"
                + "from carterinha as c natural inner join campanha as camp "
                + "natural inner join vacina as v"
                + "inner join pessoa as p on (p.id_pessoa = c.id_profissional) where c.id_usuario = ?";
        PreparedStatement stmt = null;
        Vacina vacinaDaCampanha;
        Campanha campanha;
        Profissional prof;
        try {
            ResultSet rs = executePrepared(stmt, sql, id_usuario);
            while (rs.next()) {
            	long id_pessoa = getResultSetLong(rs, "id_pessoa");
            	long id_profissional = getResultSetLong(rs, "id_profissional");
            	String nome = getResultSetString(rs, "nome_profissional");
                prof = new Profissional(id_pessoa, id_profissional, nome);
                
                String data = getResultSetString(rs, "data_aplicacao");
                int dose = getResultSetInt(rs, "dose_aplicada");
                String nome_vacina = getResultSetString(rs, "nome_vacina");
                
                vacinaDaCampanha = new Vacina(new Date(data), dose, nome_vacina, prof);
                
                String slogan = getResultSetString(rs, "slogan");
                String dataI = getResultSetString(rs, "data_inicio");
                String dataF = getResultSetString(rs, "data_fim");
                campanha = new Campanha(slogan, vacinaDaCampanha, new Date(dataI), new Date(dataF));
                lista.add(campanha);
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

        return lista;
    }

    public List<Vacina> getVacinaDoUsuario(Long id_usuario) throws SQLException, ClassNotFoundException {
        List<Vacina> lista = new ArrayList<>();
        String sql = "select p.id_pessoa,c.id_profissional,p.nome_pessoa, c.data_aplicacao,"
                + "c.dose_aplicada, v.nome_vacina"
                + "from carterinha as c"
                + "natural inner join vacina as v"
                + "inner join pessoa as p on (p.id_pessoa = c.id_profissional) where c.id_usuario = ?";
        PreparedStatement stmt = null;
        Vacina vacinaDaCampanha;
        Profissional prof;
        try {
        	ResultSet rs = executePrepared(stmt, sql, id_usuario);
            while (rs.next()) {
            	long id_pessoa = getResultSetLong(rs, "id_pessoa");
            	long id_profissional = getResultSetLong(rs, "id_profissional");
            	String nome = getResultSetString(rs, "nome_profissional");
                prof = new Profissional(id_pessoa, id_profissional, nome);
                
                String data = getResultSetString(rs, "data_aplicacao");
                int dose = getResultSetInt(rs, "dose_aplicada");
                String nome_vacina = getResultSetString(rs, "nome_vacina");
                
                vacinaDaCampanha = new Vacina(new Date(data), dose, nome_vacina, prof);
                lista.add(vacinaDaCampanha);
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
        return lista;
    }
}
