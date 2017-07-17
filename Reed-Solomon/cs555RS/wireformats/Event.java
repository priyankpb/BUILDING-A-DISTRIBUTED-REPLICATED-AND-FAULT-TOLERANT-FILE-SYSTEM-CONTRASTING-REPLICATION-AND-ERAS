package cs555RS.wireformats;

public abstract interface Event
{
  public abstract byte[] getByte()
    throws Exception;
  
  public abstract byte getType();
}


/* Location:              C:\Users\YANK\Dropbox\CSU\cs555a1.jar!\cs555\wireformats\Event.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */