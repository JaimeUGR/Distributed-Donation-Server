
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ISD_Servidor extends Remote
{
	public ArrayList<ReplicaInfo> GetReplicas() throws RemoteException;
	public int RegistrarReplica(ReplicaInfo replicaInfo) throws RemoteException, Excepciones.SD_ReplicaRegistrada;
	public ReplicaInfo ReplicaInfo() throws RemoteException;
	public ISD_Cliente RegistrarCliente(String token, int repSeleccionada, int repSelecRegistrados) throws RemoteException;
	public ISD_Cliente IniciarSesionServidor(String token) throws RemoteException, Excepciones.SD_NoRegistrado;
	public int EstaClienteRegistrado(String token) throws RemoteException;

	public int TotalDonado() throws RemoteException;
	public int TotalDonaciones() throws RemoteException;
}
