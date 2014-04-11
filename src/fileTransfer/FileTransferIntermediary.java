package fileTransfer;

public interface FileTransferIntermediary {

	public abstract void setString(String string);

	public abstract void setValue(int value);

	public abstract void fail(Exception e);

	public abstract void cancel();

	public abstract void done();

}