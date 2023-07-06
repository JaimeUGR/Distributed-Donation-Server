import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClienteDonacion implements ISD_Cliente
{
	private ISD_ServidorCliente servidor;
	private String token;
	private int totalDonado = 0;
	private int numDonaciones = 0;
	public ISD_Cliente refRem;
	public int sesiones = 0;

	ClienteDonacion(ISD_ServidorCliente servidor, String token) throws RemoteException
	{
		super();

		this.servidor = servidor;
		this.token = token;
		this.totalDonado = 0;
		this.numDonaciones = 0;
		this.sesiones = 0;
	}

	//
	// Métodos Cliente
	//
	@Override
	public void HacerDonacion(int cantidad) throws RemoteException
	{
		// Hacer la donación en el servidor
		this.servidor.Cliente_HacerDonacion(this.token, cantidad);
	}

	@Override
	public int ConsultaTotalDonado() throws RemoteException, Excepciones.SD_NoRegistrado, Excepciones.SD_NoDonado
	{
		return this.servidor.Cliente_TotalDonado(this.token);
	}

	public int ConsultaTotalDonaciones() throws RemoteException, Excepciones.SD_NoRegistrado, Excepciones.SD_NoDonado
	{
		return this.servidor.Cliente_TotalDonaciones(this.token);
	}

	public int ConsultaMiTotalDonado() throws RemoteException, Excepciones.SD_NoRegistrado
	{
		return this.servidor.Cliente_MiTotalDonado(this.token);
	}

	@Override
	public int ConsultaMisDonaciones() throws RemoteException, Excepciones.SD_NoDonado
	{
		return this.servidor.Cliente_MisDonaciones(this.token);
	}

	@Override
	public void CerrarSesion() throws RemoteException
	{
		this.servidor.Cliente_CerrarSesion(this.token);
	}

	@Override
	public int ConsultaIDReplica() throws RemoteException
	{
		return this.servidor.Cliente_ConsultarIDReplica();
	}
}
