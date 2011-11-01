<%@ Page Title="" Language="C#" MasterPageFile="~/MasterPage/Principal.Master" AutoEventWireup="true" CodeBehind="ConsultarRotas.aspx.cs" Inherits="AdmiSee.Web.ConsultarRotas" %>

<asp:Content ID="Content1" ContentPlaceHolderID="ContentPlaceHolder1" runat="server">
	<asp:UpdatePanel ID="uppMaster" runat="server">
		<ContentTemplate>
			<table cellpadding="0" cellspacing="0" class="tablePrincipal">
				<tr>
					<td align="center" valign="top">
						<span class="spanTitulo">Consultar Rotas</span>
					</td>
				</tr>
				<tr>
					<td height="10" valign="middle" align="center">
						<asp:Image ID="Image2" runat="server" ImageUrl="~/Image/LinhaHorizontal.gif" />
					</td>
				</tr>
				<tr>
					<td align="center">
						<table border="0" width="100%" cellpadding="0" cellspacing="0">
							<tr>
								<td colspan="2">
									<fieldset>
										<legend>Pesquisa </legend>
										<table border="0" width="100%" cellpadding="0" cellspacing="0">
											<tr>
												<td align="left" width="20%">
													<span>Endereço de origem:</span>
												</td>
												<td align="left" width="80%">
													<asp:TextBox ID="txtEnderecoOrigem" runat="server" CssClass="TxtMedio" MaxLength="500"></asp:TextBox>
												</td>
											</tr>
											<tr>
												<td colspan="2" height="5">
												</td>
											</tr>
											<tr>
												<td align="left">
													<span>Endereço de destino:</span>
												</td>
												<td align="left">
													<asp:TextBox ID="txtEnderecoDestino" runat="server" CssClass="TxtMedio" MaxLength="500"></asp:TextBox>
												</td>
											</tr>
											<tr>
												<td colspan="2" height="5">
												</td>
											</tr>
											<tr>
												<td colspan="2">
													<asp:ImageButton ID="btnPesquisar" runat="server" ImageUrl="~/Image/btnPesquisar.gif" ValidationGroup="vgFormulario" OnClick="btnPesquisar_Click" />
													<asp:ImageButton ID="btnLimpar" runat="server" ImageUrl="~/Image/btnLimpar.gif" OnClick="btnLimpar_Click" />
													<asp:ImageButton ID="btnVoltar" runat="server" ImageUrl="~/Image/btnVoltar.gif" OnClick="btnVoltar_Click" />
												</td>
											</tr>
										</table>
									</fieldset>
								</td>
							</tr>
							<tr>
								<td colspan="2" height="5">
								</td>
							</tr>
							<tr>
								<td height="10" valign="middle" align="center" colspan="2">
									<asp:Image ID="Image3" runat="server" ImageUrl="~/Image/LinhaHorizontal.gif" />
								</td>
							</tr>
							<tr>
								<td colspan="2" height="5">
								</td>
							</tr>
							<tr>
								<td colspan="2">
									<fieldset>
										<legend>Rotas </legend>
										<table border="0" width="100%" cellpadding="0" cellspacing="0">
											<tr>
												<td colspan="2" height="5">
												</td>
											</tr>
											<tr>
												<td colspan="2" align="center">
													<asp:GridView ID="gvRota" runat="server" DataKeyNames="idrota" AutoGenerateColumns="False" onselectedindexchanged="gvRota_SelectedIndexChanged">
														<Columns>
															<asp:BoundField DataField="enderecoorigem" HeaderText="Endereço de origem" />
															<asp:BoundField DataField="enderecodestino" HeaderText="Endereço de destino" />
															<asp:BoundField DataField="quantidadeconducoes" HeaderText="Quantidade de conducções" />
															<asp:BoundField DataField="tempoviagem" HeaderText="Tempo de viagem" />
															<asp:BoundField DataField="valortarifas" HeaderText="Valor total de tarifas" />
															<asp:CommandField ButtonType="Image" SelectImageUrl="~/Image/imgEditar.jpg" ShowSelectButton="True" />
														</Columns>
													</asp:GridView>
												</td>
											</tr>
											<tr>
												<td colspan="2" height="5">
												</td>
											</tr>
										</table>
									</fieldset>
								</td>
							</tr>
							<tr>
								<td colspan="2" height="5">
								</td>
							</tr>
							<tr>
								<td colspan="2">
									<fieldset>
										<legend>Conduções </legend>
										<table border="0" width="100%" cellpadding="0" cellspacing="0">
											<tr>
												<td colspan="2" height="5">
												</td>
											</tr>
											<tr>
												<td colspan="2" align="center">
													<asp:GridView ID="gvRotaConducao" runat="server" AutoGenerateColumns="False" AllowPaging="false" AllowSorting="false">
														<Columns>
															<asp:BoundField DataField="enderecoembarque" HeaderText="Endereço de embarque" />
															<asp:BoundField DataField="linha" HeaderText="Linha" />
															<asp:BoundField DataField="enderecodesembarque" HeaderText="Endereço de desembarque" />
														</Columns>
													</asp:GridView>
												</td>
											</tr>
											<tr>
												<td colspan="2" height="5">
												</td>
											</tr>
										</table>
									</fieldset>
								</td>
							</tr>
						</table>
					</td>
				</tr>
				<tr>
					<td height="10" valign="middle" align="center">
						<asp:Image ID="Image1" runat="server" ImageUrl="~/Image/LinhaHorizontal.gif" />
					</td>
				</tr>
			</table>
		</ContentTemplate>
	</asp:UpdatePanel>
</asp:Content>
