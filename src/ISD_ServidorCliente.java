
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISD_ServidorCliente extends Remote
{
	public boolean Cliente_HacerDonacion(String token, int cantidad) throws RemoteException, Excepciones.SD_NoRegistrado;

	public int Cliente_TotalDonado(String token) throws RemoteException, Excepciones.SD_NoRegistrado, Excepciones.SD_NoDonado;
	public int Cliente_TotalDonaciones(String token) throws RemoteException, Excepciones.SD_NoRegistrado, Excepciones.SD_NoDonado;

	public int Cliente_MiTotalDonado(String token) throws RemoteException, Excepciones.SD_NoRegistrado;

	public int Cliente_MisDonaciones(String token) throws RemoteException, Excepciones.SD_NoRegistrado;
	public boolean Cliente_CerrarSesion(String token) throws RemoteException, Excepciones.SD_NoRegistrado;
	public int Cliente_ConsultarIDReplica() throws RemoteException;
}
