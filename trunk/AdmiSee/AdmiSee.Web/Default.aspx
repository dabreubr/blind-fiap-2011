<%@ Page Title="" Language="C#" MasterPageFile="~/MasterPage/Principal.Master" AutoEventWireup="true" CodeBehind="Default.aspx.cs" Inherits="AdmiSee.Web.Default" %>
<asp:Content ID="Content1" ContentPlaceHolderID="ContentPlaceHolder1" runat="server">
<table cellpadding="0" cellspacing="0" class="tablePrincipal">
	<tr>
		<td align="center" valign="top"><span class="spanTitulo">Administrador</span></td>
	</tr>
	<tr>
		<td height="10" valign="middle" align="center"><asp:Image ID="Image1" runat="server" ImageUrl="~/Image/LinhaHorizontal.gif" /></td>
	</tr>
	<asp:MultiView runat="server" ID="mvHome">
		<asp:View runat="server" ID="vwNaoLogado">

			<tr>
				<td align="center">
					<table cellpadding="0" cellspacing="0" class="tablePrincipal">
						<tr>
							<td>Login:</td>
							<td><asp:TextBox ID="txtLogin" runat="server" CssClass="TxtMedio" MaxLength="500"></asp:TextBox></td>
						</tr>
						<tr>
							<td>Senha:</td>
							<td><asp:TextBox ID="txtSenha" runat="server" TextMode="Password" CssClass="TxtMedio" MaxLength="500"></asp:TextBox></td>
						</tr>
						<tr>
							<td colspan="2">
								<asp:ImageButton ID="btnLimpar" runat="server" ImageUrl="~/Image/btnLogin.gif" OnClick="btnLogin_Click" />
							</td>
						</tr>
					</table>
				</td>
			</tr>

		</asp:View>
		<asp:View runat="server" ID="vwLogado">
			<tr>
				<td>Seja bem-vindo</td>
			</tr>
		</asp:View>

	</asp:MultiView>
	<tr>
		<td height="10" valign="middle" align="center"><asp:Image ID="Image6" runat="server" ImageUrl="~/Image/LinhaHorizontal.gif" /></td>
	</tr>
</table>
</asp:Content>
