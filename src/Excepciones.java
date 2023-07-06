
import java.rmi.RemoteException;

public class Excepciones
{
	public static class SD_ReplicaRegistrada extends RemoteException
	{
		public SD_ReplicaRegistrada(String message)
		{
			super(message);
		}
	}

	public static class SD_YaRegistrado extends RemoteException
	{
		public SD_YaRegistrado(String serverDir)
		{
			super("Ya te has registrado en un servidor: " + serverDir);
		}
	}

	public static class SD_NoRegistrado extends RemoteException
	{
		public SD_NoRegistrado()
		{
			super();
		}
	}

	public static class SD_NoDonado extends RemoteException
	{
		public SD_NoDonado()
		{
			super();
		}
	}
}
