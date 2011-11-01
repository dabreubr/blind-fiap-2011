using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Data;

namespace AdmiSee.Web
{
	public partial class CadastrarRotas : System.Web.UI.Page
	{
		#region - Propriedades - 

		private DataTable rotas = new DataTable();

		#endregion

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

		#region - btnAdicionar_Click - 
		/// <summary>
		/// 
		/// </summary>
		/// <param name="sender"></param>
		/// <param name="e"></param>
		protected void btnAdicionar_Click(object sender, ImageClickEventArgs e)
		{
			rotas = RetornaConteudoGrid();
			rotas.Rows.Add(txtEnderecoEmbarque.Text.Trim(), txtLinha.Text.Trim(), txtEnderecoDesembarque.Text.Trim());
			CarregarGrid();
			LimparCamposConducao();
		}
		#endregion

		#region - btnLimpar_Click - 
		/// <summary>
		/// 
		/// </summary>
		/// <param name="sender"></param>
		/// <param name="e"></param>
		protected void btnLimpar_Click(object sender, ImageClickEventArgs e)
		{
			LimparCamposConducao();
			LimparCamposTela();
		}
		#endregion

		#region - btnSalvar_Click - 
		/// <summary>
		/// 
		/// </summary>
		/// <param name="sender"></param>
		/// <param name="e"></param>
		protected void btnSalvar_Click(object sender, ImageClickEventArgs e)
		{
			DAO dao = new DAO();
			rotas = RetornaConteudoGrid();
			bool inseriu = dao.InsereRota(txtEnderecoOrigem.Text.Trim(), txtEnderecoDestino.Text.Trim(), short.Parse(txtQuantidadeConducoes.Text.Trim()), txtTempoViagem.Text.Trim(), txtValorTarifas.Text.Trim(), rotas);
			if (inseriu)
			{
				ExibePanelForm(false);
				lblMensagem.Text = "Inserido com sucesso";
			}
			else
			{
				ExibePanelForm(true);
				lblMensagem.Text = "";
			}
		}
		#endregion

		#region - gvRotas_RowDeleting - 
		/// <summary>
		/// 
		/// </summary>
		/// <param name="sender"></param>
		/// <param name="e"></param>
		protected void gvRotas_RowDeleting(object sender, GridViewDeleteEventArgs e)
		{
			rotas = RetornaConteudoGrid();

			rotas.Rows.RemoveAt(e.RowIndex);

			CarregarGrid();
		}
		#endregion

		#endregion

		#region - Metodos - 

		#region - ExibePanelForm -
		/// <summary>
		/// 
		/// </summary>
		/// <param name="valor"></param>
		private void ExibePanelForm(bool valor)
		{
			pnlForm.Visible = valor;
			pnlMensagem.Visible = !valor;
		}
		#endregion

		#region - RetornaConteudoGrid -
		/// <summary>
		/// 
		/// </summary>
		/// <returns></returns>
		private DataTable RetornaConteudoGrid()
		{
			rotas.Columns.Add("enderecoEmbarque");
			rotas.Columns.Add("linha");
			rotas.Columns.Add("enderecoDesembarque");

			foreach (GridViewRow gridViewRow in gvRotas.Rows)
			{
				rotas.Rows.Add(gridViewRow.Cells[0].Text, gridViewRow.Cells[1].Text, gridViewRow.Cells[2].Text);
			}
			return rotas;
		}
		#endregion

		#region - CarregarGrid -
		/// <summary>
		/// 
		/// </summary>
		private void CarregarGrid()
		{
			gvRotas.DataSource = rotas;
			gvRotas.DataBind();
		}
		#endregion

		#region - LimparCamposConducao - 
		/// <summary>
		/// 
		/// </summary>
		private void LimparCamposConducao()
		{
			txtEnderecoEmbarque.Text = string.Empty;
			txtLinha.Text = string.Empty;
			txtEnderecoDesembarque.Text = string.Empty;
		}
		#endregion

		#region - LimparCamposTela - 
		/// <summary>
		/// 
		/// </summary>
		private void LimparCamposTela()
		{
			txtEnderecoOrigem.Text = string.Empty;
			txtEnderecoDestino.Text = string.Empty;
			txtQuantidadeConducoes.Text = string.Empty;
			txtTempoViagem.Text = string.Empty;
			txtValorTarifas.Text = string.Empty;
		}
		#endregion

		#endregion
	}
}
