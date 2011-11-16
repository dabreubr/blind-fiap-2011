<%@ Page Title="" Language="C#" MasterPageFile="~/MasterPage/Principal.Master" AutoEventWireup="true" CodeBehind="CadastrarRotas.aspx.cs" Inherits="AdmiSee.Web.CadastrarRotas" %>

<asp:Content ID="Content1" ContentPlaceHolderID="ContentPlaceHolder1" runat="server">
	<table cellpadding="0" cellspacing="0" class="tablePrincipal">
		<tr>
			<td align="center" valign="top">
				<span class="spanTitulo">Cadastrar Rotas</span>
			</td>
		</tr>
		<tr>
			<td height="10" valign="middle" align="center">
				<asp:Image ID="Image1" runat="server" ImageUrl="~/Image/LinhaHorizontal.gif" />
			</td>
		</tr>
		<tr>
			<td align="center">
				<asp:Panel runat="server" ID="pnlForm">
					<table border="0" width="100%" cellpadding="0" cellspacing="0">
						<tr>
							<td align="left" width="20%">
								<span>Endereço de origem:</span>
							</td>
							<td align="left" width="80%">
								<asp:TextBox ID="txtEnderecoOrigem" runat="server" CssClass="TxtMedio" MaxLength="500"></asp:TextBox>
								<asp:RequiredFieldValidator runat="server" ID="rfvEnderecoOrigem" Text="*" ErrorMessage="Preencha o campo Endereço de origem" ControlToValidate="txtEnderecoOrigem" ValidationGroup="vgFormulario"></asp:RequiredFieldValidator>
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
								<asp:RequiredFieldValidator runat="server" ID="rfvEnderecoDestino" Text="*" ErrorMessage="Preecnha o campo Endereço de destino" ControlToValidate="txtEnderecoDestino" ValidationGroup="vgFormulario"></asp:RequiredFieldValidator>
							</td>
						</tr>
						<tr>
							<td colspan="2" height="5">
							</td>
						</tr>
						<tr>
							<td align="left">
								<span>Quantidade de conduções:</span>
							</td>
							<td align="left">
								<asp:TextBox ID="txtQuantidadeConducoes" runat="server" CssClass="TxtMedio" MaxLength="2" alt="short"></asp:TextBox>
								<asp:RequiredFieldValidator runat="server" ID="rfvQuantidadeConducoes" Text="*" ErrorMessage="Preencha o campo Quantidade de conduções" ControlToValidate="txtQuantidadeConducoes" ValidationGroup="vgFormulario"></asp:RequiredFieldValidator>
							</td>
						</tr>
						<tr>
							<td colspan="2" height="5">
							</td>
						</tr>
						<tr>
							<td align="left">
								<span>Tempo de viagem:</span>
							</td>
							<td align="left">
								<asp:TextBox ID="txtTempoViagem" runat="server" CssClass="TxtMedio" MaxLength="5" alt="time"></asp:TextBox>
								<asp:RequiredFieldValidator runat="server" ID="rfvTempoVIagem" Text="*" ErrorMessage="Preencha o campo Tempo de viagem" ControlToValidate="txtTempoVIagem" ValidationGroup="vgFormulario"></asp:RequiredFieldValidator>
							</td>
						</tr>
						<tr>
							<td colspan="2" height="5">
							</td>
						</tr>
						<tr>
							<td align="left">
								<span>Valor total de tarifas:</span>
							</td>
							<td align="left">
								<asp:TextBox ID="txtValorTarifas" runat="server" CssClass="TxtMedio" MaxLength="5" alt="decimal"></asp:TextBox>
								<asp:RequiredFieldValidator runat="server" ID="rfvValorTarifas" Text="*" ErrorMessage="Preencha o campo Valor total de tarifas" ControlToValidate="txtValorTarifas" ValidationGroup="vgFormulario"></asp:RequiredFieldValidator>
							</td>
						</tr>
						<tr>
							<td colspan="2" height="5">
							</td>
						</tr>
						<tr>
							<td colspan="2">
								<fieldset>
									<legend>Inserir condução </legend>
									<table border="0" width="100%" cellpadding="0" cellspacing="0">
										<tr>
											<td align="left" width="20%">
												<span>Endereço de embarque:</span>
											</td>
											<td align="left" width="80%">
												<asp:TextBox ID="txtEnderecoEmbarque" runat="server" CssClass="TxtMedio" MaxLength="500"></asp:TextBox>
												<asp:RequiredFieldValidator runat="server" ID="rfvEnderecoEmbarque" Text="*" ErrorMessage="Preencha o campo Endereço de embarque" ControlToValidate="txtEnderecoEmbarque" ValidationGroup="vgGrid"></asp:RequiredFieldValidator>
											</td>
										</tr>
										<tr>
											<td colspan="2" height="5">
											</td>
										</tr>
										<tr>
											<td align="left">
												<span>Linha:</span>
											</td>
											<td align="left">
												<asp:TextBox ID="txtLinha" runat="server" CssClass="TxtMedio" MaxLength="500"></asp:TextBox>
												<asp:RequiredFieldValidator runat="server" ID="rfvLinha" Text="*" ErrorMessage="Preencha o campo Linha" ControlToValidate="txtLinha" ValidationGroup="vgGrid"></asp:RequiredFieldValidator>
											</td>
										</tr>
										<tr>
											<td colspan="2" height="5">
											</td>
										</tr>
										<tr>
											<td align="left">
												<span>Endereço de desembarque:</span>
											</td>
											<td align="left">
												<asp:TextBox ID="txtEnderecoDesembarque" runat="server" CssClass="TxtMedio" MaxLength="500"></asp:TextBox>
												<asp:RequiredFieldValidator runat="server" ID="rfvEnderecoDesembarque" Text="*" ErrorMessage="Preencha o campo Endereço de desembarque" ControlToValidate="txtEnderecoDesembarque" ValidationGroup="vgGrid"></asp:RequiredFieldValidator>
											</td>
										</tr>
										<tr>
											<td colspan="2" height="5">
											</td>
										</tr>
										<tr>
											<td colspan="2" align="right">
												<asp:ImageButton ID="btnAdicionar" runat="server" ImageUrl="~/Image/btnIncluir.gif" ValidationGroup="vgGrid" OnClick="btnAdicionar_Click" />
												<asp:ImageButton ID="btnLimpar" runat="server" ImageUrl="~/Image/btnLimpar.gif" OnClick="btnLimpar_Click" />
											</td>
										</tr>
										<tr>
											<td colspan="2" height="5">
											</td>
										</tr>
										<tr>
											<td colspan="2" align="center">
												<asp:GridView ID="gvRotas" Width="100%" runat="server" AutoGenerateColumns="False" onrowdeleting="gvRotas_RowDeleting" AllowPaging="false" GridLines="None"  AllowSorting="false">
													<AlternatingRowStyle CssClass="cabecalhoGrdVw_LinhaAlternada" />
													<HeaderStyle CssClass="cabecalhoGrdVw_Linha" Wrap="false" />
													<RowStyle CssClass="rowStyle" />
													<SelectedRowStyle CssClass="rowStyleSelected" />
													<PagerStyle BackColor="#3b70ae" HorizontalAlign="Right" ForeColor="White"/>

													<Columns>
														<asp:BoundField DataField="enderecoEmbarque" HeaderText="Endereço de embarque" />
														<asp:BoundField DataField="linha" HeaderText="Linha" />
														<asp:BoundField DataField="enderecoDesembarque" HeaderText="Endereço de desembarque" />
														<asp:CommandField DeleteImageUrl="~/Image/imgExcluir.jpg" ShowDeleteButton="True" ButtonType="Image" />
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
							<td height="10" colspan="2" valign="middle" align="center">
								<asp:Image ID="Image4" runat="server" ImageUrl="~/Image/LinhaHorizontal.gif" />
							</td>
						</tr>
						<tr>
							<td colspan="2" height="5">
							</td>
						</tr>
						<tr>
							<td colspan="2" align="right">
								<asp:ImageButton ID="btnSalvar" runat="server" ImageUrl="~/Image/btnSalvar.gif" ValidationGroup="vgFormulario" OnClick="btnSalvar_Click" />
							</td>
						</tr>
					</table>
				</asp:Panel>
				<asp:Panel runat="server" ID="pnlMensagem">
					<asp:Label runat="server" ID="lblMensagem"></asp:Label>
					<br /><br />
					<asp:ImageButton ID="btnVoltar" runat="server" ImageUrl="~/Image/btnVoltar.gif" OnClick="btnVoltar_Click" />
				</asp:Panel>
			</td>
		</tr>
		<tr>
			<td height="5">
			</td>
		</tr>
		<tr>
			<td height="10" valign="middle" align="center">
				<asp:Image ID="Image2" runat="server" ImageUrl="~/Image/LinhaHorizontal.gif" />
			</td>
		</tr>
	</table>
	<asp:ValidationSummary runat="server" ShowMessageBox="true" ShowSummary="false" ValidationGroup="vgGrid" ID="vsGrid" />
	<asp:ValidationSummary runat="server" ShowMessageBox="true" ShowSummary="false" ValidationGroup="vgFormulario" ID="vsFormulario" />
</asp:Content>
