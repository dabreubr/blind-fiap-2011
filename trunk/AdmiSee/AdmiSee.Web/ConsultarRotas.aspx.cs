using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Data;

namespace AdmiSee.Web
{
	public partial class ConsultarRotas : System.Web.UI.Page
	{
		#region - Eventos -

		#region - Page_Load -
		/// <summary>
		/// 
		/// </summary>
		/// <param name="sender"></param>
		/// <param name="e"></param>
        protected void Page_Load(object sender, EventArgs e)
        {
			if (Session["login"] != null)
			{
				if (!Session["login"].Equals(true))
				{
					Response.Redirect("Default.aspx");
				}
			}
			else
			{
				Response.Redirect("Default.aspx");
			}

			ScriptManager.RegisterStartupScript(this, GetType(), "Alerta", "(function($) {$(function() {$('input:text').setMask();});})(jQuery);", true);
        }
		#endregion

		#region - btnVoltar_Click -
		/// <summary>
		/// 
		/// </summary>
		/// <param name="sender"></param>
		/// <param name="e"></param>
		protected void btnVoltar_Click(object sender, ImageClickEventArgs e)
		{
			Response.Redirect("Default.aspx");
		}
		#endregion

		#region - Eventos -
		/// <summary>
		/// 
		/// </summary>
		/// <param name="sender"></param>
		/// <param name="e"></param>
		protected void btnLimpar_Click(object sender, ImageClickEventArgs e)
		{
			LimparTela();
		}
		#endregion

		#region - btnPesquisar_Click -
		/// <summary>
		/// 
		/// </summary>
		/// <param name="sender"></param>
		/// <param name="e"></param>
		protected void btnPesquisar_Click(object sender, ImageClickEventArgs e)
		{
			try
			{
				LimparGridRota();
				LimparGridRotaConducao();

				DAO dao = new DAO();
				gvRotas.DataSource = dao.RetornaRota(txtEnderecoOrigem.Text.Trim(), txtEnderecoDestino.Text.Trim());
				gvRotas.DataBind();
			}
			catch (Exception ex)
			{
			}
		}
		#endregion

		#region - gvRota_SelectedIndexChanged -
		/// <summary>
		/// 
		/// </summary>
		/// <param name="sender"></param>
		/// <param name="e"></param>
		protected void gvRota_SelectedIndexChanged(object sender, EventArgs e)
		{
			try
			{
				DAO dao = new DAO();
				int idRota = int.Parse(gvRotas.DataKeys[gvRotas.SelectedIndex].Value.ToString());
				gvRotaConducao.DataSource = dao.RetornaRotaConducao(idRota);
				gvRotaConducao.DataBind();
			}
			catch (Exception ex)
			{

			}
		}
		#endregion

		#endregion

		#region - Métodos -

		#region - LimparTela -
		/// <summary>
		/// 
		/// </summary>
		private void LimparTela()
		{
			LimparCampos();
			LimparGridRota();
			LimparGridRotaConducao();
		}
		#endregion

		#region - LimparCampos -
		/// <summary>
		/// 
		/// </summary>
		private void LimparCampos()
		{
			txtEnderecoOrigem.Text = string.Empty;
			txtEnderecoDestino.Text = string.Empty;
		}
		#endregion

		#region - LimparGridRota -
		/// <summary>
		/// 
		/// </summary>
		private void LimparGridRota()
		{
			gvRotas.SelectedIndex = -1;
			gvRotas.DataSource = null;
			gvRotas.DataBind();
		}
		#endregion

		#region - LimparGridRotaConducao -
		/// <summary>
		/// 
		/// </summary>
		private void LimparGridRotaConducao()
		{
			gvRotaConducao.DataSource = null;
			gvRotaConducao.DataBind();
		}
		#endregion

		#endregion
	}
}
