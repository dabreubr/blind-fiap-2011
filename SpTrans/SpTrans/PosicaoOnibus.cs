using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SpTrans
{
	public class PosicaoOnibus
	{
		public String idPosicaoOnibus { get; set; }
		public String linha { get; set; }
		public String onibus { get; set; }
		public String coordenadaX { get; set; }
		public String coordenadaY { get; set; }

		public PosicaoOnibus()
		{
		}
	}
}