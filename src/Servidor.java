
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.ArrayList;

public class Servidor
{
	public static void main(String[] args)
	{
		if (System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());

		String miIp = "";
		int miPuerto = 0;
		int numServidor = 0;

		if (args.length < 3)
		{
			System.err.println("Argumentos: NumeroServidor Ip Puerto");
			System.exit(1);
		}

		numServidor = Integer.parseUnsignedInt(args[0]);
		miIp = args[1];
		miPuerto = Integer.parseUnsignedInt(args[2]);

		// Pasar la réplica maestra
		ReplicaInfo repMaestra = null;

		if (args.length >= 5)
			repMaestra = new ReplicaInfo(args[3], Integer.parseUnsignedInt(args[4]), null, -1);

		try
		{
			Registry reg = LocateRegistry.createRegistry(miPuerto);
			ServidorDonacion servDon = new ServidorDonacion(new ReplicaInfo(miIp, miPuerto, null, numServidor), repMaestra);

			// Esta distinción no es necesaria, pero la haremos por comodidad
			reg.rebind("servidorDonacion", servDon);
			reg.rebind("clienteDonacion", servDon);

			System.out.println("Servidor Donaciones Preparado");
		}
		catch (RemoteException e)
		{
			System.out.println("Exception: " + e.getMessage());
		}

		System.out.println("Hello world!");
	}
}
