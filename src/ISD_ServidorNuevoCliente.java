
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISD_ServidorNuevoCliente extends Remote
{
	public ISD_Cliente Registrar(String ip) throws RemoteException, Excepciones.SD_YaRegistrado;
	public ISD_Cliente IniciarSesion(String token) throws RemoteException, Excepciones.SD_NoRegistrado;
}
