using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Configuration;
using MySql.Data.MySqlClient;
using System.Data;

namespace AdmiSee.Web
{
	public class DAO
	{
		private static string constr = ConfigurationManager.AppSettings["connectionString"];
		private MySqlConnection myConnection = new MySqlConnection(constr);

		public DAO()
		{
		}

		/// <summary>
		/// Abre a conexão com o banco
		/// </summary>
		public void AbrirConexao()
		{
			try
			{
				myConnection.Open();
			}
			catch (Exception e)
			{
			}
		}

		/// <summary>
		/// Fecha a conexao com o banco
		/// </summary>
		public void FecharConexao()
		{
			try
			{
				myConnection.Close();
			}
			catch (Exception e)
			{
			}
		}

		/// <summary>
		/// 
		/// </summary>
		/// <param name="enderecoOrigem"></param>
		/// <param name="enderecoDestino"></param>
		/// <param name="quantidadeConducoes"></param>
		/// <param name="tempoViagem"></param>
		/// <param name="valorTarifas"></param>
		/// <param name="rotas"></param>
		/// <returns></returns>
		public bool InsereRota(string enderecoOrigem, string enderecoDestino, short quantidadeConducoes, string tempoViagem, string valorTarifas, DataTable rotas)
		{
			try
			{
				AbrirConexao();
				
				Int32 idRotaConducao = 0, idRota = 0;

				MySqlParameter pEnderecoOrigem = new MySqlParameter("@enderecoOrigem", MySqlDbType.VarChar);
				pEnderecoOrigem.Value = enderecoOrigem;

				MySqlParameter pEnderecoDestino = new MySqlParameter("@enderecoDestino", MySqlDbType.VarChar);
				pEnderecoDestino.Value = enderecoDestino;

				MySqlParameter pQuantidadeConducoes = new MySqlParameter("@quantidadeConducoes", MySqlDbType.Int32);
				pQuantidadeConducoes.Value = quantidadeConducoes;

				MySqlParameter pTempoViagem = new MySqlParameter("@tempoViagem", MySqlDbType.VarChar);
				pTempoViagem.Value = tempoViagem;

				MySqlParameter pValorTarifas = new MySqlParameter("@valorTarifas", MySqlDbType.VarChar);
				pValorTarifas.Value = valorTarifas.Replace(",", ".");

				MySqlCommand cmdSelect = new MySqlCommand("SELECT idrota FROM `homeaccess4`.`rota` WHERE enderecoorigem = @enderecoOrigem and enderecodestino = @enderecoDestino", myConnection);
				cmdSelect.Parameters.Add(pEnderecoOrigem);
				cmdSelect.Parameters.Add(pEnderecoDestino);
				MySqlDataReader dr = cmdSelect.ExecuteReader();

				if (!dr.HasRows)
				{
					dr.Close();

					MySqlTransaction myTrans = myConnection.BeginTransaction();

					try
					{

						//Inserir
						MySqlCommand cmdInsertRotas = new MySqlCommand("INSERT `homeaccess4`.`rota` (enderecoorigem, enderecodestino, quantidadeconducoes, tempoviagem, valortarifas) VALUES (@enderecoOrigem, @enderecoDestino, @quantidadeConducoes, @tempoViagem, @valorTarifas);select @@IDENTITY", myConnection);
						cmdInsertRotas.Transaction = myTrans;
						cmdInsertRotas.Parameters.Add(pEnderecoOrigem);
						cmdInsertRotas.Parameters.Add(pEnderecoDestino);
						cmdInsertRotas.Parameters.Add(pQuantidadeConducoes);
						cmdInsertRotas.Parameters.Add(pTempoViagem);
						cmdInsertRotas.Parameters.Add(pValorTarifas);
						idRota = int.Parse(cmdInsertRotas.ExecuteScalar().ToString());

						MySqlParameter pIdRota = new MySqlParameter("@idRota", MySqlDbType.Int32);
						pIdRota.Value = idRota;

						foreach (DataRow dataRow in rotas.Rows)
						{
							MySqlParameter pEnderecoEmbarque = new MySqlParameter("@enderecoEmbarque", MySqlDbType.VarChar);
							pEnderecoEmbarque.Value = dataRow["enderecoEmbarque"].ToString();

							MySqlParameter pLinha = new MySqlParameter("@linha", MySqlDbType.VarChar);
							pLinha.Value = dataRow["linha"].ToString();

							MySqlParameter pEnderecoDesembarque = new MySqlParameter("@enderecoDesembarque", MySqlDbType.VarChar);
							pEnderecoDesembarque.Value = dataRow["enderecoDesembarque"].ToString();

							MySqlCommand cmdInsertRotaConducao = new MySqlCommand("INSERT `homeaccess4`.`rotaconducao` (idrota, enderecoEmbarque, linha, enderecoDesembarque) VALUES (@idRota, @enderecoEmbarque, @linha, @enderecoDesembarque)", myConnection);
							cmdInsertRotaConducao.Transaction = myTrans;
							cmdInsertRotaConducao.Parameters.Add(pIdRota);
							cmdInsertRotaConducao.Parameters.Add(pEnderecoEmbarque);
							cmdInsertRotaConducao.Parameters.Add(pLinha);
							cmdInsertRotaConducao.Parameters.Add(pEnderecoDesembarque);
							idRotaConducao = cmdInsertRotaConducao.ExecuteNonQuery();
						}

						myTrans.Commit();
					}
					catch (Exception ex)
					{
						myTrans.Rollback();
						return false;
					}
				}

				return (idRotaConducao > 0 && idRota > 0);
			}
			catch (Exception ex)
			{
				return false;
			}
			finally
			{
				FecharConexao();
			}
		}

