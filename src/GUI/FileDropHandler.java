package GUI;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class FileDropHandler implements DropTargetListener {

	@Override
	public final void dragEnter(DropTargetDragEvent dtde) {
	}

	@Override
	public final void dragOver(DropTargetDragEvent dtde) {
	}

	@Override
	public final void dropActionChanged(DropTargetDragEvent dtde) {
	}

	@Override
	public final void dragExit(DropTargetEvent dte) {
	}

	@Override
	@SuppressWarnings("unchecked")
	public void drop(final DropTargetDropEvent dtde) {
		if (dtde.getCurrentDataFlavorsAsList().contains(DataFlavor.javaFileListFlavor)) {
			dtde.acceptDrop(1);
			try {
				final List<File> files = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
				final List<Path> filePaths = new ArrayList<>(files.size());

				for (final File f : files) {
					filePaths.add(FileSystems.getDefault().getPath(f.getAbsolutePath()));
				}

				handleFiles(filePaths);
			} catch (UnsupportedFlavorException | IOException e) {
				System.err.println("FILE DROP ERROR!\n\nWhat did you do???");
				e.printStackTrace();
			}
		}
	}

	public abstract void handleFiles(final List<Path> files);

}
