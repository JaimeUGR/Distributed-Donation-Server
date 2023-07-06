
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISD_Cliente extends Remote
{
	public void HacerDonacion(int cantidad) throws RemoteException, Excepciones.SD_NoRegistrado;
	public int ConsultaTotalDonado() throws RemoteException, Excepciones.SD_NoRegistrado, Excepciones.SD_NoDonado;
	public int ConsultaTotalDonaciones() throws RemoteException, Excepciones.SD_NoRegistrado, Excepciones.SD_NoDonado;
	public int ConsultaMiTotalDonado() throws RemoteException, Excepciones.SD_NoRegistrado;
	public int ConsultaMisDonaciones() throws RemoteException, Excepciones.SD_NoRegistrado;
	public void CerrarSesion() throws RemoteException;
	public int ConsultaIDReplica() throws RemoteException;
}
