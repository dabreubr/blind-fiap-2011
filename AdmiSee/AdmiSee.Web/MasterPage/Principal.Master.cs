using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;

namespace AdmiSee.Web.MasterPage
{
    public partial class Principal : System.Web.UI.MasterPage
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
			DesabilitaItens();
        }
		#endregion

		#region - btnSair_Click -
		/// <summary>
		/// 
		/// </summary>
		/// <param name="sender"></param>
		/// <param name="e"></param>
		protected void btnSair_Click(object sender, ImageClickEventArgs e)
		{
			Session.Abandon();
			Response.Redirect("Default.aspx");
		}
		#endregion

		#endregion

		#region - Métodos -

		#region - DesabilitaItens -
		/// <summary>
		/// 
		/// </summary>
		public void DesabilitaItens()
		{
			if (Session["login"] == null)
			{
				TratarMenu(false);
				btnSair.Visible = false;
			}
			else
			{
				TratarMenu(Session["login"].Equals(true));
				btnSair.Visible = Session["login"].Equals(true);
			}
		}

		private void TratarMenu(bool valor)
		{
			hlConfiguracoes.Enabled = valor;
			hlLimites.Enabled = valor;
			hlMonitoramento.Enabled = valor;
			hlDesenvolvimento.Enabled = valor;
		}
		#endregion
	
		#endregion
	}
}