		/// <summary>
		/// Método que retora as rotas
		/// </summary>
		/// <param name="linha">enderecoOrigem</param>
		/// <param name="onibus">enderecoDestino</param>
		/// <returns></returns>
		public DataTable RetornaRota(string enderecoOrigem, string enderecoDestino)
		{
			try
			{
				AbrirConexao();

				string comando = "SELECT idrota, enderecoorigem, enderecodestino, quantidadeconducoes, tempoviagem, valortarifas FROM `homeaccess4`.`rota` WHERE (enderecoorigem like '%" + enderecoOrigem.Trim() + "%') and (enderecodestino like '%" + enderecoDestino.Trim() + "%')";

				MySqlCommand cmdSelect = new MySqlCommand(comando, myConnection);
				MySqlDataReader dr = cmdSelect.ExecuteReader();

				DataTable lista = new DataTable();
				lista.Columns.Add("idrota");
				lista.Columns.Add("enderecoOrigem");
				lista.Columns.Add("enderecoDestino");
				lista.Columns.Add("quantidadeConducoes");
				lista.Columns.Add("tempoViagem");
				lista.Columns.Add("valorTarifas");

				if (dr.HasRows)
				{
					while (dr.Read())
					{
						string idrota = dr["idrota"].ToString();
						enderecoOrigem = dr["enderecoOrigem"].ToString();
						enderecoDestino = dr["enderecoDestino"].ToString();
						string quantidadeConducoes = dr["quantidadeConducoes"].ToString();
						string tempoViagem = dr["tempoViagem"].ToString();
						string valorTarifas = dr["valorTarifas"].ToString();

						lista.Rows.Add(idrota, enderecoOrigem, enderecoDestino, quantidadeConducoes, tempoViagem, valorTarifas);
					}
				}

				return lista;
			}
			catch (Exception ex)
			{
				throw new Exception(ex.Message);
			}
			finally
			{
				FecharConexao();
			}
		}

		public DataTable RetornaRotaConducao(int idRota)
		{
			try
			{
				AbrirConexao();

				MySqlParameter pIdRota = new MySqlParameter("@idrota", MySqlDbType.Int32);
				pIdRota.Value = idRota;

				MySqlCommand cmdSelect = new MySqlCommand("SELECT idrotaconducao, idrota, enderecoembarque, linha, enderecodesembarque FROM `homeaccess4`.`rotaconducao` WHERE idrota = @idRota", myConnection);
				cmdSelect.Parameters.Add(pIdRota);
				MySqlDataReader dr = cmdSelect.ExecuteReader();

				DataTable lista = new DataTable();

				lista.Columns.Add("idrotaconducao");
				lista.Columns.Add("idrota");
				lista.Columns.Add("enderecoembarque");
				lista.Columns.Add("linha");
				lista.Columns.Add("enderecodesembarque");

				if (dr.HasRows)
				{
					while (dr.Read())
					{
						string idrotaconducao = dr["idrotaconducao"].ToString();
						idRota = int.Parse(dr["idrota"].ToString());
						string enderecoembarque = dr["enderecoembarque"].ToString();
						string linha = dr["linha"].ToString();
						string enderecodesembarque = dr["enderecodesembarque"].ToString();

						lista.Rows.Add(idrotaconducao, idRota.ToString(), enderecoembarque, linha, enderecodesembarque);
					}
				}

				return lista;
			}
			catch (Exception ex)
			{
				throw new Exception(ex.Message);
			}
			finally
			{
				FecharConexao();
			}
		}
	}
}