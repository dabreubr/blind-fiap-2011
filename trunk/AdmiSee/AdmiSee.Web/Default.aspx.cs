using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Data;
using AdmiSee.Web.MasterPage;

namespace AdmiSee.Web
{
    public partial class Default : System.Web.UI.Page
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
					mvHome.SetActiveView(vwLogado);
				}
				else
				{
					mvHome.SetActiveView(vwNaoLogado);
				}
			}
			else
			{
				mvHome.SetActiveView(vwNaoLogado);
			}
        }
		#endregion

		#region - btnLogin_Click -
		/// <summary>
		/// 
		/// </summary>
		/// <param name="sender"></param>
		/// <param name="e"></param>
		protected void btnLogin_Click(object sender, ImageClickEventArgs e)
		{
			try
			{
				if (txtLogin.Text.Trim() == "admin" && txtSenha.Text.Trim() == "isee")
				{
					Session["login"] = true;
					mvHome.SetActiveView(vwLogado);

					AdmiSee.Web.MasterPage.Principal master = ((AdmiSee.Web.MasterPage.Principal)this.Master);
					master.DesabilitaItens();
				}
				else
				{
					Session["login"] = false;
				}
			}
			catch (Exception ex)
			{
			}
		}
		#endregion

		#endregion
	}
}
