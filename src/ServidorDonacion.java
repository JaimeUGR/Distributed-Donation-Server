

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLOutput;
import java.util.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.*;
import java.rmi.registry.Registry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ServidorDonacion extends UnicastRemoteObject implements ISD_ServidorNuevoCliente, ISD_Servidor, ISD_ServidorCliente
{
	private ReplicaInfo replicaInfo;
	private int totalDonado;
	private int totalDonaciones;

	private ConcurrentHashMap<String, ClienteInfo> clientesRegistrados;
	private HashMap<String, ClienteDonacion> clientesAbiertos;
	private SortedMap<Integer, ReplicaInfo> replicas;

	public ServidorDonacion(ReplicaInfo replicaInfo, ReplicaInfo repMaestra) throws RemoteException
	{
		super();

		replicaInfo.replica = this;
		this.replicaInfo = replicaInfo;

		this.totalDonado = 0;
		this.totalDonaciones = 0;

		this.clientesRegistrados = new ConcurrentHashMap<>();
		this.clientesAbiertos = new HashMap<>();
		this.replicas = new TreeMap<>();

		if (repMaestra == null)
			return;

		// Registrar las réplicas y enviar mi referencia
		Registry mireg = null;

		// Obtengo la réplica maestra
		try
		{
			mireg = LocateRegistry.getRegistry(repMaestra.ip, repMaestra.puerto);
			repMaestra.replica = (ISD_Servidor)mireg.lookup("servidorDonacion");

			ArrayList<ReplicaInfo> replicas = repMaestra.replica.GetReplicas();

			// La réplica maestra es la última
			repMaestra.replicaID = replicas.get(replicas.size() - 1).replicaID;
			replicas.remove(replicas.size() - 1);

			// Una vez tengo las réplicas, las registro todas
			for (ReplicaInfo rep : replicas)
			{
				// Me registro en la réplica
				try
				{
					mireg = LocateRegistry.getRegistry(rep.ip, rep.puerto);
					rep.replica = (ISD_Servidor)mireg.lookup("servidorDonacion");

					// Intentar registrarme
					int numReplica = rep.replica.RegistrarReplica(this.replicaInfo);

					// TODO: Si una replica falla, se pierde la interacción con ella
					if (numReplica < 0)
					{
						rep.replica = null;
						System.err.println("No he podido registrarme en la réplica");
						continue;
					}

					// Registrar la réplica junto a su número en mi SortedSet
					System.out.println("Me he registrado en la réplica " + numReplica);
					rep.replicaID = numReplica;
					this.replicas.put(numReplica, rep);
				}
				catch (NotBoundException | RemoteException e)
				{
					System.err.println("Excepción: " + e);
				}

				this.replicas.put(rep.replicaID, rep);
			}
		}
		catch (NotBoundException | RemoteException e)
		{
			System.err.println("Excepción: " + e);
		}

		// Me registro en la réplica maestra
		if (repMaestra.replica.RegistrarReplica(this.replicaInfo) < 0)
			throw new Excepciones.SD_ReplicaRegistrada("No me he podido registrar en la réplica maestra");

		this.replicas.put(repMaestra.replicaID, repMaestra);
		System.out.println("Me he registrado en la réplica Maestra");
	}

	public ArrayList<ReplicaInfo> GetReplicas() throws RemoteException
	{
		ArrayList<ReplicaInfo> replicas = new ArrayList<>(this.replicas.size() + 1);

		if (!this.replicas.isEmpty())
			replicas.addAll(this.replicas.values());

		replicas.add(this.replicaInfo);

		return replicas;
	}

	//
	// MÉTODOS SERVIDOR
	//
	@Override
	public int RegistrarReplica(ReplicaInfo replicaInfo) throws RemoteException, Excepciones.SD_ReplicaRegistrada
	{
		if (this.replicas.containsKey(replicaInfo.replicaID))
			throw new Excepciones.SD_ReplicaRegistrada("La réplica " + replicaInfo.replicaID + " ya está registrada en el servidor " + this.replicaInfo.replicaID);

		System.out.printf("Registrando la réplica " + replicaInfo.replicaID);

		this.replicas.put(replicaInfo.replicaID, replicaInfo);

		return this.replicaInfo.replicaID;
	}

	@Override
	public ReplicaInfo ReplicaInfo() throws RemoteException
	{
		return this.replicaInfo;
	}

	@Override
	public ISD_Cliente RegistrarCliente(String token, int repSeleccionada, int repSelecRegistrados) throws RemoteException
	{
		// Soy el servidor maestro => Hago el registro completo
		if (this.replicas.isEmpty() || this.replicaInfo.replicaID < this.replicas.firstKey())
		{
			if (this.clientesRegistrados.containsKey(token))
				throw new Excepciones.SD_YaRegistrado(Integer.toString(this.replicaInfo.replicaID));

			// TODO: Posible problema si registro una réplica en este momento
			// Hago las peticiones a todos los servidores
			System.out.println("Iniciando registro del cliente: " + token);
			for (int key : this.replicas.keySet())
			{
				System.out.println("Compruebo la réplica: " + key);
				if (key == repSeleccionada)
					continue;

				ReplicaInfo repInfo = this.replicas.get(key);
				int numRegistrados = repInfo.replica.EstaClienteRegistrado(token);

				// Está registrado en esa réplica
				if (numRegistrados < 0)
					throw new Excepciones.SD_YaRegistrado(Integer.toString(key));

				// Seleccionamos esa si es mejor
				if (numRegistrados < repSelecRegistrados)
				{
					repSelecRegistrados = numRegistrados;
					repSeleccionada = key;
				}
			}

			System.out.println("He seleccionado " + repSeleccionada);

			// Si soy yo el mejor, lo registro
			if (this.clientesRegistrados.size() < repSelecRegistrados || repSeleccionada == this.replicaInfo.replicaID)
			{
				System.out.println(this.replicaInfo.replicaID + " --> Registro el cliente de token: " + token);

				this.clientesRegistrados.put(token, new ClienteInfo());
				return this.IniciarSesionServidor(token);
			}

			return this.replicas.get(repSeleccionada).replica.RegistrarCliente(token, -1, -1);
		}

		// No soy el servidor maestro => Lo debo registrar yo
		System.out.println(this.replicaInfo.replicaID + " --> Registro el cliente de token: " + token);

		// Hago el registro y le inicio sesión
		this.clientesRegistrados.put(token, new ClienteInfo());
		return this.IniciarSesionServidor(token);
	}

	@Override
	public ISD_Cliente IniciarSesionServidor(String token) throws RemoteException, Excepciones.SD_NoRegistrado
	{
		if (!clientesRegistrados.containsKey(token))
			throw new Excepciones.SD_NoRegistrado();

		if (!clientesAbiertos.containsKey(token))
		{
			// Crear un nuevo cliente remoto
			ClienteDonacion cliente = new ClienteDonacion(this, token);
			clientesAbiertos.put(token, cliente);

			// TODO: Mejorar este registro para no tener que almacenarlo ahí
			cliente.refRem = (ISD_Cliente) UnicastRemoteObject.exportObject(cliente, 0);
		}

		System.out.println("El cliente " + token + " inicia sesión");
		ClienteDonacion cliente = clientesAbiertos.get(token);
		cliente.sesiones++;

		return (ISD_Cliente) cliente.refRem;
	}

	@Override
	public int EstaClienteRegistrado(String token) throws RemoteException
	{
		int numRegistrados = this.clientesRegistrados.size();

		if (this.clientesRegistrados.containsKey(token))
			numRegistrados = -1;

		// Si está registrado, devuelve -1, si no lo está, devuelve el número de clientes registrados
		return numRegistrados;
	}

	@Override
	public int TotalDonado() throws RemoteException
	{
		return this.totalDonado;
	}

	@Override
	public int TotalDonaciones() throws RemoteException
	{
		return this.totalDonaciones;
	}

	//
	// Métodos cliente sin registrar
	//

	public synchronized ISD_Cliente Registrar(String ip) throws RemoteException, Excepciones.SD_YaRegistrado
	{
		// TODO: Obtener el token del cliente a partir del usuario + contraseña
		String token = ip;

		// Optimización en caso de que el cliente esté en este servidor
		if (clientesRegistrados.containsKey(token))
			throw new Excepciones.SD_YaRegistrado("Registrado en la réplica " + this.replicaInfo.replicaID);

		ISD_Servidor replicaMaestra = (ISD_Servidor) this;

		// Hay alguna replica y no soy la maestra
		if (!this.replicas.isEmpty() && this.replicaInfo.replicaID > this.replicas.firstKey())
			replicaMaestra = this.replicas.get(this.replicas.firstKey()).replica;

		return replicaMaestra.RegistrarCliente(token, this.replicaInfo.replicaID, this.clientesRegistrados.size());
	}

	public synchronized ISD_Cliente IniciarSesion(String token) throws RemoteException, Excepciones.SD_NoRegistrado
	{
		if (!clientesRegistrados.containsKey(token))
		{
			// TODO: Comprobar si este cliente está en otra réplica registrado
			for (int key : this.replicas.keySet())
				// Si el cliente está registrado, le iniciamos una sesión
				if (this.replicas.get(key).replica.EstaClienteRegistrado(token) < 0)
					return this.replicas.get(key).replica.IniciarSesionServidor(token);

			throw new Excepciones.SD_NoRegistrado();
		}

		return this.IniciarSesionServidor(token);
	}


	//
	// Métodos Cliente Registrado
	//

	public synchronized boolean Cliente_HacerDonacion(String token, int cantidad) throws RemoteException, Excepciones.SD_NoRegistrado
	{
		if (!clientesRegistrados.containsKey(token))
			// TODO: Comprobar si el cliente está registrado en una réplica (No necesario si usamos el ClienteRemoto)
			throw new Excepciones.SD_NoRegistrado();

		// Guardar la información en el cliente
		ClienteInfo cliente = clientesRegistrados.get(token);
		cliente.totalDonado += cantidad;
		cliente.numDonaciones++;

		// Guardar la donación
		this.totalDonado += cantidad;
		this.totalDonaciones++;

		return true;
	}

	public synchronized int Cliente_TotalDonado(String token) throws RemoteException, Excepciones.SD_NoRegistrado, Excepciones.SD_NoDonado
	{
		if (!clientesRegistrados.containsKey(token))
			// TODO: Comprobar si el cliente está registrado en una réplica (No necesario si usamos ClienteRemoto)
			throw new Excepciones.SD_NoRegistrado();

		if (clientesRegistrados.get(token).numDonaciones < 1)
			throw new Excepciones.SD_NoDonado();

		int totalDonado = this.totalDonado;

		for (ReplicaInfo repInfo : this.replicas.values())
			totalDonado += repInfo.replica.TotalDonado();

		return totalDonado;
	}

	public int Cliente_TotalDonaciones(String token) throws RemoteException, Excepciones.SD_NoRegistrado, Excepciones.SD_NoDonado
	{
		if (!clientesRegistrados.containsKey(token))
			// TODO: Comprobar si el cliente está registrado en una réplica (No necesario si usamos ClienteRemoto)
			throw new Excepciones.SD_NoRegistrado();

		if (clientesRegistrados.get(token).numDonaciones < 1)
			throw new Excepciones.SD_NoDonado();

		int totalDonaciones = this.totalDonaciones;

		for (ReplicaInfo repInfo : this.replicas.values())
			totalDonaciones += repInfo.replica.TotalDonaciones();

		return totalDonaciones;
	}

	public synchronized int Cliente_MiTotalDonado(String token) throws RemoteException, Excepciones.SD_NoRegistrado
	{
		if (!clientesRegistrados.containsKey(token))
			throw new Excepciones.SD_NoRegistrado();

		return clientesRegistrados.get(token).totalDonado;
	}

	public synchronized int Cliente_MisDonaciones(String token) throws RemoteException, Excepciones.SD_NoRegistrado
	{
		if (!clientesRegistrados.containsKey(token))
			throw new Excepciones.SD_NoRegistrado();

		return clientesRegistrados.get(token).numDonaciones;
	}

	public synchronized boolean Cliente_CerrarSesion(String token) throws RemoteException, Excepciones.SD_NoRegistrado
	{
		if (!clientesRegistrados.containsKey(token))
			throw new Excepciones.SD_NoRegistrado();

		// Cancelar la exportación de la referencia si el cliente ya no tiene usos
		if (!clientesAbiertos.containsKey(token))
			return true;

		ClienteDonacion cliente = clientesAbiertos.get(token);
		cliente.sesiones--;

		System.out.println("El cliente " + token + " cierra sesión. Restantes: " + cliente.sesiones);

		if (cliente.sesiones <= 0)
		{
			try
			{
				UnicastRemoteObject.unexportObject(cliente, true);
				clientesAbiertos.remove(token);
			}
			catch (NoSuchObjectException e)
			{
				System.err.println("Error cerrando sesión: " + e);
			}

			return true;
		}

		return false;
	}

	public int Cliente_ConsultarIDReplica() throws RemoteException
	{
		return this.replicaInfo.replicaID;
	}
}
