package backEnd;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class Wallet {
    /**
     * The RandomAccessFile of the wallet file
     */
    private RandomAccessFile file;
    private FileChannel fileChannel;

    /**
     * Creates a Wallet object
     *
     * A Wallet object interfaces with the wallet RandomAccessFile
     */
    public Wallet () throws Exception {
	this.file = new RandomAccessFile(new File("backEnd/wallet.txt"), "rw");
    this.fileChannel = this.file.getChannel();
    }

    /**
     * Gets the wallet balance.
     *
     * @return                   The content of the wallet file as an integer
     */
    public synchronized int getBalance() throws IOException {
        this.file.seek(0);
        return Integer.parseInt(this.file.readLine());

    }

    /**
     * TOCTOU safe method for withdrawing money from the wallet
     *
     * @param valueToWithdraw the amount to withdraw
     * @return True if the withdrawal succeeded
     * @throws Exception
     */
    public synchronized boolean safeWithdraw(int valueToWithdraw) throws Exception {
        try (FileLock lock = fileChannel.lock(0, Long.MAX_VALUE, false)) {
            int currentBalance = getBalance();
            if (currentBalance >= valueToWithdraw) {
                setBalance(currentBalance - valueToWithdraw);
                lock.release();
                return true;
            }
            lock.release();
            return false;
        }
    }
    /**
     * Sets a new balance in the wallet
     *
     * @param  newBalance          new balance to write in the wallet
     */
    public synchronized void setBalance(int newBalance) throws Exception {
        this.file.setLength(0);
        String str = Integer.valueOf(newBalance).toString() + '\n';
        this.file.writeBytes(str);

    }
    /**
     * Closes the RandomAccessFile in this.file
     */
    public synchronized void close() throws Exception {
        fileChannel.close();
        file.close();
    }
}
