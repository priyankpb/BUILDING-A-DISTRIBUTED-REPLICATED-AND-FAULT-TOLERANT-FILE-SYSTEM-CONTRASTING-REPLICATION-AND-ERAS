package cs555RS.nodes;

import java.io.IOException;
import java.net.Socket;

public abstract interface Node
{
  public abstract void onEvent(byte[] paramArrayOfByte, Socket paramSocket)
    throws IOException;
}

