import java.io.Serializable;

public class ReplicaInfo implements Comparable<ReplicaInfo>, Serializable
{
	public String ip;
	public int puerto;
	public int replicaID;
	public ISD_Servidor replica;

	public ReplicaInfo(String ip, int puerto, ISD_Servidor serv, int replicaID)
	{
		this.ip = ip;
		this.puerto = puerto;
		this.replicaID = replicaID;
		this.replica = serv;
	}

	@Override
	public int compareTo(ReplicaInfo rep)
	{
		return Integer.compare(this.replicaID, rep.replicaID);
	}
}
