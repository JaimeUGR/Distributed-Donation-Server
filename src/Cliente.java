
import java.net.MalformedURLException;
import java.rmi.registry.LocateRegistry;
import java.rmi.*;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Cliente
{
	private static Scanner menuScanner;

	private static int LeerOpcion()
	{
		String cadena = menuScanner.nextLine();
		int opcion = 0;

		try
		{
			opcion = Integer.parseInt(cadena);
		}
		catch (NumberFormatException e)
		{
			System.err.println("Error: " + e);
			opcion = -1;
		}

		return opcion;
	}

	private static int LeerNumeroDonacion()
	{
		int cantidad = 0;

		do
		{
			System.out.println("Introduce la cantidad a donar (mayor que 0)");
			String cadena = menuScanner.nextLine();
			try
			{
				cantidad = Integer.parseInt(cadena);
			}
			catch (NumberFormatException e)
			{
				System.err.println("Error: " + e);
				cantidad = -1;
			}
		} while(cantidad <= 0);

		return cantidad;
	}

	public static void main(String[] args)
	{
		menuScanner = new Scanner(System.in);

		if (System.getSecurityManager() == null)
		{
			System.setSecurityManager(new SecurityManager());
		}

		if (args.length < 3)
		{
			System.err.println("Argumentos: MiIp IpServidor PuertoServidor");
			System.exit(1);
		}

		String miip = args[0];
		String ip = args[1];
		int puerto = Integer.parseUnsignedInt(args[2]);

		try
		{
			Registry mireg = LocateRegistry.getRegistry(ip, puerto);

			// Obtener el servidor nuevo cliente
			ISD_ServidorNuevoCliente servidor = (ISD_ServidorNuevoCliente)mireg.lookup("clienteDonacion");

			int opcionSeleccionada = -1;
			ISD_Cliente cliente = null;

			// Mostrar el menú inicial
			System.out.println("Elige una opción\n");
			System.out.println("[1] Registrarse");
			System.out.println("[2] Iniciar Sesión");

			while (opcionSeleccionada != 1 && opcionSeleccionada != 2)
				opcionSeleccionada = LeerOpcion();

			switch (opcionSeleccionada)
			{
				case 1:
					cliente = servidor.Registrar(miip);
					break;
				case 2:
					cliente = servidor.IniciarSesion(miip);
					break;
				default:
					System.exit(0);
					break;
			}

			System.out.println("Me estoy comunicando con el servidor " + cliente.ConsultaIDReplica() + "\n");

			while (opcionSeleccionada != 6)
			{
				System.out.println("Selecciona una acción: ");
				System.out.println("[1] Hacer donación");
				System.out.println("[2] Consultar total donado");
				System.out.println("[3] Consultar número donaciones totales");
				System.out.println("[4] Consultar mi total donado");
				System.out.println("[5] Consultar mi número de donaciones");
				System.out.println("[6] Cerrar sesión");

				System.out.print("\nOpción >> ");
				opcionSeleccionada = LeerOpcion();
				System.out.println("\nHas seleccionado la opción " + opcionSeleccionada + "\n");

				switch (opcionSeleccionada)
				{
					case 1:
						int donacion = LeerNumeroDonacion();
						cliente.HacerDonacion(donacion);
						System.out.println("Se ha hecho la donación con éxito");
						break;
					case 2:
						try
						{
							System.out.println("En total se ha donado " + cliente.ConsultaTotalDonado());
						}
						catch (Excepciones.SD_NoDonado e)
						{
							System.out.println("Tienes que realizar al menos una donación para acceder a esta información");
						}
						break;
					case 3:
						try
						{
							System.out.println("En total se han hecho " + cliente.ConsultaTotalDonaciones() + " donaciones");
						}
						catch (Excepciones.SD_NoDonado e)
						{
							System.out.println("Tienes que realizar al menos una donación para acceder a esta información");
						}
						break;
					case 4:
						System.out.println("Has donado " + cliente.ConsultaMiTotalDonado() + " en total");
						break;
					case 5:
						System.out.println("Has hecho " + cliente.ConsultaMisDonaciones() + " donaciones");
						break;
					case 6:
						cliente.CerrarSesion();
						System.out.println("Sesión cerrada");
						break;
					default:
						System.out.println("Opción desconocida, inténtalo de nuevo");
						break;
				}

				System.out.println("\n\n");
			}
		}
		catch(NotBoundException | RemoteException e)
		{
			System.err.println("Exception del sistema: " + e);
		}
		finally
		{
			menuScanner.close();
		}
	}
}
