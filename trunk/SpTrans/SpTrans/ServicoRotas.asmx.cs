using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Services;
using System.Xml;
using System.Text;
using System.Data;
using MySql.Data.MySqlClient;
using System.Configuration;
using System.Web.Script.Services;
using System.Web.Script.Serialization;
using System.Net.Mail;
using System.Net;

namespace SpTrans
{
    [WebService(Namespace = "http://tempuri.org/")]
    [WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
    [System.ComponentModel.ToolboxItem(false)]
    [System.Web.Script.Services.ScriptService]
    public class ServicoRotas : System.Web.Services.WebService
    {
		private static string constr = ConfigurationManager.AppSettings["connectionString"];
		private MySqlConnection myConnection = new MySqlConnection(constr);

		/// <summary>
		/// Abre a conexão com o banco
		/// </summary>
        private void AbrirConexao()
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
        private void FecharConexao()
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
        /// Método que retorna o trajeto a ser feito entr dois pontos, utilizando transporte público.
        /// </summary>
        /// <param name="enderecoOrigem">Endereço de origem</param>
        /// <param name="enderecoDestino">Endereço de destino</param>
        /// <returns>XML com o trajeto</returns>
        [WebMethod]
        public XmlDocument TracarRota(string enderecoOrigem, string enderecoDestino)
        {
			XmlDocument xmlDoc = new XmlDocument();

            try
            {
				xmlDoc.Load(Server.MapPath("xml/Rota.xml"));
				return xmlDoc;
            }
            catch (Exception ex)
            {
				XmlNode Erro = xmlDoc.CreateNode(XmlNodeType.Element, "Erro: " + ex.InnerException, null);
				xmlDoc.AppendChild(Erro);
				return xmlDoc;
            }
        }

		/// <summary>
		/// Método que limpa a base de dados das posições dos ônibus
		/// </summary>
		/// <returns></returns>
        [WebMethod]
        public int LimparPosicaoOnibus()
        {
            try
            {
                AbrirConexao();

				MySqlCommand cmdDelete = new MySqlCommand("DELETE FROM `homeaccess4`.`posicaoonibus`", myConnection);
                return Convert.ToInt32(cmdDelete.ExecuteNonQuery());
            }
            catch (Exception ex)
            {
                return 0;
            }
            finally
            {
                FecharConexao();
            }
        }

		/// <summary>
		/// Método que faz a manutenção na base de posições, insere e altera
		/// </summary>
		/// <param name="linha">Identificação da linha</param>
		/// <param name="onibus">Identificação do ônibus</param>
		/// <param name="coordenadaX">Coordenada de localização X</param>
		/// <param name="coordenadaY">Coordenada de localização Y</param>
		/// <returns></returns>
        [WebMethod]
        public bool InserePosicaoOnibus(string linha, string onibus, string coordenadaX, string coordenadaY)
        {
            try
            {
                AbrirConexao();

                Int32 retorno = 0;

				MySqlParameter pLinha = new MySqlParameter("@linha", MySqlDbType.VarChar);
                pLinha.Value = linha;

				MySqlParameter pOnibus = new MySqlParameter("@onibus", MySqlDbType.VarChar);
                pOnibus.Value = onibus;

				MySqlParameter pcoordenadaX = new MySqlParameter("@coordenadaX", MySqlDbType.VarChar);
                pcoordenadaX.Value = coordenadaX;

				MySqlParameter pcoordenadaY = new MySqlParameter("@coordenadaY", MySqlDbType.VarChar);
                pcoordenadaY.Value = coordenadaY;

				MySqlCommand cmdSelect = new MySqlCommand("SELECT idPosicaoOnibus FROM `homeaccess4`.`posicaoonibus` WHERE linha = @linha and onibus = @onibus", myConnection);
                cmdSelect.Parameters.Add(pLinha);
                cmdSelect.Parameters.Add(pOnibus);
				MySqlDataReader dr = cmdSelect.ExecuteReader();

				if (!dr.HasRows)
                {
					dr.Close();

                    //Inserir
					MySqlCommand cmdInsert = new MySqlCommand("INSERT `homeaccess4`.`posicaoonibus` (linha, onibus, coordenadaX, coordenadaY) VALUES (@Linha , @onibus, @coordenadaX, @coordenaday)", myConnection);

                    cmdInsert.Parameters.Add(pLinha);
                    cmdInsert.Parameters.Add(pOnibus);
                    cmdInsert.Parameters.Add(pcoordenadaX);
                    cmdInsert.Parameters.Add(pcoordenadaY);
                    retorno = cmdInsert.ExecuteNonQuery();
                }
                else
                {
                    //Alterar

					Int32 idPosicaoOnibus = 0;

					if(dr.Read())
					{
						idPosicaoOnibus = Convert.ToInt32(dr["idPosicaoOnibus"]);
					}

					dr.Close();

					MySqlParameter pIdPosicaoOnibus = new MySqlParameter("@idPosicaoOnibus", MySqlDbType.Int32);
					pIdPosicaoOnibus.Value = idPosicaoOnibus;

					MySqlCommand cmdInsert = new MySqlCommand("UPDATE `homeaccess4`.`posicaoonibus` SET coordenadaX = @coordenadaX, coordenadaY = @coordenadaY where idPosicaoOnibus = @idPosicaoOnibus", myConnection);
                    cmdInsert.Parameters.Add(pcoordenadaX);
                    cmdInsert.Parameters.Add(pcoordenadaY);
					cmdInsert.Parameters.Add(pIdPosicaoOnibus);
                    retorno = cmdInsert.ExecuteNonQuery();
                }

                return (retorno > 0);
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
		/// Método que retora a posicão de um ou mais ônibus, se não quiser filtrar por algum parametro, deixeo em branco
		/// </summary>
		/// <param name="linha">Identificação da linha</param>
		/// <param name="onibus">Identificação do ônibus</param>
		/// <returns></returns>
        [WebMethod]
		[ScriptMethod(ResponseFormat = ResponseFormat.Json)]
        public XmlDocument RetornaPosicaoOnibus(string linha, string onibus)
        {
			XmlDocument xmlDoc = new XmlDocument();

			try
			{
				AbrirConexao();

				MySqlParameter pLinha = new MySqlParameter("@linha", MySqlDbType.VarChar);
				pLinha.Value = linha;

				MySqlParameter pOnibus = new MySqlParameter("@onibus", MySqlDbType.VarChar);
				pOnibus.Value = onibus;

				MySqlCommand cmdSelect = new MySqlCommand("SELECT idPosicaoOnibus, linha, onibus, coordenadaX, coordenadaY FROM `homeaccess4`.`posicaoonibus` WHERE (linha = @linha or @linha = '') and (onibus = @onibus or @onibus = '')", myConnection);
				cmdSelect.Parameters.Add(pLinha);
				cmdSelect.Parameters.Add(pOnibus);
				MySqlDataReader drSelect = cmdSelect.ExecuteReader();

				if (drSelect.HasRows)
				{
					XmlNode PosicaoOnibusDataList = xmlDoc.CreateNode(XmlNodeType.Element, "PosicaoOnibusDataList", null);

					while (drSelect.Read())
					{
						XmlAttribute xmlAttribute;

						XmlNode PosicaoOnibus = xmlDoc.CreateNode(XmlNodeType.Element, "PosicaoOnibus", null);

						XmlNode idPosicaoOnibus = xmlDoc.CreateNode(XmlNodeType.Element, "idPosicaoOnibus", null);
						xmlAttribute = xmlDoc.CreateAttribute("Value");
						xmlAttribute.Value = drSelect["idPosicaoOnibus"].ToString();
						idPosicaoOnibus.Attributes.Append(xmlAttribute);
						PosicaoOnibus.AppendChild(idPosicaoOnibus);

						XmlNode xmlLinha = xmlDoc.CreateNode(XmlNodeType.Element, "linha", null);
						xmlAttribute = xmlDoc.CreateAttribute("Value");
						xmlAttribute.Value = drSelect["linha"].ToString();
						xmlLinha.Attributes.Append(xmlAttribute);
						PosicaoOnibus.AppendChild(xmlLinha);

						XmlNode xmlOnibus = xmlDoc.CreateNode(XmlNodeType.Element, "onibus", null);
						xmlAttribute = xmlDoc.CreateAttribute("Value");
						xmlAttribute.Value = drSelect["onibus"].ToString();
						xmlOnibus.Attributes.Append(xmlAttribute);
						PosicaoOnibus.AppendChild(xmlOnibus);

						XmlNode xmlCoordenadaX = xmlDoc.CreateNode(XmlNodeType.Element, "coordenadaX", null);
						xmlAttribute = xmlDoc.CreateAttribute("Value");
						xmlAttribute.Value = drSelect["coordenadaX"].ToString();
						xmlCoordenadaX.Attributes.Append(xmlAttribute);
						PosicaoOnibus.AppendChild(xmlCoordenadaX);

						XmlNode xmlCoordenadaY = xmlDoc.CreateNode(XmlNodeType.Element, "coordenadaY", null);
						xmlAttribute = xmlDoc.CreateAttribute("Value");
						xmlAttribute.Value = drSelect["coordenadaY"].ToString();
						xmlCoordenadaY.Attributes.Append(xmlAttribute);
						PosicaoOnibus.AppendChild(xmlCoordenadaY);

						PosicaoOnibusDataList.AppendChild(PosicaoOnibus);

						xmlDoc.AppendChild(PosicaoOnibusDataList);
					}
				}
				return xmlDoc;
			}
			catch (Exception ex)
			{
				XmlNode Erro = xmlDoc.CreateNode(XmlNodeType.Element, "Erro: " + ex.InnerException, null);
				xmlDoc.AppendChild(Erro);
				return xmlDoc;
			}
			finally
			{
				FecharConexao();
			}
        }

		/// <summary>
		/// Método que retora a posicão de um ou mais ônibus, se não quiser filtrar por algum parametro, deixeo em branco
		/// </summary>
		/// <param name="linha">Identificação da linha</param>
		/// <param name="onibus">Identificação do ônibus</param>
		/// <returns></returns>
		[WebMethod]
		[ScriptMethod(ResponseFormat = ResponseFormat.Json)]
		public XmlDocument TracarRotaBD(string enderecoOrigem, string enderecoDestino)
		{
			XmlDocument xmlDoc = new XmlDocument();

			try
			{
				AbrirConexao();

				string idRota = string.Empty;

				MySqlParameter pEnderecoOrigem = new MySqlParameter("@enderecoOrigem", MySqlDbType.VarChar);
				pEnderecoOrigem.Value = enderecoOrigem;

				MySqlParameter pEnderecoDestino = new MySqlParameter("@enderecoDestino", MySqlDbType.VarChar);
				pEnderecoDestino.Value = enderecoDestino;

				//string comando = "SELECT idrota, enderecoorigem, enderecodestino, quantidadeconducoes, tempoviagem, valortarifas FROM `homeaccess4`.`rota` WHERE (rota.enderecoorigem = @enderecoOrigem) and (rota.enderecodestino = @enderecoDestino)";
				string comando = "SELECT rota.idrota, rota.enderecoorigem, rota.enderecodestino, rota.quantidadeconducoes, rota.tempoviagem, rota.valortarifas, rotaconducao.idrotaconducao, rotaconducao.enderecoEmbarque, rotaconducao.linha, rotaconducao.enderecoDesembarque FROM `homeaccess4`.`rota` rota inner join `homeaccess4`.`rotaconducao` rotaconducao on rota.idrota = rotaconducao.idrota WHERE (rota.enderecoorigem = @enderecoOrigem) and (rota.enderecodestino = @enderecoDestino);";
				MySqlCommand cmdRota = new MySqlCommand(comando, myConnection);
				cmdRota.Parameters.Add(pEnderecoOrigem);
				cmdRota.Parameters.Add(pEnderecoDestino);
				MySqlDataReader drRota = cmdRota.ExecuteReader();

				if (drRota.HasRows)
				{

					XmlNode RotaBD = xmlDoc.CreateNode(XmlNodeType.Element, "RotaBD", null);

					int i = 1;

					XmlNode rota = null;

					while (drRota.Read())
					{

						if (idRota != drRota["idrota"].ToString())
						{
							rota = xmlDoc.CreateNode(XmlNodeType.Element, "rota", null);

							XmlNode xmlQuantidadeConducoes = xmlDoc.CreateNode(XmlNodeType.Element, "quantidadeConducao", null);
							xmlQuantidadeConducoes.InnerXml = drRota["quantidadeConducoes"].ToString();
							rota.AppendChild(xmlQuantidadeConducoes);

							XmlNode xmlTempoViagem = xmlDoc.CreateNode(XmlNodeType.Element, "tempoViagem", null);
							xmlTempoViagem.InnerXml = drRota["tempoViagem"].ToString();
							rota.AppendChild(xmlTempoViagem);

							XmlNode xmlValorTarifas = xmlDoc.CreateNode(XmlNodeType.Element, "valorTotalTarifa", null);
							xmlValorTarifas.InnerXml = drRota["valorTarifas"].ToString();
							rota.AppendChild(xmlValorTarifas);

							XmlNode xmlEnderecoOrigem = xmlDoc.CreateNode(XmlNodeType.Element, "enderecoInicial", null);
							xmlEnderecoOrigem.InnerXml = drRota["enderecoOrigem"].ToString();
							rota.AppendChild(xmlEnderecoOrigem);

							XmlNode xmlEnderecoDestino = xmlDoc.CreateNode(XmlNodeType.Element, "enderecoFinal", null);
							xmlEnderecoDestino.InnerXml = drRota["enderecoDestino"].ToString();
							rota.AppendChild(xmlEnderecoDestino);

							i = 1;
						}

						XmlNode xmlEnderecoembarque = xmlDoc.CreateNode(XmlNodeType.Element, "enderecoEmbarque" + i, null);
						xmlEnderecoembarque.InnerXml = drRota["enderecoEmbarque"].ToString();
						rota.AppendChild(xmlEnderecoembarque);

						XmlNode xmlLinha = xmlDoc.CreateNode(XmlNodeType.Element, "linha" + i, null);
						xmlLinha.InnerXml = drRota["linha"].ToString();
						rota.AppendChild(xmlLinha);

						XmlNode xmlEnderecoDesembarque = xmlDoc.CreateNode(XmlNodeType.Element, "enderecoDesembarque" + i, null);
						xmlEnderecoDesembarque.InnerXml = drRota["enderecoDesembarque"].ToString();
						rota.AppendChild(xmlEnderecoDesembarque);

						RotaBD.AppendChild(rota);

						xmlDoc.AppendChild(RotaBD);

						idRota = drRota["idrota"].ToString();
						i++;
					}
				}
				else
				{
					XmlNode RegistroNaoEncontrado = xmlDoc.CreateNode(XmlNodeType.Element, "RegistroNaoEncontrado", null);
					xmlDoc.AppendChild(RegistroNaoEncontrado);
				}

				return xmlDoc;
			}
			catch (Exception ex)
			{
				XmlNode Erro = xmlDoc.CreateNode(XmlNodeType.Element, "Erro: " + ex.InnerException, null);
				xmlDoc.AppendChild(Erro);
				return xmlDoc;
			}
			finally
			{
				FecharConexao();
			}
		}

		/// <summary>
		/// Método que retora a posicão de um ou mais ônibus, se não quiser filtrar por algum parametro, deixeo em branco
		/// </summary>
		/// <param name="linha">Identificação da linha</param>
		/// <param name="onibus">Identificação do ônibus</param>
		/// <returns></returns>
        [WebMethod]
		[ScriptMethod(ResponseFormat = ResponseFormat.Json)]
        public String RetornaPosicaoOnibusNovo(string linha, string onibus)
        {
			try
			{
				AbrirConexao();

				MySqlParameter pLinha = new MySqlParameter("@linha", MySqlDbType.VarChar);
				pLinha.Value = linha;

				MySqlParameter pOnibus = new MySqlParameter("@onibus", MySqlDbType.VarChar);
				pOnibus.Value = onibus;

				MySqlCommand cmdSelect = new MySqlCommand("SELECT idPosicaoOnibus, linha, onibus, coordenadaX, coordenadaY FROM `homeaccess4`.`posicaoonibus` WHERE (linha = @linha or @linha = '') and (onibus = @onibus or @onibus = '')", myConnection);
				cmdSelect.Parameters.Add(pLinha);
				cmdSelect.Parameters.Add(pOnibus);
				MySqlDataReader drSelect = cmdSelect.ExecuteReader();

				// Lista principal
				List<object> obj = new List<object>();

				if (drSelect.HasRows)
				{
					while (drSelect.Read())
					{
						PosicaoOnibus posicaoOnibus = new SpTrans.PosicaoOnibus();

						posicaoOnibus.idPosicaoOnibus = drSelect["idPosicaoOnibus"].ToString();
						posicaoOnibus.linha = drSelect["linha"].ToString();
						posicaoOnibus.onibus = drSelect["onibus"].ToString();
						posicaoOnibus.coordenadaX = drSelect["coordenadaX"].ToString();
						posicaoOnibus.coordenadaY = drSelect["coordenadaY"].ToString();

						obj.Add(posicaoOnibus);
					}
				}

				// Serializar objeto em formato JSON
				var js = new JavaScriptSerializer();
				return js.Serialize(obj);
			}
			catch (Exception ex)
			{
				return ex.Message;
			}
			finally
			{
				FecharConexao();
			}
        }

		/// <summary>
		/// Método que retora a posicão de um ou mais ônibus, se não quiser filtrar por algum parametro, deixeo em branco
		/// </summary>
		/// <param name="linha">Identificação da linha</param>
		/// <param name="onibus">Identificação do ônibus</param>
		/// <returns></returns>
		[WebMethod]
		public bool EmailCadastroRota(string enderecoOrigem, string enderecoDestino)
		{
			try
			{
				AbrirConexao();

				Int32 retorno = 0;

				MySqlParameter pEnderecoOrigem = new MySqlParameter("@enderecoOrigem", MySqlDbType.VarChar);
				pEnderecoOrigem.Value = enderecoOrigem;

				MySqlParameter pEnderecoDestino = new MySqlParameter("@enderecoDestino", MySqlDbType.VarChar);
				pEnderecoDestino.Value = enderecoDestino;

				MySqlCommand cmdInsert = new MySqlCommand("INSERT `homeaccess4`.`emailcadastrorota` (enderecoOrigem, enderecoDestino) VALUES (@enderecoOrigem , @enderecoDestino)", myConnection);

				cmdInsert.Parameters.Add(pEnderecoOrigem);
				cmdInsert.Parameters.Add(pEnderecoDestino);
					
				retorno = cmdInsert.ExecuteNonQuery();

				EnviarEmail(enderecoOrigem, enderecoDestino);

				return (retorno > 0);
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

		private void EnviarEmail(string enderecoOrigem, string enderecoDestino)
		{
			MailMessage mailMessage = new MailMessage();
			mailMessage.From = new MailAddress(ConfigurationManager.AppSettings["emailRemetente"]);
			mailMessage.To.Add(new MailAddress(ConfigurationManager.AppSettings["emailDestinatario"]));
			mailMessage.Subject = ConfigurationManager.AppSettings["assunto"];

			mailMessage.Body = string.Format(ConfigurationManager.AppSettings["mensagem"], enderecoOrigem, enderecoDestino);

			SmtpClient smtpClient = new SmtpClient(ConfigurationManager.AppSettings["smtp"], int.Parse(ConfigurationManager.AppSettings["porta"]));
			smtpClient.EnableSsl = false; //True ou False dependendo se o seu servidor exige SSL

			//NetworkCredential credenciais = new NetworkCredential(ConfigurationManager.AppSettings["emailLogin"], ConfigurationManager.AppSettings["pwd"]);
			//smtpClient.Credentials = credenciais;
			//smtpClient.UseDefaultCredentials = true;

			Object mailState = mailMessage;

			//este código cria o gerenciador de evento que vai notificar se o email foi enviado ou não

			smtpClient.SendCompleted += new SendCompletedEventHandler(smtpClient_SendCompleted);
			try
			{
				smtpClient.SendAsync(mailMessage, mailState);
			}
			catch (Exception ex)
			{

			}
		}

		//O segundo bloco de código é justamente aquele que vai gerenciar o status do envio, permitindo saber se a mensagem foi enviada com sucesso ou não:

		void smtpClient_SendCompleted(object sender, System.ComponentModel.AsyncCompletedEventArgs e)
		{
			MailMessage mailMessage = e.UserState as MailMessage;

			if (!e.Cancelled && e.Error != null)
			{
				string msg = "enviado com sucesso";
			}
			else
			{
				string msg = "enviado NAO com sucesso";
			}
		}

		/*
		private void EnviarEmail(string enderecoOrigem, string enderecoDestino)
		{
			try
			{

				//Cria o objeto que envia o e-mail
				SmtpClient cliente = new SmtpClient("smtp.gmail.com", 587);
				cliente.EnableSsl = true;
				
				NetworkCredential credenciais = new NetworkCredential("angelo.rici@gmail.com", "*#06660#*");

				cliente.Credentials = credenciais;

				//Cria o endereço de email do remetente
				MailAddress de = new MailAddress(ConfigurationManager.AppSettings["emailRemetente"]);

				//Cria o endereço de email do destinatário -->
				MailAddress para = new MailAddress(ConfigurationManager.AppSettings["emailDestinatario"]);

				MailMessage mensagem = new MailMessage(de, para);
				mensagem.IsBodyHtml = true;

				//Assunto do email
				mensagem.Subject = ConfigurationManager.AppSettings["assunto"];

				string corpoMensagem = "Cadastrar o xml para o enderecoOrigem: " + enderecoOrigem + " e o enderecoDestino: " + enderecoDestino;

				//Conteúdo do email
				mensagem.Body = corpoMensagem;

				//Envia o email
				cliente.Send(mensagem);
			}
			catch (Exception ex)
			{
			}
		}
		*/
	}
}